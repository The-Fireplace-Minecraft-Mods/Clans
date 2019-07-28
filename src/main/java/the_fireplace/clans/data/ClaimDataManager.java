package the_fireplace.clans.data;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.*;
import the_fireplace.clans.Clans;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.model.*;
import the_fireplace.clans.util.JsonHelper;

import javax.annotation.Nullable;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public final class ClaimDataManager {
    private static boolean isLoaded = false;
    private static final File chunkDataLocation = new File(Clans.getMinecraftHelper().getServer().getWorld(0).getSaveHandler().getWorldDirectory(), "clans/chunk");

    //Main storage
    private static HashMap<UUID, ClanClaimData> claimedChunks = Maps.newHashMap();
    //Cache for easy access to data based on chunk position
    private static HashMap<ChunkPositionWithData, ClanClaimData> claimDataMap = Maps.newHashMap();

    public static HashMap<UUID, Integer> regenBorderlandsTimers = Maps.newHashMap();

    public static Set<ChunkPositionWithData> getClaimedChunks(UUID clan) {
        if(!isLoaded)
            load();
        ClanClaimData claimed = claimedChunks.get(clan);
        return claimed != null ? claimed.chunks.stream().filter(d -> !d.isBorderland()).collect(Collectors.toSet()) : Collections.emptySet();
    }

    public static Set<ChunkPositionWithData> getBorderlandChunks(UUID clan) {
        if(!isLoaded)
            load();
        ClanClaimData claimed = claimedChunks.get(clan);
        return claimed != null ? claimed.chunks.stream().filter(ChunkPositionWithData::isBorderland).collect(Collectors.toSet()) : Collections.emptySet();
    }

    public static Set<Clan> clansWithClaims() {
        if(!isLoaded)
            load();
        Set<Clan> claimClans = Sets.newHashSet();
        for(Map.Entry<UUID, ClanClaimData> clanId: Sets.newHashSet(claimedChunks.entrySet())) {
            Clan clan = ClanCache.getClanById(clanId.getKey());
            if(clan != null) {
                if (!clanId.getValue().chunks.isEmpty())
                    claimClans.add(clan);
            } else
                delClan(clanId.getKey());
        }
        return claimClans;
    }

    //region addChunk
    public static void addChunk(Clan clan, ChunkPositionWithData pos) {
        if(!isLoaded)
            load();
        claimedChunks.putIfAbsent(clan.getClanId(), new ClanClaimData(clan.getClanId()));
        claimedChunks.get(clan.getClanId()).addChunk(pos);
        claimDataMap.put(pos, claimedChunks.get(clan.getClanId()));
        Clans.getDynmapCompat().queueClaimEventReceived(new ClanDimInfo(clan.getClanId().toString(), pos.getDim(), clan.getClanName(), clan.getDescription(), clan.getColor()));
        claimedChunks.get(clan.getClanId()).isChanged = true;
        regenBorderlandsTimers.put(clan.getClanId(), 5);
    }

    public static void addChunk(Clan clan, int x, int z, int dim) {
        addChunk(clan, new ChunkPositionWithData(x, z, dim));
    }

    public static void addChunk(UUID clanId, int x, int z, int dim) {
        Clan clan = ClanCache.getClanById(clanId);
        if(clan == null)
            delClan(clanId);
        else
            addChunk(clan, x, z, dim);
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
        claimedChunks.putIfAbsent(clan.getClanId(), new ClanClaimData(clan.getClanId()));
        claimDataMap.remove(pos);
        if(claimedChunks.get(clan.getClanId()).delChunk(pos)) {
            Clans.getDynmapCompat().queueClaimEventReceived(new ClanDimInfo(clan.getClanId().toString(), pos.getDim(), clan.getClanName(), clan.getDescription(), clan.getColor()));
            claimedChunks.get(clan.getClanId()).isChanged = true;
            regenBorderlandsTimers.put(clan.getClanId(), 5);
        }
    }

    public static void delChunk(Clan clan, int x, int z, int dim) {
        delChunk(clan, new ChunkPositionWithData(x, z, dim));
    }

    /**
     * Delete a claim. If you already have the clan, use the delChunk method that takes it for efficiency.
     */
    public static void delChunk(@Nullable UUID clanId, int x, int z, int dim) {
        Clan clan = ClanCache.getClanById(clanId);
        if(clan == null)
            delClan(clanId);
        else
            delChunk(clan, x, z, dim);
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

    /**
     * Delete a claim. If you already have the clan or the clan Id of the owner, use one of the delChunk methods that takes that for efficiency.
     */
    public static void delChunk(ChunkPositionWithData pos) {
        delChunk(getChunkClanId(pos), pos);
    }
    //endregion

    @Nullable
    public static ChunkPositionWithData getChunkPositionData(int x, int z, int d) {
        return getChunkPositionData(new ChunkPosition(x, z, d));
    }

    @Nullable
    public static ChunkPositionWithData getChunkPositionData(ChunkPosition pos) {
        for(ChunkPositionWithData pos2 : claimDataMap.keySet())
            if(pos2.equals(pos))
                return pos2;
        return null;
    }

    @Nullable
    public static Clan getChunkClan(int x, int z, int dim) {
        return ClanCache.getClanById(getChunkClanId(x, z, dim));
    }

    @Nullable
    public static UUID getChunkClanId(int x, int z, int dim) {
        if(!isLoaded)
            load();
        ClanClaimData data = claimDataMap.get(new ChunkPositionWithData(x, z, dim));
        return data != null ? data.clan : null;
    }

    @Nullable
    public static UUID getChunkClanId(ChunkPositionWithData position) {
        if(!isLoaded)
            load();
        ClanClaimData data = claimDataMap.get(position);
        return data != null ? data.clan : null;
    }

    @Nullable
    public static ClanClaimData getChunkClaimData(int x, int z, int dim) {
        if(!isLoaded)
            load();
        return claimDataMap.get(new ChunkPositionWithData(x, z, dim));
    }

    @Nullable
    public static ClanClaimData getChunkClaimData(ChunkPositionWithData position) {
        if(!isLoaded)
            load();
        return claimDataMap.get(position);
    }

    public static boolean delClan(@Nullable UUID clan) {
        if(clan == null)
            return false;
        for(Map.Entry<ChunkPositionWithData, ClanClaimData> entry : claimDataMap.entrySet())
            if(entry.getValue().clan.equals(clan))
                claimDataMap.remove(entry.getKey());
        claimedChunks.get(clan).chunkDataFile.delete();
        return claimedChunks.remove(clan) != null;
        //TODO: Make sure the deleted clan is removed from Dynmap
    }

    public static void swapChunk(ChunkPositionWithData pos, @Nullable UUID oldOwner, UUID newOwner) {
        delChunk(oldOwner != null ? oldOwner : getChunkClanId(pos), pos);
        addChunk(newOwner, pos);
    }

    /**
     * Use a timer so in the future, when mass claiming and abandoning chunks is possible, it isn't regenerating every single time a claim or abandon is done.
     */
    public static void decrementBorderlandsRegenTimers() {
        for(Map.Entry<UUID, Integer> entry: Sets.newHashSet(regenBorderlandsTimers.entrySet())) {
            if(entry.getValue() <= 0) {
                regenBorderlandsTimers.remove(entry.getKey());
                claimedChunks.get(entry.getKey()).regenBorderlands();
            } else {
                regenBorderlandsTimers.put(entry.getKey(), entry.getValue() - 1);
            }
        }
    }

    private static void load() {
        if(!chunkDataLocation.exists())
            chunkDataLocation.mkdirs();
        File[] files = chunkDataLocation.listFiles();
        if(files != null)
            for(File file: files) {
                try {
                    ClanClaimData loadedData = ClanClaimData.load(file);
                    if(loadedData != null) {
                        claimedChunks.put(loadedData.clan, loadedData);
                        for(ChunkPositionWithData cPos : loadedData.chunks)
                            claimDataMap.put(cPos, loadedData);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        readLegacy();
        isLoaded = true;
        for(Map.Entry<UUID, ClanClaimData> entry : claimedChunks.entrySet()) {
            if(Clans.getConfig().isEnableBorderlands() && !entry.getValue().hasBorderlands)
                entry.getValue().genBorderlands();
            else if(!Clans.getConfig().isEnableBorderlands() && entry.getValue().hasBorderlands)
                entry.getValue().clearBorderlands();
        }
    }

    private static boolean reading;

    @Deprecated
    private static void readLegacy() {
        if(!reading) {
            File oldFile = new File(Clans.getMinecraftHelper().getServer().getWorld(0).getSaveHandler().getWorldDirectory(), "chunkclancache.json");
            if(!oldFile.exists())
                return;
            reading = true;
            JsonParser jsonParser = new JsonParser();
            try {
                Object obj = jsonParser.parse(new FileReader(oldFile));
                if (obj instanceof JsonObject) {
                    JsonObject jsonObject = (JsonObject) obj;
                    JsonArray claimedChunkMap = jsonObject.get("claimedChunks").getAsJsonArray();
                    for (int i = 0; i < claimedChunkMap.size(); i++) {
                        Set<ChunkPositionWithData> positions = Sets.newHashSet();
                        UUID clan = UUID.fromString(claimedChunkMap.get(i).getAsJsonObject().get("key").getAsString());
                        ClanClaimData newData = new ClanClaimData(clan);
                        for (JsonElement element : claimedChunkMap.get(i).getAsJsonObject().get("value").getAsJsonArray()) {
                            ChunkPositionWithData pos = new ChunkPositionWithData(element.getAsJsonObject().get("x").getAsInt(), element.getAsJsonObject().get("z").getAsInt(), element.getAsJsonObject().get("d").getAsInt());
                            claimDataMap.put(pos, newData);
                            positions.add(pos);
                        }
                        newData.chunks.addAll(positions);
                        claimedChunks.put(clan, newData);
                    }
                }
            } catch (FileNotFoundException e) {
                //do nothing, it just hasn't been created yet
            } catch (Exception e) {
                e.printStackTrace();
            }
            reading = false;
            oldFile.delete();
        }
    }

    public static void save() {
        for(ClanClaimData data: claimedChunks.values())
            data.save();
    }

    public static class ClanClaimData {
        //region Internal variables
        private File chunkDataFile;
        private boolean isChanged, saving;
        //endregion

        //region Saved variables
        private UUID clan;
        private Set<ChunkPositionWithData> chunks = Sets.newHashSet();
        private boolean hasBorderlands;
        //endregion

        //region Constructor
        private ClanClaimData(UUID clan) {
            chunkDataFile = new File(chunkDataLocation, clan.toString()+".json");
            this.clan = clan;
            isChanged = true;
        }
        //endregion

        public void markChanged() {
            isChanged = true;
        }

        //region load
        @Nullable
        private static ClanClaimData load(File file) {
            JsonParser jsonParser = new JsonParser();
            try {
                Object obj = jsonParser.parse(new FileReader(file));
                if(obj instanceof JsonObject) {
                    JsonObject jsonObject = (JsonObject) obj;
                    UUID clan = UUID.fromString(jsonObject.getAsJsonPrimitive("clan").getAsString());
                    Set<ChunkPositionWithData> positions = Sets.newHashSet();
                    ClanClaimData loadClanClaimData = new ClanClaimData(clan);
                    loadClanClaimData.isChanged = false;
                    for (JsonElement element : jsonObject.get("chunks").getAsJsonArray()) {
                        ChunkPositionWithData pos = new ChunkPositionWithData(element.getAsJsonObject().get("x").getAsInt(), element.getAsJsonObject().get("z").getAsInt(), element.getAsJsonObject().get("d").getAsInt());
                        pos.setBorderland(element.getAsJsonObject().has("isBorderland") && element.getAsJsonObject().get("isBorderland").getAsBoolean());
                        pos.getAddonData().putAll(JsonHelper.getAddonData(element.getAsJsonObject()));
                        claimDataMap.put(pos, loadClanClaimData);
                        positions.add(pos);
                    }
                    loadClanClaimData.chunks.addAll(positions);
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
                for(ChunkPositionWithData pos: chunks) {
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
            }).run();
        }
        //endregion

        public boolean addChunk(ChunkPositionWithData pos) {
            markChanged();
            return chunks.add(pos);
        }

        public boolean delChunk(ChunkPositionWithData pos) {
            markChanged();
            return chunks.remove(pos);
        }

        public Set<ChunkPositionWithData> getChunks() {
            return chunks;
        }

        public void genBorderlands() {
            for(int d : Clans.getMinecraftHelper().getDimensionIds())
                chunks.addAll(new CoordNodeTree(d, clan).forBorderlandRetrieval().getBorderChunks());
            hasBorderlands = true;
            markChanged();
        }

        public void regenBorderlands() {
            clearBorderlands();
            genBorderlands();
            markChanged();
        }

        public void clearBorderlands() {
            chunks.removeIf(ChunkPositionWithData::isBorderland);
            hasBorderlands = false;
            markChanged();
        }
    }
}
