package the_fireplace.clans.legacy.data;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.util.internal.ConcurrentSet;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import net.minecraft.entity.Entity;
import the_fireplace.clans.clan.land.ClanClaimCount;
import the_fireplace.clans.io.FileToJsonObject;
import the_fireplace.clans.io.JsonWritable;
import the_fireplace.clans.legacy.ClansModContainer;
import the_fireplace.clans.legacy.api.ClaimAccessor;
import the_fireplace.clans.legacy.model.*;
import the_fireplace.clans.multithreading.ThreadedSaveHandler;
import the_fireplace.clans.multithreading.ThreadedSaveable;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public final class ClaimData implements ClaimAccessor {
    private static final File CHUNK_DATA_LOCATION = new File(ClansModContainer.getMinecraftHelper().getServer().getWorld(0).getSaveHandler().getWorldDirectory(), "clans/chunk");
    private static final byte CACHE_SECTION_SIZE = 64;
    @Deprecated
    public static final ClaimData INSTANCE = new ClaimData().load();

    //Main storage
    private final ConcurrentMap<UUID, ClaimStoredData> claimedChunks = new ConcurrentHashMap<>();
    //Cache for easy access to data based on chunk position
    //chunkx / CACHE_SECTION_SIZE, z / CACHE_SECTION_SIZE, position, data
    private final ConcurrentMap<Integer, ConcurrentMap<Integer, ConcurrentMap<ChunkPositionWithData, ClaimStoredData>>> claimDataMap = new ConcurrentHashMap<>();

    private final ConcurrentMap<UUID, Integer> regenBordersTimer = new ConcurrentHashMap<>();

    private ClaimData() {
    }

    public void save() {
        for (ClaimStoredData data: claimedChunks.values()) {
            data.save();
        }
    }

    private static final ConcurrentMap<?, ?> EMPTY_CONCURRENT_MAP = new EmptyConcurrentMap<>();
    private static <K, V> ConcurrentMap<K, V> getEmptyConcurrentMap() {
        //noinspection unchecked
        return (ConcurrentMap<K, V>) EMPTY_CONCURRENT_MAP;
    }

    private ConcurrentMap<ChunkPositionWithData, ClaimStoredData> getCacheSection(ChunkPosition pos, boolean create) {
        OrderedPair<Integer, Integer> cacheSectionCoordinates = getCacheSectionCoordinates(pos.getPosX(), pos.getPosZ());
        int sectionX = cacheSectionCoordinates.getValue1();
        int sectionZ = cacheSectionCoordinates.getValue2();
        if (create) {
            return claimDataMap.computeIfAbsent(sectionX, (unused) -> new ConcurrentHashMap<>())
                .computeIfAbsent(sectionZ, (unused) -> new ConcurrentHashMap<>());
        } else {
            return claimDataMap.getOrDefault(sectionX, getEmptyConcurrentMap()).getOrDefault(sectionZ, getEmptyConcurrentMap());
        }
    }

    private OrderedPair<Integer, Integer> getCacheSectionCoordinates(int chunkX, int chunkZ) {
        return new OrderedPair<>((int)Math.round(((double) chunkX) / CACHE_SECTION_SIZE), (int)Math.round(((double) chunkZ) / CACHE_SECTION_SIZE));
    }

    public Collection<OrderedPair<Integer, Integer>> getOccupiedCacheSections() {
        Set<OrderedPair<Integer, Integer>> cacheSections = Sets.newHashSet();
        claimDataMap.forEach((x, zMap) -> zMap.forEach((z, data) -> cacheSections.add(new OrderedPair<>(x, z))));
        return Collections.unmodifiableSet(cacheSections);
    }

    @Override
    public long getClaimedChunkCount(UUID clan) {
        ClaimStoredData claimed = claimedChunks.get(clan);
        return claimed != null ? claimed.getChunks().stream().filter(d -> !d.isBorderland()).count() : 0;
    }

    @Override
    public Set<ChunkPositionWithData> getBorderlandChunks(UUID clan) {
        ClaimStoredData claimed = claimedChunks.get(clan);
        return Collections.unmodifiableSet(claimed != null ? claimed.getChunks().stream().filter(ChunkPositionWithData::isBorderland).collect(Collectors.toSet()) : Collections.emptySet());
    }

    @Override
    public Set<UUID> getClansWithClaims() {
        Set<UUID> claimClans = Sets.newHashSet();
        for (Map.Entry<UUID, ClaimStoredData> claimedChunkEntry: claimedChunks.entrySet()) {
            UUID clan = claimedChunkEntry.getKey();
            if (!claimedChunkEntry.getValue().getChunks().isEmpty())
                claimClans.add(clan);
        }
        return Collections.unmodifiableSet(claimClans);
    }

    @Override
    public void addChunk(UUID clan, ChunkPositionWithData pos) {
        claimedChunks.computeIfAbsent(clan, ClaimStoredData::new);
        claimedChunks.get(clan).addChunk(pos);
        getCacheSection(pos, true).put(pos, claimedChunks.get(clan));
        if (!pos.isBorderland()) {
            resetRegenBorderlandsTimer(clan);
            ClanClaimCount.get(clan).incrementCachedClaimCount();
        }
    }

    @Override
    public void delChunk(UUID clan, ChunkPositionWithData pos) {
        claimedChunks.computeIfAbsent(clan, ClaimStoredData::new);
        getCacheSection(pos, true).remove(pos);
        if(claimedChunks.get(clan).delChunk(pos) && !pos.isBorderland()) {
            resetRegenBorderlandsTimer(clan);
            ClanClaimCount.get(clan).decrementCachedClaimCount();
        }
    }

    public void resetRegenBorderlandsTimer(UUID clanId) {
        regenBordersTimer.put(clanId, 5);
    }

    @Override
    public Set<Integer> getClaimDims(UUID clanId) {
        Set<Integer> dims = new IntArraySet(1);
        for (ChunkPositionWithData pos: getClaimedChunks(clanId)) {
            dims.add(pos.getDim());
        }
        return Collections.unmodifiableSet(dims);
    }

    @Override
    public boolean isBorderland(int x, int z, int d) {
        ChunkPositionWithData data = getChunkPositionData(new ChunkPosition(x, z, d));
        return data != null && data.isBorderland();
    }

    @Override
    @Nullable
    public ChunkPositionWithData getChunkPositionData(int x, int z, int d) {
        return getChunkPositionData(new ChunkPosition(x, z, d));
    }

    @Override
    @Nullable
    public ChunkPositionWithData getChunkPositionData(Entity entity) {
        return getChunkPositionData(new ChunkPosition(entity.chunkCoordX, entity.chunkCoordZ, entity.dimension));
    }

    @Override
    @Nullable
    public ChunkPositionWithData getChunkPositionData(ChunkPosition pos) {
        return getCacheSection(pos, false).keySet().stream().filter(p -> p.equals(pos)).findFirst().orElse(null);
    }

    @Override
    @Nullable
    public UUID getChunkClan(Entity entity) {
        return getChunkClan(entity.chunkCoordX, entity.chunkCoordZ, entity.dimension);
    }

    @Override
    @Nullable
    public UUID getChunkClan(int x, int z, int dim) {
        ClaimStoredData data = getCacheSection(new ChunkPosition(x, z, dim), false).get(new ChunkPositionWithData(x, z, dim));
        return data != null ? data.clan : null;
    }

    @Override
    @Nullable
    public UUID getChunkClan(@Nullable ChunkPosition position) {
        if (position == null) {
            return null;
        }
        if (!(position instanceof ChunkPositionWithData)) {
            position = new ChunkPositionWithData(position);
        }
        ClaimStoredData data = getCacheSection(position, false).get(position);
        return data != null ? data.clan : null;
    }

    @Nullable
    public ClaimStoredData getChunkClaimData(int x, int z, int dim) {
        return getCacheSection(new ChunkPosition(x, z, dim), false).get(new ChunkPositionWithData(x, z, dim));
    }

    @Nullable
    public ClaimStoredData getChunkClaimData(ChunkPositionWithData position) {
        return getCacheSection(position, false).get(position);
    }

    @Override
    public boolean deleteClanClaims(UUID clan) {
        if (claimedChunks.containsKey(clan)) {
            removeClanFromCache(clan);
            claimedChunks.get(clan).chunkDataFile.delete();
        }
        return claimedChunks.remove(clan) != null;
    }

    /**
     * This must be called BEFORE the clan is removed from claimedChunks.
     * An alternative way could be created, but it would be far worse for performance.
     */
    private void removeClanFromCache(UUID clan) {
        for (ChunkPositionWithData pos: claimedChunks.get(clan).chunks) {
            getCacheSection(pos, false).remove(pos);
        }
    }

    @Override
    public void updateChunkOwner(ChunkPositionWithData pos, @Nullable UUID oldOwner, UUID newOwner) {
        if (oldOwner == null) {
            oldOwner = getChunkClan(pos);
        }
        if (oldOwner != null) {
            delChunk(oldOwner, pos);
        }
        //Create a new ChunkPositionWithData because the old one has addon data and borderland data attached
        addChunk(newOwner, new ChunkPositionWithData(pos));
    }

    @Override
    public boolean chunkDataExists(ChunkPositionWithData position) {
        return getCacheSection(position, false).containsKey(position);
    }

    /**
     * Use a timer so it isn't regenerating every single time a claim or abandon is done.
     */
    public void decrementBorderlandsRegenTimers() {
        if (ClansModContainer.getConfig().isEnableBorderlands()) {
            for (Map.Entry<UUID, Integer> entry : Sets.newHashSet(regenBordersTimer.entrySet())) {
                if (entry.getValue() <= 0) {
                    regenBordersTimer.remove(entry.getKey());
                    claimedChunks.get(entry.getKey()).regenBorderlands();
                    for (int dim : getClaimDims(entry.getKey())) {
                        ClansModContainer.getDynmapCompat().queueClaimEventReceived(new ClanDimInfo(entry.getKey(), dim));
                    }
                } else {
                    decrementRegenBorderlandsTimer(entry);
                }
            }
        }
    }

    private void decrementRegenBorderlandsTimer(Map.Entry<UUID, Integer> entry) {
        regenBordersTimer.put(entry.getKey(), entry.getValue() - 1);
    }

    @Override
    public Set<ChunkPositionWithData> getClaimedChunks(UUID clan) {
        ClaimStoredData claimed = claimedChunks.get(clan);
        return Collections.unmodifiableSet(claimed != null ? claimed.getChunks().stream().filter(d -> !d.isBorderland()).collect(Collectors.toSet()) : Collections.emptySet());
    }

    private ClaimData load() {
        if (!CHUNK_DATA_LOCATION.exists()) {
            CHUNK_DATA_LOCATION.mkdirs();
        }
        File[] files = CHUNK_DATA_LOCATION.listFiles();
        if (files != null) {
            for (File file : files) {
                try {
                    ClaimStoredData loadedData = ClaimStoredData.load(file);
                    if (loadedData != null) {
                        claimedChunks.put(loadedData.clan, loadedData);
                        for (ChunkPositionWithData cPos : loadedData.getChunks()) {
                            getCacheSection(cPos, true).put(cPos, loadedData);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        for (UUID entry : claimedChunks.keySet()) {
            resetRegenBorderlandsTimer(entry);
        }
        
        return this;
    }

    public static class ClaimStoredData implements ThreadedSaveable, JsonWritable {
        private final File chunkDataFile;
        private final ThreadedSaveHandler<ClaimStoredData> saveHandler = ThreadedSaveHandler.create(this);

        private final UUID clan;
        private final Set<ChunkPositionWithData> chunks = new ConcurrentSet<>();
        private boolean hasBorderlands = false;

        private ClaimStoredData(UUID clan) {
            chunkDataFile = new File(CHUNK_DATA_LOCATION, clan.toString()+".json");
            this.clan = clan;
        }

        @Nullable
        private static ClaimStoredData load(File file) {
            JsonObject obj = FileToJsonObject.readJsonFile(file);
            if (obj == null) {
                return null;
            }
            UUID clan = UUID.fromString(obj.getAsJsonPrimitive("clan").getAsString());
            Set<ChunkPositionWithData> positions = Sets.newHashSet();
            ClaimStoredData loadClaimStoredData = new ClaimStoredData(clan);
            for (JsonElement element : obj.get("chunks").getAsJsonArray()) {
                ChunkPositionWithData pos = new ChunkPositionWithData(element.getAsJsonObject().get("x").getAsInt(), element.getAsJsonObject().get("z").getAsInt(), element.getAsJsonObject().get("d").getAsInt());
                pos.setBorderland(element.getAsJsonObject().has("isBorderland") && element.getAsJsonObject().get("isBorderland").getAsBoolean());
                positions.add(pos);
            }
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

        boolean addChunk(ChunkPositionWithData pos) {
            boolean added = chunks.add(pos);
            if(added)
                markChanged();
            return added;
        }

        boolean addAllChunks(Collection<? extends ChunkPositionWithData> positions) {
            boolean added = chunks.addAll(positions);
            if(added)
                markChanged();
            return added;
        }

        boolean delChunk(ChunkPositionWithData pos) {
            boolean removed = chunks.remove(pos);
            if(removed)
                markChanged();
            return removed;
        }

        Set<ChunkPositionWithData> getChunks() {
            return Collections.unmodifiableSet(chunks);
        }

        void genBorderlands() {
            if(ClansModContainer.getConfig().isEnableBorderlands()) {
                for (int d : ClansModContainer.getMinecraftHelper().getDimensionIds())
                    for (ChunkPositionWithData pos : new CoordNodeTree(d, clan).forBorderlandRetrieval().getBorderChunks().stream().filter(pos -> !getChunks().contains(pos) && !INSTANCE.chunkDataExists(pos)).collect(Collectors.toSet()))
                        INSTANCE.addChunk(clan, pos);
                hasBorderlands = true;
                markChanged();
            }
        }

        void regenBorderlands() {
            clearBorderlands();
            genBorderlands();
        }

        void clearBorderlands() {
            if(hasBorderlands) {
                for(ChunkPositionWithData chunk: getChunks())
                    if(chunk.isBorderland())
                        INSTANCE.delChunk(clan, chunk);
                hasBorderlands = false;
                markChanged();
            }
        }
    }
}
