package the_fireplace.clans.data;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.*;
import the_fireplace.clans.Clans;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.model.ChunkPositionWithData;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.model.ClanDimInfo;
import the_fireplace.clans.util.JsonHelper;

import javax.annotation.Nullable;
import java.io.*;
import java.util.*;

public final class ClanChunkData {
    private static boolean isLoaded = false;
    private static final File chunkDataLocation = new File(Clans.getMinecraftHelper().getServer().getWorld(0).getSaveHandler().getWorldDirectory(), "clans/chunk");

    private static HashMap<UUID, ChunkData> claimedChunks = Maps.newHashMap();
    private static HashMap<ChunkPositionWithData, UUID> chunkOwners = Maps.newHashMap();

    public static Set<ChunkPositionWithData> getChunks(UUID clan) {
        if(!isLoaded)
            load();
        ChunkData claimed = claimedChunks.get(clan);
        return claimed != null ? claimed.chunks : Collections.emptySet();
    }

    public static Set<Clan> clansWithClaims() {
        if(!isLoaded)
            load();
        Set<Clan> claimClans = Sets.newHashSet();
        for(Map.Entry<UUID, ChunkData> clanId: Sets.newHashSet(claimedChunks.entrySet())) {
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
        claimedChunks.putIfAbsent(clan.getClanId(), new ChunkData(clan.getClanId()));
        claimedChunks.get(clan.getClanId()).chunks.add(pos);
        chunkOwners.put(pos, clan.getClanId());
        Clans.getDynmapCompat().queueClaimEventReceived(new ClanDimInfo(clan.getClanId().toString(), pos.dim, clan.getClanName(), clan.getDescription(), clan.getColor()));
        claimedChunks.get(clan.getClanId()).isChanged = true;
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
        claimedChunks.putIfAbsent(clan.getClanId(), new ChunkData(clan.getClanId()));
        chunkOwners.remove(pos);
        if(claimedChunks.get(clan.getClanId()).chunks.remove(pos)) {
            Clans.getDynmapCompat().queueClaimEventReceived(new ClanDimInfo(clan.getClanId().toString(), pos.dim, clan.getClanName(), clan.getDescription(), clan.getColor()));
            claimedChunks.get(clan.getClanId()).isChanged = true;
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
    public static Clan getChunkClan(int x, int z, int dim) {
        return ClanCache.getClanById(getChunkClanId(x, z, dim));
    }

    @Nullable
    public static UUID getChunkClanId(int x, int z, int dim) {
        if(!isLoaded)
            load();
        return chunkOwners.get(new ChunkPositionWithData(x, z, dim));
    }

    @Nullable
    public static UUID getChunkClanId(ChunkPositionWithData position) {
        if(!isLoaded)
            load();
        return chunkOwners.get(position);
    }

    public static boolean delClan(@Nullable UUID clan) {
        if(clan == null)
            return false;
        for(Map.Entry<ChunkPositionWithData, UUID> entry : chunkOwners.entrySet())
            if(entry.getValue().equals(clan))
                chunkOwners.remove(entry.getKey());
        claimedChunks.get(clan).chunkDataFile.delete();
        return claimedChunks.remove(clan) != null;
        //TODO: Make sure the deleted clan is removed from Dynmap
    }

    public static void swapChunk(ChunkPositionWithData pos, @Nullable UUID oldOwner, UUID newOwner) {
        delChunk(oldOwner != null ? oldOwner : getChunkClanId(pos), pos);
        addChunk(newOwner, pos);
    }

    private static void load() {
        if(!chunkDataLocation.exists())
            chunkDataLocation.mkdirs();
        for(File file: chunkDataLocation.listFiles()) {
            try {
                ChunkData loadedData = ChunkData.load(file);
                if(loadedData != null) {
                    claimedChunks.put(loadedData.clan, loadedData);
                    for(ChunkPositionWithData cPos : loadedData.chunks)
                        chunkOwners.put(cPos, loadedData.clan);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        readLegacy();
        isLoaded = true;
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
                        for (JsonElement element : claimedChunkMap.get(i).getAsJsonObject().get("value").getAsJsonArray()) {
                            ChunkPositionWithData pos = new ChunkPositionWithData(element.getAsJsonObject().get("x").getAsInt(), element.getAsJsonObject().get("z").getAsInt(), element.getAsJsonObject().get("d").getAsInt());
                            chunkOwners.put(pos, clan);
                            positions.add(pos);
                        }
                        ChunkData newData = new ChunkData(clan);
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
        for(ChunkData data: claimedChunks.values())
            data.save();
    }

    private static class ChunkData {
        //region Internal variables
        private File chunkDataFile;
        private boolean isChanged, saving;
        //endregion

        //region Saved variables
        private UUID clan;
        private Set<ChunkPositionWithData> chunks = Sets.newHashSet();
        //endregion

        //region Constructor
        private ChunkData(UUID clan) {
            chunkDataFile = new File(chunkDataLocation, clan.toString()+".json");
            this.clan = clan;
        }
        //endregion

        //region load
        @Nullable
        private static ChunkData load(File file) {
            JsonParser jsonParser = new JsonParser();
            try {
                Object obj = jsonParser.parse(new FileReader(file));
                if(obj instanceof JsonObject) {
                    JsonObject jsonObject = (JsonObject) obj;
                    UUID clan = UUID.fromString(jsonObject.getAsJsonPrimitive("clan").getAsString());
                    Set<ChunkPositionWithData> positions = Sets.newHashSet();
                    for (JsonElement element : jsonObject.get("chunks").getAsJsonArray()) {
                        ChunkPositionWithData pos = new ChunkPositionWithData(element.getAsJsonObject().get("x").getAsInt(), element.getAsJsonObject().get("z").getAsInt(), element.getAsJsonObject().get("d").getAsInt());
                        pos.getAddonData().putAll(JsonHelper.getAddonData(element.getAsJsonObject()));
                        chunkOwners.put(pos, clan);
                        positions.add(pos);
                    }
                    ChunkData loadChunkData = new ChunkData(clan);
                    loadChunkData.chunks.addAll(positions);
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
            new Thread(() -> {
                JsonObject obj = new JsonObject();
                obj.addProperty("clan", clan.toString());
                JsonArray positionArray = new JsonArray();
                for(ChunkPositionWithData pos: chunks) {
                    JsonObject chunkPositionObject = new JsonObject();
                    chunkPositionObject.addProperty("x", pos.posX);
                    chunkPositionObject.addProperty("z", pos.posZ);
                    chunkPositionObject.addProperty("d", pos.dim);
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
    }
}
