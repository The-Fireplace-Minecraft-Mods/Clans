package the_fireplace.clans.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.*;
import net.minecraft.entity.Entity;
import the_fireplace.clans.ClansHelper;
import the_fireplace.clans.Clans;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.model.*;
import the_fireplace.clans.util.JsonHelper;

import javax.annotation.Nullable;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public final class ClaimData {
    private static boolean isLoaded = false;
    private static final File chunkDataLocation = new File(Clans.getMinecraftHelper().getServer().getWorld(0).getSaveHandler().getWorldDirectory(), "clans/chunk");

    //Main storage
    private static Map<UUID, ClaimStoredData> claimedChunks;
    //Cache for easy access to data based on chunk position
    private static Map<ChunkPositionWithData, ClaimStoredData> claimDataMap;

    public static Map<UUID, Integer> regenBordersTimer;

    public static Set<ChunkPositionWithData> getClaimedChunks(UUID clan) {
        if(!isLoaded)
            load();
        ClaimStoredData claimed = claimedChunks.get(clan);
        return Collections.unmodifiableSet(claimed != null ? claimed.getChunks().stream().filter(d -> !d.isBorderland()).collect(Collectors.toSet()) : Collections.emptySet());
    }

    public static Set<ChunkPositionWithData> getBorderlandChunks(UUID clan) {
        if(!isLoaded)
            load();
        ClaimStoredData claimed = claimedChunks.get(clan);
        return Collections.unmodifiableSet(claimed != null ? claimed.getChunks().stream().filter(ChunkPositionWithData::isBorderland).collect(Collectors.toSet()) : Collections.emptySet());
    }

    public static Set<Clan> clansWithClaims() {
        if(!isLoaded)
            load();
        Set<Clan> claimClans = Sets.newHashSet();
        for(Map.Entry<UUID, ClaimStoredData> clanId: Sets.newHashSet(claimedChunks.entrySet())) {
            Clan clan = ClanCache.getClanById(clanId.getKey());
            if(clan != null) {
                if (!clanId.getValue().getChunks().isEmpty())
                    claimClans.add(clan);
            } else
                delClan(clanId.getKey());
        }
        return Collections.unmodifiableSet(claimClans);
    }

    //region addChunk
    public static void addChunk(Clan clan, ChunkPositionWithData pos) {
        if(!isLoaded)
            load();
        if(!claimedChunks.containsKey(clan.getId()))
            claimedChunks.put(clan.getId(), new ClaimStoredData(clan.getId()));
        claimedChunks.get(clan.getId()).addChunk(pos);
        claimDataMap.put(pos, claimedChunks.get(clan.getId()));
        if(!pos.isBorderland())
            regenBordersTimer.put(clan.getId(), 5);
        claimedChunks.get(clan.getId()).isChanged = true;
    }

    public static void addChunk(@Nullable UUID clanId, ChunkPositionWithData pos) {
        Clan clan = ClanCache.getClanById(clanId);
        if(clan == null)
            delClan(clanId);
        else
            addChunk(clan, pos);
    }
    //endregion

    //region delChunk
    public static void delChunk(Clan clan, ChunkPositionWithData pos) {
        if(!isLoaded)
            load();
        if(!claimedChunks.containsKey(clan.getId()))
            claimedChunks.put(clan.getId(), new ClaimStoredData(clan.getId()));
        claimDataMap.remove(pos);
        if(claimedChunks.get(clan.getId()).delChunk(pos) && !pos.isBorderland())
            regenBordersTimer.put(clan.getId(), 5);
    }

    /**
     * Delete a claim. If you already have the clan, use the delChunk method that takes it for efficiency.
     */
    public static void delChunk(@Nullable UUID clanId, ChunkPositionWithData pos) {
        Clan clan = ClanCache.getClanById(clanId);
        if(clan == null)
            delClan(clanId);
        else
            delChunk(clan, pos);
    }
    //endregion

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
        if(!isLoaded)
            load();
        return claimDataMap.keySet().stream().filter(p -> p.equals(pos)).findFirst().orElse(null);
    }

    @Nullable
    public static Clan getChunkClan(@Nullable ChunkPositionWithData pos) {
        return ClanCache.getClanById(getChunkClanId(pos));
    }

    @Nullable
    public static Clan getChunkClan(int x, int z, int dim) {
        return ClanCache.getClanById(getChunkClanId(x, z, dim));
    }

    @Nullable
    public static Clan getChunkClan(Entity entity) {
        return ClanCache.getClanById(getChunkClanId(entity.chunkCoordX, entity.chunkCoordZ, entity.dimension));
    }

    @Nullable
    public static UUID getChunkClanId(int x, int z, int dim) {
        if(!isLoaded)
            load();
        ClaimStoredData data = claimDataMap.get(new ChunkPositionWithData(x, z, dim));
        return data != null ? data.clan : null;
    }

    @Nullable
    public static UUID getChunkClanId(@Nullable ChunkPosition position) {
        if(!isLoaded)
            load();
        if(position == null)
            return null;
        if(!(position instanceof ChunkPositionWithData))
            position = new ChunkPositionWithData(position);
        ClaimStoredData data = claimDataMap.get(position);
        return data != null ? data.clan : null;
    }

    @Nullable
    public static ClaimStoredData getChunkClaimData(int x, int z, int dim) {
        if(!isLoaded)
            load();
        return claimDataMap.get(new ChunkPositionWithData(x, z, dim));
    }

    @Nullable
    public static ClaimStoredData getChunkClaimData(ChunkPositionWithData position) {
        if(!isLoaded)
            load();
        return claimDataMap.get(position);
    }

    public static boolean delClan(@Nullable UUID clan) {
        if(clan == null)
            return false;
        for(Map.Entry<ChunkPositionWithData, ClaimStoredData> entry : Sets.newHashSet(claimDataMap.entrySet()))
            if(entry.getValue().clan.equals(clan))
                claimDataMap.remove(entry.getKey());
        if(claimedChunks.containsKey(clan))
            claimedChunks.get(clan).chunkDataFile.delete();
        return claimedChunks.remove(clan) != null;
    }

    public static void swapChunk(ChunkPositionWithData pos, @Nullable UUID oldOwner, UUID newOwner) {
        delChunk(oldOwner != null ? oldOwner : getChunkClanId(pos), pos);
        //Create a new ChunkPositionWithData because the old one has addon data and borderland data attached
        addChunk(newOwner, new ChunkPositionWithData(pos));
    }

    public static Set<ChunkPositionWithData> getAllClaimedChunks() {
        return Collections.unmodifiableSet(claimDataMap.keySet());
    }

    /**
     * Use a timer so in the future, when mass claiming and abandoning chunks is possible, it isn't regenerating every single time a claim or abandon is done.
     */
    public static void decrementBorderlandsRegenTimers() {
        if(!isLoaded)
            load();
        if(ClansHelper.getConfig().isEnableBorderlands())
            for(Map.Entry<UUID, Integer> entry: Sets.newHashSet(regenBordersTimer.entrySet())) {
                if(entry.getValue() <= 0) {
                    regenBordersTimer.remove(entry.getKey());
                    Clan target = ClanCache.getClanById(entry.getKey());
                    if(target == null)//Skip if clan has disbanded
                        continue;
                    claimedChunks.get(entry.getKey()).regenBorderlands();
                    for(int dim: getClaimDims(entry.getKey()))
                        Clans.getDynmapCompat().queueClaimEventReceived(new ClanDimInfo(target, dim));
                } else {
                    regenBordersTimer.put(entry.getKey(), entry.getValue() - 1);
                }
            }
    }

    private static void load() {
        if(!chunkDataLocation.exists())
            chunkDataLocation.mkdirs();
        claimedChunks = Maps.newHashMap();
        claimDataMap = Maps.newConcurrentMap();
        regenBordersTimer = Maps.newHashMap();
        File[] files = chunkDataLocation.listFiles();
        if(files != null)
            for(File file: files) {
                try {
                    ClaimStoredData loadedData = ClaimStoredData.load(file);
                    if(loadedData != null) {
                        claimedChunks.put(loadedData.clan, loadedData);
                        for(ChunkPositionWithData cPos : loadedData.getChunks())
                            claimDataMap.put(cPos, loadedData);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        isLoaded = true;
        for(Map.Entry<UUID, ClaimStoredData> entry : claimedChunks.entrySet()) {
            if(ClansHelper.getConfig().isEnableBorderlands() && !entry.getValue().hasBorderlands)
                entry.getValue().genBorderlands();
            else if(!ClansHelper.getConfig().isEnableBorderlands() && entry.getValue().hasBorderlands)
                entry.getValue().clearBorderlands();
        }
    }

    public static void save() {
        for(ClaimStoredData data: claimedChunks.values())
            data.save();
    }

    public static class ClaimStoredData {
        //region Internal variables
        private File chunkDataFile;
        private boolean isChanged, saving;
        //endregion

        //region Saved variables
        private UUID clan;
        private Set<ChunkPositionWithData> chunks;
        private boolean hasBorderlands;
        //endregion

        //region Constructor
        private ClaimStoredData(UUID clan) {
            chunkDataFile = new File(chunkDataLocation, clan.toString()+".json");
            this.clan = clan;
            this.chunks = Sets.newHashSet();
            markChanged();
        }
        //endregion

        public void markChanged() {
            isChanged = true;
        }

        //region load
        @Nullable
        private static ClaimStoredData load(File file) {
            JsonParser jsonParser = new JsonParser();
            try {
                Object obj = jsonParser.parse(new FileReader(file));
                if(obj instanceof JsonObject) {
                    JsonObject jsonObject = (JsonObject) obj;
                    UUID clan = UUID.fromString(jsonObject.getAsJsonPrimitive("clan").getAsString());
                    Set<ChunkPositionWithData> positions = Sets.newHashSet();
                    ClaimStoredData loadClaimStoredData = new ClaimStoredData(clan);
                    loadClaimStoredData.isChanged = false;
                    for (JsonElement element : jsonObject.get("chunks").getAsJsonArray()) {
                        ChunkPositionWithData pos = new ChunkPositionWithData(element.getAsJsonObject().get("x").getAsInt(), element.getAsJsonObject().get("z").getAsInt(), element.getAsJsonObject().get("d").getAsInt());
                        pos.setBorderland(element.getAsJsonObject().has("isBorderland") && element.getAsJsonObject().get("isBorderland").getAsBoolean());
                        pos.getAddonData().putAll(JsonHelper.getAddonData(element.getAsJsonObject()));
                        claimDataMap.put(pos, loadClaimStoredData);
                        positions.add(pos);
                    }
                    loadClaimStoredData.chunks = Sets.newHashSet(positions);
                    return loadClaimStoredData;
                }
            } catch (FileNotFoundException e) {
                //do nothing, it just hasn't been created yet
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        //endregion

        //region save
        private void save() {
            if(!isChanged || saving)
                return;
            saving = true;
            //TODO check if this completes when the server is shutting down
            new Thread(() -> {
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

                try {
                    FileWriter file = new FileWriter(chunkDataFile);
                    file.write(new GsonBuilder().setPrettyPrinting().create().toJson(obj));
                    file.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                saving = isChanged = false;
            }).start();
        }
        //endregion

        public boolean addChunk(ChunkPositionWithData pos) {
            markChanged();
            return chunks.add(pos);
        }

        public boolean addAllChunks(Collection<? extends ChunkPositionWithData> positions) {
            markChanged();
            return chunks.addAll(positions);
        }

        public boolean delChunk(ChunkPositionWithData pos) {
            markChanged();
            return chunks.remove(pos);
        }

        public Set<ChunkPositionWithData> getChunks() {
            return Collections.unmodifiableSet(Sets.newHashSet(chunks));
        }

        public void genBorderlands() {
            if(ClansHelper.getConfig().isEnableBorderlands()) {
                for (int d : Clans.getMinecraftHelper().getDimensionIds())
                    for (ChunkPositionWithData pos : new CoordNodeTree(d, clan).forBorderlandRetrieval().getBorderChunks().stream().filter(pos -> !getChunks().contains(pos) && !ClaimData.getAllClaimedChunks().contains(pos)).collect(Collectors.toSet()))
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
