package dev.the_fireplace.clans.legacy.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.the_fireplace.clans.io.FileToJsonObject;
import dev.the_fireplace.clans.io.JsonWritable;
import dev.the_fireplace.clans.legacy.ClansModContainer;
import dev.the_fireplace.clans.legacy.model.ChunkPosition;
import dev.the_fireplace.clans.legacy.util.BlockSerializeUtil;
import dev.the_fireplace.clans.multithreading.ThreadedSaveHandler;
import dev.the_fireplace.clans.multithreading.ThreadedSaveable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class RaidRestoreDatabase implements ThreadedSaveable, JsonWritable
{
    private static RaidRestoreDatabase instance = null;
    private final ThreadedSaveHandler<RaidRestoreDatabase> saveHandler = ThreadedSaveHandler.create(this);
    private static File raidDataFile;

    public static RaidRestoreDatabase getInstance() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    private final Map<ChunkPosition, ChunkRestoreData> raidedChunks = new ConcurrentHashMap<>();

    public static void addRestoreBlock(int dim, Chunk c, BlockPos pos, String block) {
        ChunkPosition coords = new ChunkPosition(c.x, c.z, dim);
        getInstance().raidedChunks.computeIfAbsent(coords, (unused) -> new ChunkRestoreData());
        if (getInstance().raidedChunks.get(coords).hasRestoreBlock(pos.getX(), pos.getY(), pos.getZ())) {
            ClansModContainer.getMinecraftHelper().getLogger().error("Block restore cache being written when it already exists at position ({}, {}, {}). Block being written is: {}.", pos.getX(), pos.getY(), pos.getZ(), block);
        }
        getInstance().raidedChunks.get(coords).addRestoreBlock(pos.getX(), pos.getY(), pos.getZ(), block);
        getInstance().markChanged();
    }

    @Nullable
    public static String popRestoreBlock(int dim, Chunk c, BlockPos pos) {
        ChunkPosition coords = new ChunkPosition(c.x, c.z, dim);
        if (!getInstance().raidedChunks.containsKey(coords)) {
            return null;
        }
        String block = getInstance().raidedChunks.get(coords).popRestoreBlock(pos.getX(), pos.getY(), pos.getZ());
        if (block != null) {
            getInstance().markChanged();
        }
        return block;
    }

    public static void addRemoveBlock(int dim, Chunk c, BlockPos pos) {
        ChunkPosition coords = new ChunkPosition(c.x, c.z, dim);
        if (!getInstance().raidedChunks.containsKey(coords)) {
            getInstance().raidedChunks.put(coords, new ChunkRestoreData());
        }
        getInstance().raidedChunks.get(coords).addRemoveBlock(pos.getX(), pos.getY(), pos.getZ(), BlockSerializeUtil.blockToString(c.getWorld().getBlockState(pos)));
        getInstance().markChanged();
    }

    public static boolean delRemoveBlock(int dim, Chunk c, BlockPos pos) {
        ChunkPosition coords = new ChunkPosition(c.x, c.z, dim);
        if (!getInstance().raidedChunks.containsKey(coords)) {
            return false;
        }
        boolean block = getInstance().raidedChunks.get(coords).delRemoveBlock(pos.getX(), pos.getY(), pos.getZ());
        if (block) {
            getInstance().markChanged();
        }
        return block;
    }

    @Nullable
    public static ChunkRestoreData popChunkRestoreData(int dim, Chunk c) {
        ChunkRestoreData d = getInstance().raidedChunks.remove(new ChunkPosition(c.x, c.z, dim));
        if (d != null) {
            getInstance().markChanged();
        }
        return d;
    }

    private static void load() {
        instance = new RaidRestoreDatabase();
        raidDataFile = new File(ClansModContainer.getMinecraftHelper().getServer().getWorld(0).getSaveHandler().getWorldDirectory(), "raids.json");
        JsonObject jsonObject = FileToJsonObject.readJsonFile(raidDataFile);
        if (jsonObject == null) {
            return;
        }
        JsonArray clanMap = jsonObject.get("restoreChunks").getAsJsonArray();
        for (int i = 0; i < clanMap.size(); i++) {
            instance.raidedChunks.put(new ChunkPosition(clanMap.get(i).getAsJsonObject().get("key").getAsJsonObject()), new ChunkRestoreData(clanMap.get(i).getAsJsonObject().get("value").getAsJsonObject()));
        }
    }

    @Override
    public void blockingSave() {
        writeToJson(raidDataFile);
    }

    @Override
    public ThreadedSaveHandler<?> getSaveHandler() {
        return saveHandler;
    }

    @Override
    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        JsonArray chunkRestoreMap = new JsonArray();
        for (Map.Entry<ChunkPosition, ChunkRestoreData> entry : raidedChunks.entrySet()) {
            JsonObject outputEntry = new JsonObject();
            outputEntry.add("key", entry.getKey().toJsonObject());
            outputEntry.add("value", entry.getValue().toJsonObject());
            chunkRestoreMap.add(outputEntry);
        }
        obj.add("restoreChunks", chunkRestoreMap);
        return obj;
    }
}
