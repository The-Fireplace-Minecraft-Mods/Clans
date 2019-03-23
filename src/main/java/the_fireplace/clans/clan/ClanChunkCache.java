package the_fireplace.clans.clan;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraftforge.fml.common.FMLCommonHandler;
import the_fireplace.clans.Clans;
import the_fireplace.clans.compat.dynmap.data.ClanDimInfo;
import the_fireplace.clans.util.ChunkPosition;

import java.io.*;
import java.util.*;

public class ClanChunkCache {

    private static boolean isLoaded = false;
    private static boolean isChanged = false;
    private static HashMap<UUID, Set<ChunkPosition>> claimedChunks = Maps.newHashMap();

    public static Set<ChunkPosition> getChunks(UUID clan) {
        if(!isLoaded)
            load();
        Set<ChunkPosition> claimed = claimedChunks.get(clan);
        return claimed != null ? claimed : Collections.emptySet();
    }

    public static Set<Clan> clansWithClaims() {
        if(!isLoaded)
            load();
        Set<Clan> claimClans = Sets.newHashSet();
        for(UUID clanId: claimedChunks.keySet())
            claimClans.add(ClanCache.getClanById(clanId));
        return claimClans;
    }

    public static void addChunk(Clan clan, int x, int z, int dim) {
        if(!isLoaded)
            load();
        claimedChunks.putIfAbsent(clan.getClanId(), Sets.newHashSet());
        claimedChunks.get(clan.getClanId()).add(new ChunkPosition(x, z, dim));
        Clans.getDynmapCompat().queueClaimEventReceived(new ClanDimInfo(clan.getClanId().toString(), dim, clan.getClanName(), clan.getDescription()));
        isChanged = true;
    }

    public static void delChunk(Clan clan, int x, int z, int dim) {
        if(!isLoaded)
            load();
        claimedChunks.putIfAbsent(clan.getClanId(), Sets.newHashSet());
        if(claimedChunks.get(clan.getClanId()).remove(new ChunkPosition(x, z, dim)))
            isChanged = true;
    }

    private static void load() {
        read(getFile());
        isLoaded = true;
    }

    private static File getFile() {
        return new File(FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0).getSaveHandler().getWorldDirectory(), "chunkclancache.json");
    }

    private static void read(File file) {
        JsonParser jsonParser = new JsonParser();
        try {
            Object obj = jsonParser.parse(new FileReader(file));
            if(obj instanceof JsonObject) {
                JsonObject jsonObject = (JsonObject) obj;
                JsonArray keyArray = jsonObject.get("claimedChunksKeys").getAsJsonArray();
                JsonArray valueArray = jsonObject.get("claimedChunksValues").getAsJsonArray();
                for (int i = 0; i < keyArray.size(); i++) {
                    Set<ChunkPosition> positions = Sets.newHashSet();
                    for (JsonElement element : valueArray.get(i).getAsJsonArray())
                        positions.add(new ChunkPosition(element.getAsJsonObject().get("x").getAsInt(), element.getAsJsonObject().get("z").getAsInt(), element.getAsJsonObject().get("d").getAsInt()));
                    claimedChunks.put(UUID.fromString(keyArray.get(i).getAsString()), positions);
                }
            }
        } catch (FileNotFoundException e) {
            //do nothing, it just hasn't been created yet
        } catch (Exception e) {
            e.printStackTrace();
        }
        isChanged = false;
    }

    public static void save() {
        write(getFile());
    }

    private static void write(File location) {
        if(!isChanged)
            return;
        JsonObject obj = new JsonObject();
        JsonArray keyArray = new JsonArray();
        JsonArray valueArray = new JsonArray();
        for(Map.Entry<UUID, Set<ChunkPosition>> position : claimedChunks.entrySet()) {
            keyArray.add(position.getKey().toString());
            JsonArray positionArray = new JsonArray();
            for(ChunkPosition pos: position.getValue()) {
                JsonObject chunkPositionObject = new JsonObject();
                chunkPositionObject.addProperty("x", pos.posX);
                chunkPositionObject.addProperty("z", pos.posZ);
                chunkPositionObject.addProperty("d", pos.dim);
                positionArray.add(chunkPositionObject);
            }
            valueArray.add(positionArray);
        }
        obj.add("claimedChunksKeys", keyArray);
        obj.add("claimedChunksValues", valueArray);
        try {
            FileWriter file = new FileWriter(location);
            String str = obj.toString();
            file.write(str);
        } catch(IOException e) {
            e.printStackTrace();
        }
        isChanged = false;
    }
}
