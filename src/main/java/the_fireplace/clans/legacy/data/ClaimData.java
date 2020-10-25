package the_fireplace.clans.legacy.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.util.internal.ConcurrentSet;
import net.minecraft.entity.Entity;
import the_fireplace.clans.ClansModContainer;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanDatabase;
import the_fireplace.clans.io.FileToJsonObject;
import the_fireplace.clans.io.JsonWritable;
import the_fireplace.clans.legacy.model.ChunkPosition;
import the_fireplace.clans.legacy.model.ChunkPositionWithData;
import the_fireplace.clans.legacy.model.ClanDimInfo;
import the_fireplace.clans.legacy.model.CoordNodeTree;
import the_fireplace.clans.legacy.util.JsonHelper;
import the_fireplace.clans.multithreading.ThreadedSaveHandler;
import the_fireplace.clans.multithreading.ThreadedSaveable;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public final class ClaimData {
    private static boolean isLoaded = false;
    private static final File CHUNK_DATA_LOCATION = new File(ClansModContainer.getMinecraftHelper().getServer().getWorld(0).getSaveHandler().getWorldDirectory(), "clans/chunk");
    //The map commands use this, so it should be divisible by 7 and not exceed 53.
    // Divisible by 7 so the smaller map can take exactly a seventh of the section.
    // 53 map-width characters is all the chat window can fit before going to a new line.
    // 49 is ideal because it is the largest number that fits those conditions.
    public static final byte CACHE_SECTION_SIZE = 49;

    //Main storage
    private static ConcurrentMap<UUID, ClaimStoredData> claimedChunks;
    //Cache for easy access to data based on chunk position
    //chunkx / CACHE_SECTION_SIZE, z / CACHE_SECTION_SIZE, position, data
    private static ConcurrentMap<Integer, ConcurrentMap<Integer, ConcurrentMap<ChunkPositionWithData, ClaimStoredData>>> claimDataMap;

    private static ConcurrentMap<UUID, Integer> regenBordersTimer;

    private static ConcurrentMap<ChunkPositionWithData, ClaimStoredData> getCacheSection(ChunkPosition pos) {
        int sectionX = pos.getPosX() / CACHE_SECTION_SIZE;
        int sectionZ = pos.getPosZ() / CACHE_SECTION_SIZE;
        claimDataMap.putIfAbsent(sectionX, new ConcurrentHashMap<>());
        claimDataMap.get(sectionX).putIfAbsent(sectionZ, new ConcurrentHashMap<>());
        return claimDataMap.get(sectionX).get(sectionZ);
    }

    public static Set<ChunkPositionWithData> getClaimedChunks(UUID clan) {
        loadIfNeeded();
        ClaimStoredData claimed = claimedChunks.get(clan);
        return Collections.unmodifiableSet(claimed != null ? claimed.getChunks().stream().filter(d -> !d.isBorderland()).collect(Collectors.toSet()) : Collections.emptySet());
    }

    private static void loadIfNeeded() {
        if (!isLoaded)
            load();
    }

    public static long getClaimedChunkCount(UUID clan) {
        loadIfNeeded();
        ClaimStoredData claimed = claimedChunks.get(clan);
        return claimed != null ? claimed.getChunks().stream().filter(d -> !d.isBorderland()).count() : 0;
    }

    public static Set<ChunkPositionWithData> getBorderlandChunks(UUID clan) {
        loadIfNeeded();
        ClaimStoredData claimed = claimedChunks.get(clan);
        return Collections.unmodifiableSet(claimed != null ? claimed.getChunks().stream().filter(ChunkPositionWithData::isBorderland).collect(Collectors.toSet()) : Collections.emptySet());
    }

    public static Set<Clan> clansWithClaims() {
        loadIfNeeded();
        Set<Clan> claimClans = Sets.newHashSet();
        for(Map.Entry<UUID, ClaimStoredData> clanId: Sets.newHashSet(claimedChunks.entrySet())) {
            Clan clan = ClanDatabase.getClanById(clanId.getKey());
            if(clan != null) {
                if (!clanId.getValue().getChunks().isEmpty())
                    claimClans.add(clan);
            } else
                delClan(clanId.getKey());
        }
        return Collections.unmodifiableSet(claimClans);
    }

    public static void addChunk(Clan clan, ChunkPositionWithData pos) {
        loadIfNeeded();
        claimedChunks.putIfAbsent(clan.getId(), new ClaimStoredData(clan.getId()));
        claimedChunks.get(clan.getId()).addChunk(pos);
        getCacheSection(pos).put(pos, claimedChunks.get(clan.getId()));
        if(!pos.isBorderland()) {
            resetRegenBorderlandsTimer(clan.getId());
            clan.incrementCachedClaimCount();
        }
    }

    public static void addChunk(@Nullable UUID clanId, ChunkPositionWithData pos) {
        Clan clan = ClanDatabase.getClanById(clanId);
        if(clan == null)
            delClan(clanId);
        else
            addChunk(clan, pos);
    }

    public static void delChunk(Clan clan, ChunkPositionWithData pos) {
        loadIfNeeded();
        claimedChunks.putIfAbsent(clan.getId(), new ClaimStoredData(clan.getId()));
        getCacheSection(pos).remove(pos);
        if(claimedChunks.get(clan.getId()).delChunk(pos) && !pos.isBorderland()) {
            resetRegenBorderlandsTimer(clan.getId());
            clan.decrementCachedClaimCount();
        }
    }

    public static void resetRegenBorderlandsTimer(UUID clanId) {
        regenBordersTimer.put(clanId, 5);
    }

    /**
     * Delete a claim. If you already have the clan, use the delChunk method that takes it for efficiency.
     */
    public static void delChunk(@Nullable UUID clanId, ChunkPositionWithData pos) {
        Clan clan = ClanDatabase.getClanById(clanId);
        if(clan == null)
            delClan(clanId);
        else
            delChunk(clan, pos);
    }

    public static List<Integer> getClaimDims(UUID clanId) {
        List<Integer> dims = Lists.newArrayList();
        for(ChunkPositionWithData pos: getClaimedChunks(clanId))
            if(!dims.contains(pos.getDim()))
                dims.add(pos.getDim());
            return Collections.unmodifiableList(dims);
    }

    public static boolean isBorderland(int x, int z, int d) {
        ChunkPositionWithData data = getChunkPositionData(new ChunkPosition(x, z, d));
        return data != null && data.isBorderland();
    }

    @Nullable
    public static ChunkPositionWithData getChunkPositionData(int x, int z, int d) {
        return getChunkPositionData(new ChunkPosition(x, z, d));
    }

    @Nullable
    public static ChunkPositionWithData getChunkPositionData(Entity entity) {
        return getChunkPositionData(new ChunkPosition(entity.chunkCoordX, entity.chunkCoordZ, entity.dimension));
    }

    @Nullable
    public static ChunkPositionWithData getChunkPositionData(ChunkPosition pos) {
        loadIfNeeded();
        return getCacheSection(pos).keySet().stream().filter(p -> p.equals(pos)).findFirst().orElse(null);
    }

    @Nullable
    public static Clan getChunkClan(@Nullable ChunkPositionWithData pos) {
        return ClanDatabase.getClanById(getChunkClanId(pos));
    }

    @Nullable
    public static Clan getChunkClan(int x, int z, int dim) {
        return ClanDatabase.getClanById(getChunkClanId(x, z, dim));
    }

    @Nullable
    public static Clan getChunkClan(Entity entity) {
        return ClanDatabase.getClanById(getChunkClanId(entity.chunkCoordX, entity.chunkCoordZ, entity.dimension));
    }

    @Nullable
    public static UUID getChunkClanId(int x, int z, int dim) {
        loadIfNeeded();
        ClaimStoredData data = getCacheSection(new ChunkPosition(x, z, dim)).get(new ChunkPositionWithData(x, z, dim));
        return data != null ? data.clan : null;
    }

    @Nullable
    public static UUID getChunkClanId(@Nullable ChunkPosition position) {
        loadIfNeeded();
        if(position == null)
            return null;
        if(!(position instanceof ChunkPositionWithData))
            position = new ChunkPositionWithData(position);
        ClaimStoredData data = getCacheSection(position).get(position);
        return data != null ? data.clan : null;
    }

    @Nullable
    public static ClaimStoredData getChunkClaimData(int x, int z, int dim) {
        loadIfNeeded();
        return getCacheSection(new ChunkPosition(x, z, dim)).get(new ChunkPositionWithData(x, z, dim));
    }

    @Nullable
    public static ClaimStoredData getChunkClaimData(ChunkPositionWithData position) {
        loadIfNeeded();
        return getCacheSection(position).get(position);
    }

    public static boolean delClan(@Nullable UUID clan) {
        if(clan == null)
            return false;
        if(claimedChunks.containsKey(clan)) {
            removeClanFromCache(clan);
            claimedChunks.get(clan).chunkDataFile.delete();
        }
        return claimedChunks.remove(clan) != null;
    }

    /**
     * This must be called BEFORE the clan is removed from claimedChunks.
     * An alternative way could be created, but it would be far worse for performance.
     */
    private static void removeClanFromCache(UUID clan) {
        for(ChunkPositionWithData pos: claimedChunks.get(clan).chunks)
            getCacheSection(pos).remove(pos);
    }

    public static void updateChunkOwner(ChunkPositionWithData pos, @Nullable UUID oldOwner, UUID newOwner) {
        delChunk(oldOwner != null ? oldOwner : getChunkClanId(pos), pos);
        //Create a new ChunkPositionWithData because the old one has addon data and borderland data attached
        addChunk(newOwner, new ChunkPositionWithData(pos));
    }

    public static boolean chunkDataExists(ChunkPositionWithData position) {
        return getCacheSection(position).containsKey(position);
    }

    /**
     * Use a timer so it isn't regenerating every single time a claim or abandon is done.
     */
    public static void decrementBorderlandsRegenTimers() {
        loadIfNeeded();
        if(ClansModContainer.getConfig().isEnableBorderlands())
            for(Map.Entry<UUID, Integer> entry: Sets.newHashSet(regenBordersTimer.entrySet())) {
                if(entry.getValue() <= 0) {
                    regenBordersTimer.remove(entry.getKey());
                    Clan target = ClanDatabase.getClanById(entry.getKey());
                    if(target == null)//Skip if clan has disbanded
                        continue;
                    claimedChunks.get(entry.getKey()).regenBorderlands();
                    for(int dim: getClaimDims(entry.getKey()))
                        ClansModContainer.getDynmapCompat().queueClaimEventReceived(new ClanDimInfo(target, dim));
                } else {
                    decrementRegenBorderlandsTimer(entry);
                }
            }
    }

    private static void decrementRegenBorderlandsTimer(Map.Entry<UUID, Integer> entry) {
        regenBordersTimer.put(entry.getKey(), entry.getValue() - 1);
    }

    private static void load() {
        if(!CHUNK_DATA_LOCATION.exists())
            CHUNK_DATA_LOCATION.mkdirs();
        claimedChunks = new ConcurrentHashMap<>();
        claimDataMap = new ConcurrentHashMap<>();
        regenBordersTimer = new ConcurrentHashMap<>();
        File[] files = CHUNK_DATA_LOCATION.listFiles();
        if(files != null)
            for(File file: files) {
                try {
                    ClaimStoredData loadedData = ClaimStoredData.load(file);
                    if(loadedData != null) {
                        claimedChunks.put(loadedData.clan, loadedData);
                        for(ChunkPositionWithData cPos : loadedData.getChunks())
                            getCacheSection(cPos).put(cPos, loadedData);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        isLoaded = true;
        for(Map.Entry<UUID, ClaimStoredData> entry : claimedChunks.entrySet()) {
            if(ClansModContainer.getConfig().isEnableBorderlands() && !entry.getValue().hasBorderlands)
                entry.getValue().genBorderlands();
            else if(!ClansModContainer.getConfig().isEnableBorderlands() && entry.getValue().hasBorderlands)
                entry.getValue().clearBorderlands();
        }
    }

    public static void save() {
        for(ClaimStoredData data: claimedChunks.values())
            data.save();
    }

    public static class ClaimStoredData implements ThreadedSaveable, JsonWritable {
        private final File chunkDataFile;
        private final ThreadedSaveHandler<ClaimStoredData> saveHandler = ThreadedSaveHandler.create(this);

        private final UUID clan;
        private Set<ChunkPositionWithData> chunks;
        private boolean hasBorderlands;

        private ClaimStoredData(UUID clan) {
            chunkDataFile = new File(CHUNK_DATA_LOCATION, clan.toString()+".json");
            this.clan = clan;
            this.chunks = new ConcurrentSet<>();
        }

        @Nullable
        private static ClaimStoredData load(File file) {
            JsonObject obj = FileToJsonObject.readJsonFile(file);
            if(obj == null)
                return null;
            UUID clan = UUID.fromString(obj.getAsJsonPrimitive("clan").getAsString());
            Set<ChunkPositionWithData> positions = Sets.newHashSet();
            ClaimStoredData loadClaimStoredData = new ClaimStoredData(clan);
            for (JsonElement element : obj.get("chunks").getAsJsonArray()) {
                ChunkPositionWithData pos = new ChunkPositionWithData(element.getAsJsonObject().get("x").getAsInt(), element.getAsJsonObject().get("z").getAsInt(), element.getAsJsonObject().get("d").getAsInt());
                pos.setBorderland(element.getAsJsonObject().has("isBorderland") && element.getAsJsonObject().get("isBorderland").getAsBoolean());
                pos.getAddonData().putAll(JsonHelper.getAddonData(element.getAsJsonObject()));
                getCacheSection(pos).put(pos, loadClaimStoredData);
                positions.add(pos);
            }
            loadClaimStoredData.chunks = new ConcurrentSet<>();
            loadClaimStoredData.chunks.addAll(positions);
            return loadClaimStoredData;
        }

        @Override
        public JsonObject toJson() {
            JsonObject obj = new JsonObject();
            obj.addProperty("clan", clan.toString());
            JsonArray positionArray = new JsonArray();
            for (ChunkPositionWithData pos : getChunks()) {
                JsonObject chunkPositionObject = new JsonObject();
                chunkPositionObject.addProperty("x", pos.getPosX());
                chunkPositionObject.addProperty("z", pos.getPosZ());
                chunkPositionObject.addProperty("d", pos.getDim());
                chunkPositionObject.addProperty("isBorderland", pos.isBorderland());
                JsonHelper.attachAddonData(chunkPositionObject, pos.getAddonData());
                positionArray.add(chunkPositionObject);
            }
            obj.add("chunks", positionArray);

            return obj;
        }

        @Override
        public void blockingSave() {
            writeToJson(chunkDataFile);
        }

        @Override
        public ThreadedSaveHandler<?> getSaveHandler() {
            return saveHandler;
        }

        public boolean addChunk(ChunkPositionWithData pos) {
            boolean added = chunks.add(pos);
            if(added)
                markChanged();
            return added;
        }

        public boolean addAllChunks(Collection<? extends ChunkPositionWithData> positions) {
            boolean added = chunks.addAll(positions);
            if(added)
                markChanged();
            return added;
        }

        public boolean delChunk(ChunkPositionWithData pos) {
            boolean removed = chunks.remove(pos);
            if(removed)
                markChanged();
            return removed;
        }

        public Set<ChunkPositionWithData> getChunks() {
            return Collections.unmodifiableSet(chunks);
        }

        public void genBorderlands() {
            if(ClansModContainer.getConfig().isEnableBorderlands()) {
                for (int d : ClansModContainer.getMinecraftHelper().getDimensionIds())
                    for (ChunkPositionWithData pos : new CoordNodeTree(d, clan).forBorderlandRetrieval().getBorderChunks().stream().filter(pos -> !getChunks().contains(pos) && !ClaimData.chunkDataExists(pos)).collect(Collectors.toSet()))
                        ClaimData.addChunk(clan, pos);
                hasBorderlands = true;
                markChanged();
            }
        }

        public void regenBorderlands() {
            clearBorderlands();
            genBorderlands();
        }

        public void clearBorderlands() {
            if(hasBorderlands) {
                for(ChunkPositionWithData chunk: getChunks())
                    if(chunk.isBorderland())
                        ClaimData.delChunk(clan, chunk);
                hasBorderlands = false;
                markChanged();
            }
        }
    }
}
