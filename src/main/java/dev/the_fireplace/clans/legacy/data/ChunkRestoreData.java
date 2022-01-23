package dev.the_fireplace.clans.legacy.data;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.the_fireplace.clans.legacy.model.ChunkPosition;
import dev.the_fireplace.clans.legacy.util.BlockSerializeUtil;
import dev.the_fireplace.clans.legacy.util.EntityUtil;
import dev.the_fireplace.clans.legacy.util.JsonHelper;
import io.netty.util.internal.ConcurrentSet;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class ChunkRestoreData
{
    private final Map<BlockPos, String> replaceBlocks = new ConcurrentHashMap<>();
    private final Set<BlockPos> removeBlocks = new ConcurrentSet<>();

    public ChunkRestoreData() {
    }

    public void addRestoreBlock(int x, int y, int z, String block) {
        BlockPos pos = new BlockPos(x, y, z);
        if (!removeBlocks.remove(pos)) {
            replaceBlocks.put(pos, block);
        }
    }

    public void addRemoveBlock(int x, int y, int z, String block) {
        BlockPos pos = new BlockPos(x, y, z);
        if (!replaceBlocks.remove(pos, block)) {
            removeBlocks.add(pos);
        }
    }

    public String popRestoreBlock(int x, int y, int z) {
        return replaceBlocks.remove(new BlockPos(x, y, z));
    }

    public boolean hasRestoreBlock(int x, int y, int z) {
        return replaceBlocks.containsKey(new BlockPos(x, y, z));
    }

    public boolean delRemoveBlock(int x, int y, int z) {
        return removeBlocks.remove(new BlockPos(x, y, z));
    }

    public void restore(Chunk c) {
        removePlayers(c);
        removePrimedTNT(c);
        removePlacedBlocks(c);
        placeRemovedBlocks(c);
    }

    private void placeRemovedBlocks(Chunk c) {
        for (Map.Entry<BlockPos, String> entry : replaceBlocks.entrySet()) {
            c.getWorld().setBlockState(new BlockPos(entry.getKey().getX(), entry.getKey().getY(), entry.getKey().getZ()), BlockSerializeUtil.blockFromString(entry.getValue()));
        }
    }

    private void removePlacedBlocks(Chunk c) {
        for (BlockPos entry : removeBlocks) {
            c.getWorld().setBlockToAir(new BlockPos(entry.getX(), entry.getY(), entry.getZ()));
        }
    }

    private void removePrimedTNT(Chunk c) {
        List<EntityTNTPrimed> tnts = Lists.newArrayList();
        c.getEntitiesOfTypeWithinAABB(EntityTNTPrimed.class,
            new AxisAlignedBB(new BlockPos(c.getPos().getXStart(), 0, c.getPos().getZStart()), new BlockPos(c.getPos().getXEnd(), (c.getTopFilledSegment() + 1) * 16, c.getPos().getZEnd())),
            tnts, p -> true);
        for (EntityTNTPrimed tnt : tnts) {
            c.getWorld().removeEntity(tnt);
        }
    }

    private void removePlayers(Chunk c) {
        List<ServerPlayerEntity> players = Lists.newArrayList();
        c.getEntitiesOfTypeWithinAABB(ServerPlayerEntity.class,
            new AxisAlignedBB(new BlockPos(c.getPos().getXStart(), 0, c.getPos().getZStart()), new BlockPos(c.getPos().getXEnd(), (c.getTopFilledSegment() + 1) * 16, c.getPos().getZEnd())),
            players, p -> true);
        //Teleport players out of the chunk before restoring the data, to help prevent them from suffocating
        for (ServerPlayerEntity p : players) {
            EntityUtil.teleportSafelyToChunk(p, EntityUtil.findSafeChunkFor(p, new ChunkPosition(c), true));
        }
    }

    public JsonObject toJsonObject() {
        JsonObject ret = new JsonObject();
        JsonArray replaceBlocksMap = new JsonArray();
        for (Map.Entry<BlockPos, String> entry : replaceBlocks.entrySet()) {
            JsonObject outputEntry = new JsonObject();
            outputEntry.add("key", JsonHelper.toJsonObject(entry.getKey()));
            outputEntry.addProperty("value", entry.getValue());
            replaceBlocksMap.add(outputEntry);
        }
        ret.add("replaceBlocks", replaceBlocksMap);
        JsonArray removeBlocksList = new JsonArray();
        for (BlockPos pos : removeBlocks) {
            removeBlocksList.add(JsonHelper.toJsonObject(pos));
        }
        ret.add("removeBlocks", removeBlocksList);

        return ret;
    }

    public ChunkRestoreData(JsonObject obj) {
        for (JsonElement e : obj.get("replaceBlocks").getAsJsonArray()) {
            this.replaceBlocks.put(JsonHelper.fromJsonObject(e.getAsJsonObject().get("key").getAsJsonObject()), e.getAsJsonObject().get("value").getAsString());
        }
        for (JsonElement e : obj.get("removeBlocks").getAsJsonArray()) {
            this.removeBlocks.add(JsonHelper.fromJsonObject(e.getAsJsonObject()));
        }
    }
}
