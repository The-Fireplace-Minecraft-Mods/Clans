package the_fireplace.clans.raid;

import com.google.common.collect.Maps;
import com.google.gson.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import the_fireplace.clans.Clans;
import the_fireplace.clans.util.ChunkPosition;
import the_fireplace.clans.util.ChunkUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class NewRaidRestoreDatabase {
    static NewRaidRestoreDatabase instance = null;
    static boolean isChanged = false;

    public static NewRaidRestoreDatabase getInstance() {
        if(instance == null)
            load();
        return instance;
    }

    HashMap<ChunkPosition, NewChunkRestoreData> raidedChunks = Maps.newHashMap();

    public static void addRestoreBlock(int dim, IChunk c, BlockPos pos, String block) {
        addRestoreBlock(dim, c, pos, block, ChunkUtils.getChunkOwner(c));
    }

    public static void addRestoreBlock(int dim, IChunk c, BlockPos pos, String block, UUID chunkOwner) {
        ChunkPosition coords = new ChunkPosition(c.getPos().x, c.getPos().z, dim);
        if(!getInstance().raidedChunks.containsKey(coords))
            getInstance().raidedChunks.put(coords, new NewChunkRestoreData(chunkOwner));
        if(getInstance().raidedChunks.get(coords).hasRestoreBlock(pos.getX(), pos.getY(), pos.getZ()))
            Clans.LOGGER.error("Block restore data being written when it already exists at position (%s, %s, %s). Block being written is: %s.", pos.getX(), pos.getY(), pos.getZ(), block);
        getInstance().raidedChunks.get(coords).addRestoreBlock(pos.getX(), pos.getY(), pos.getZ(), block);
        isChanged = true;
    }

    public static String popRestoreBlock(int dim, IChunk c, BlockPos pos) {
        ChunkPosition coords = new ChunkPosition(c.getPos().x, c.getPos().z, dim);
        if(!getInstance().raidedChunks.containsKey(coords))
            return null;
        String block = getInstance().raidedChunks.get(coords).popRestoreBlock(pos.getX(), pos.getY(), pos.getZ());
        if(block != null)
            isChanged = true;
        return block;
    }

    public static void addRemoveBlock(int dim, IChunk c, BlockPos pos) {
        ChunkPosition coords = new ChunkPosition(c.getPos().x, c.getPos().z, dim);
        if(!getInstance().raidedChunks.containsKey(coords))
            getInstance().raidedChunks.put(coords, new NewChunkRestoreData(ChunkUtils.getChunkOwner(c)));
        getInstance().raidedChunks.get(coords).addRemoveBlock(pos.getX(), pos.getY(), pos.getZ());
        isChanged = true;
    }

    public static boolean delRemoveBlock(int dim, IChunk c, BlockPos pos) {
        ChunkPosition coords = new ChunkPosition(c.getPos().x, c.getPos().z, dim);
        if(!getInstance().raidedChunks.containsKey(coords))
            return false;
        boolean block = getInstance().raidedChunks.get(coords).delRemoveBlock(pos.getX(), pos.getY(), pos.getZ());
        if(block)
            isChanged = true;
        return block;
    }

    public static NewChunkRestoreData popChunkRestoreData(int dim, IChunk c) {
        NewChunkRestoreData d = getInstance().raidedChunks.remove(new ChunkPosition(c.getPos().x, c.getPos().z, dim));
        if(d != null)
            isChanged = true;
        return d;
    }

    private static void load() {
        instance = new NewRaidRestoreDatabase();
        JsonParser jsonParser = new JsonParser();
        try {
            Object obj = jsonParser.parse(new FileReader(new File(ServerLifecycleHooks.getCurrentServer().getWorld(DimensionType.OVERWORLD).getSaveHandler().getWorldDirectory(), "raids.json")));
            if(obj instanceof JsonObject) {
                JsonObject jsonObject = (JsonObject) obj;
                JsonArray clanMap = jsonObject.get("restoreChunks").getAsJsonArray();
                for (int i = 0; i < clanMap.size(); i++)
                    instance.raidedChunks.put(new ChunkPosition(clanMap.get(i).getAsJsonObject().get("key").getAsJsonObject()), new NewChunkRestoreData(clanMap.get(i).getAsJsonObject().get("value").getAsJsonObject()));
            } else
                Clans.LOGGER.warn("Json Raid Restore Database not found! This is normal on your first run of Clans 1.2.0 and above, and when there is nothing to restore.");
        } catch (FileNotFoundException e) {
            //do nothing, it just hasn't been created yet
        } catch (Exception e) {
            e.printStackTrace();
        }
        isChanged = false;
        RaidRestoreDatabase.getInstance();
    }

    public static void save() {
        if(!isChanged)
            return;
        JsonObject obj = new JsonObject();
        JsonArray chunkRestoreMap = new JsonArray();
        for(Map.Entry<ChunkPosition, NewChunkRestoreData> entry: instance.raidedChunks.entrySet()) {
            JsonObject outputEntry = new JsonObject();
            outputEntry.add("key", entry.getKey().toJsonObject());
            outputEntry.add("value", entry.getValue().toJsonObject());
            chunkRestoreMap.add(outputEntry);
        }
        obj.add("restoreChunks", chunkRestoreMap);
        try {
            FileWriter file = new FileWriter(new File(ServerLifecycleHooks.getCurrentServer().getWorld(DimensionType.OVERWORLD).getSaveHandler().getWorldDirectory(), "raids.json"));
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