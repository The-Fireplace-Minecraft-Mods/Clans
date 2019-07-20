package the_fireplace.clans.data;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.*;
import the_fireplace.clans.Clans;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.model.ChunkPosition;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.model.ClanDimInfo;

import javax.annotation.Nullable;
import java.io.*;
import java.util.*;

public class ClanChunkData {

    private static boolean isLoaded = false;
    private static boolean isChanged = false;
    private static HashMap<UUID, Set<ChunkPosition>> claimedChunks = Maps.newHashMap();
    private static HashMap<ChunkPosition, UUID> chunkOwners = Maps.newHashMap();

    public static Set<ChunkPosition> getChunks(UUID clan) {
        if(!isLoaded)
            load();
        Set<ChunkPosition> claimed = claimedChunks.get(clan);
        return claimed != null ? Sets.newHashSet(claimed) : Collections.emptySet();
    }

    public static Set<Clan> clansWithClaims() {
        if(!isLoaded)
            load();
        Set<Clan> claimClans = Sets.newHashSet();
        for(UUID clanId: Sets.newHashSet(claimedChunks.keySet())) {
            Clan clan = ClanCache.getClanById(clanId);
            if(clan != null)
                claimClans.add(clan);
            else
                delClan(clanId);
        }
        return claimClans;
    }

    public static void addChunk(Clan clan, ChunkPosition pos) {
        if(!isLoaded)
            load();
        claimedChunks.putIfAbsent(clan.getClanId(), Sets.newHashSet());
        claimedChunks.get(clan.getClanId()).add(pos);
        chunkOwners.put(pos, clan.getClanId());
        Clans.getDynmapCompat().queueClaimEventReceived(new ClanDimInfo(clan.getClanId().toString(), pos.dim, clan.getClanName(), clan.getDescription(), clan.getColor()));
        isChanged = true;
    }

    public static void addChunk(Clan clan, int x, int z, int dim) {
        addChunk(clan, new ChunkPosition(x, z, dim));
    }

    public static void addChunk(UUID clanId, int x, int z, int dim) {
        Clan clan = ClanCache.getClanById(clanId);
        if(clan == null)
            delClan(clanId);
        else
            addChunk(clan, x, z, dim);
    }

    public static void addChunk(@Nullable UUID clanId, ChunkPosition pos) {
        Clan clan = ClanCache.getClanById(clanId);
        if(clan == null)
            delClan(clanId);
        else
            addChunk(clan, pos);
    }

    public static void delChunk(Clan clan, ChunkPosition pos) {
        if(!isLoaded)
            load();
        claimedChunks.putIfAbsent(clan.getClanId(), Sets.newHashSet());
        chunkOwners.remove(pos);
        if(claimedChunks.get(clan.getClanId()).remove(pos)) {
            Clans.getDynmapCompat().queueClaimEventReceived(new ClanDimInfo(clan.getClanId().toString(), pos.dim, clan.getClanName(), clan.getDescription(), clan.getColor()));
            isChanged = true;
        }
    }

    public static void delChunk(Clan clan, int x, int z, int dim) {
        delChunk(clan, new ChunkPosition(x, z, dim));
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
    public static void delChunk(@Nullable UUID clanId, ChunkPosition pos) {
        Clan clan = ClanCache.getClanById(clanId);
        if(clan == null)
            delClan(clanId);
        else
            delChunk(clan, pos);
    }

    /**
     * Delete a claim. If you already have the clan or the clan Id of the owner, use one of the delChunk methods that takes that for efficiency.
     */
    public static void delChunk(ChunkPosition pos) {
        delChunk(getChunkClanId(pos), pos);
    }

    @Nullable
    public static Clan getChunkClan(int x, int z, int dim) {
        return ClanCache.getClanById(getChunkClanId(x, z, dim));
    }

    @Nullable
    public static UUID getChunkClanId(int x, int z, int dim) {
        if(!isLoaded)
            load();
        return chunkOwners.get(new ChunkPosition(x, z, dim));
    }

    @Nullable
    public static UUID getChunkClanId(ChunkPosition position) {
        if(!isLoaded)
            load();
        return chunkOwners.get(position);
    }

    public static boolean delClan(@Nullable UUID clan) {
        if(clan == null)
            return false;
        for(Map.Entry<ChunkPosition, UUID> entry : chunkOwners.entrySet())
            if(entry.getValue().equals(clan))
                chunkOwners.remove(entry.getKey());
        return claimedChunks.remove(clan) != null;
        //TODO: Make sure the deleted clan is removed from Dynmap
    }

    public static void swapChunk(ChunkPosition pos, @Nullable UUID oldOwner, UUID newOwner) {
        delChunk(oldOwner != null ? oldOwner : getChunkClanId(pos), pos);
        addChunk(newOwner, pos);
    }

    private static void load() {
        read(getOldFile(), true);
        isLoaded = true;
    }

    @Deprecated
    private static File getOldFile() {
        return new File(Clans.getMinecraftHelper().getServer().getWorld(0).getSaveHandler().getWorldDirectory(), "chunkclancache.json");
    }

    private static boolean reading;

    private static void read(File file, boolean isLegacy) {
        if(!reading) {
            reading = true;
            JsonParser jsonParser = new JsonParser();
            try {
                Object obj = jsonParser.parse(new FileReader(file));
                if (obj instanceof JsonObject) {
                    JsonObject jsonObject = (JsonObject) obj;
                    JsonArray claimedChunkMap = jsonObject.get("claimedChunks").getAsJsonArray();
                    for (int i = 0; i < claimedChunkMap.size(); i++) {
                        Set<ChunkPosition> positions = Sets.newHashSet();
                        UUID clan = UUID.fromString(claimedChunkMap.get(i).getAsJsonObject().get("key").getAsString());
                        for (JsonElement element : claimedChunkMap.get(i).getAsJsonObject().get("value").getAsJsonArray()) {
                            ChunkPosition pos = new ChunkPosition(element.getAsJsonObject().get("x").getAsInt(), element.getAsJsonObject().get("z").getAsInt(), element.getAsJsonObject().get("d").getAsInt());
                            chunkOwners.put(pos, clan);
                            positions.add(pos);
                        }
                        claimedChunks.put(clan, positions);
                    }
                }
                if(isLegacy)
                    file.delete();
            } catch (FileNotFoundException e) {
                //do nothing, it just hasn't been created yet
            } catch (Exception e) {
                e.printStackTrace();
            }
            isChanged = false;
            reading = false;
        }
    }

    public static void save() {
        write(getOldFile());
    }

    private static void write(File location) {
        if(!isChanged)
            return;
        JsonObject obj = new JsonObject();
        JsonArray claimedChunkMap = new JsonArray();
        for(Map.Entry<UUID, Set<ChunkPosition>> position : Sets.newHashSet(claimedChunks.entrySet())) {
            JsonArray positionArray = new JsonArray();
            for(ChunkPosition pos: position.getValue()) {
                JsonObject chunkPositionObject = new JsonObject();
                chunkPositionObject.addProperty("x", pos.posX);
                chunkPositionObject.addProperty("z", pos.posZ);
                chunkPositionObject.addProperty("d", pos.dim);
                positionArray.add(chunkPositionObject);
            }
            JsonObject entry = new JsonObject();
            entry.addProperty("key", position.getKey().toString());
            entry.add("value", positionArray);
            claimedChunkMap.add(entry);
        }
        obj.add("claimedChunks", claimedChunkMap);
        try {
            FileWriter file = new FileWriter(location);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(obj);
            file.write(json);
            file.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
        isChanged = false;
    }
}
