package the_fireplace.clans.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.model.ChunkPosition;
import the_fireplace.clans.util.BlockSerializeUtil;
import the_fireplace.clans.util.EntityUtil;
import the_fireplace.clans.util.JsonHelper;

import java.util.List;
import java.util.Map;

public final class ChunkRestoreData {
	private Map<BlockPos, String> replaceBlocks = Maps.newHashMap();
	private List<BlockPos> removeBlocks = Lists.newArrayList();

	public ChunkRestoreData(){}

	public void addRestoreBlock(int x, int y, int z, String block) {
		BlockPos pos = new BlockPos(x, y, z);
		if(!removeBlocks.remove(pos))
			replaceBlocks.put(pos, block);
	}

	public void addRemoveBlock(int x, int y, int z, String block) {
		BlockPos pos = new BlockPos(x, y, z);
		if(!replaceBlocks.remove(pos, block))
			removeBlocks.add(pos);
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
		List<EntityPlayerMP> players = Lists.newArrayList();
		c.getEntitiesOfTypeWithinAABB(EntityPlayerMP.class,
			new AxisAlignedBB(new BlockPos(c.getPos().getXStart(), 0, c.getPos().getZStart()), new BlockPos(c.getPos().getXEnd(), (c.getTopFilledSegment()+1) * 16, c.getPos().getZEnd())),
			players, p -> true);
		//Teleport players out of the chunk before restoring the data, to help prevent them from suffocating
		for(EntityPlayerMP p: players)
			EntityUtil.teleportSafelyToChunk(p, EntityUtil.findSafeChunkFor(p, new ChunkPosition(c), true));
		List<EntityTNTPrimed> tnts = Lists.newArrayList();
		c.getEntitiesOfTypeWithinAABB(EntityTNTPrimed.class,
			new AxisAlignedBB(new BlockPos(c.getPos().getXStart(), 0, c.getPos().getZStart()), new BlockPos(c.getPos().getXEnd(), (c.getTopFilledSegment()+1) * 16, c.getPos().getZEnd())),
			tnts, p -> true);
		for(EntityTNTPrimed tnt: tnts)
			c.getWorld().removeEntity(tnt);
		for(BlockPos entry: removeBlocks)
			c.getWorld().setBlockToAir(new BlockPos(entry.getX(), entry.getY(), entry.getZ()));
		for(Map.Entry<BlockPos, String> entry: replaceBlocks.entrySet())
			c.getWorld().setBlockState(new BlockPos(entry.getKey().getX(), entry.getKey().getY(), entry.getKey().getZ()), BlockSerializeUtil.blockFromString(entry.getValue()));
	}

	public JsonObject toJsonObject() {
		JsonObject ret = new JsonObject();
		JsonArray replaceBlocksMap = new JsonArray();
		for(Map.Entry<BlockPos, String> entry: replaceBlocks.entrySet()) {
			JsonObject outputEntry = new JsonObject();
			outputEntry.add("key", JsonHelper.toJsonObject(entry.getKey()));
			outputEntry.addProperty("value", entry.getValue());
			replaceBlocksMap.add(outputEntry);
		}
		ret.add("replaceBlocks", replaceBlocksMap);
		JsonArray removeBlocksList = new JsonArray();
		for(BlockPos pos: removeBlocks)
			removeBlocksList.add(JsonHelper.toJsonObject(pos));
		ret.add("removeBlocks", removeBlocksList);

		return ret;
	}

	public ChunkRestoreData(JsonObject obj){
		for(JsonElement e: obj.get("replaceBlocks").getAsJsonArray())
			this.replaceBlocks.put(JsonHelper.fromJsonObject(e.getAsJsonObject().get("key").getAsJsonObject()), e.getAsJsonObject().get("value").getAsString());
		for(JsonElement e: obj.get("removeBlocks").getAsJsonArray())
			this.removeBlocks.add(JsonHelper.fromJsonObject(e.getAsJsonObject()));
	}
}
