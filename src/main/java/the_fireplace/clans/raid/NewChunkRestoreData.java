package the_fireplace.clans.raid;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.util.BlockSerializeUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NewChunkRestoreData {
	private UUID clan;
	private HashMap<SerialBlockPos, String> replaceBlocks = Maps.newHashMap();
	private ArrayList<SerialBlockPos> removeBlocks = Lists.newArrayList();

	public NewChunkRestoreData(UUID clan) {
		this.clan = clan;
	}

	public void addRestoreBlock(int x, int y, int z, String block) {
		SerialBlockPos pos = new SerialBlockPos(x, y, z);
		if(!removeBlocks.remove(pos))
			replaceBlocks.put(pos, block);
	}

	public void addRemoveBlock(int x, int y, int z) {
		SerialBlockPos pos = new SerialBlockPos(x, y, z);
		if(replaceBlocks.remove(pos) == null)
			removeBlocks.add(pos);
	}

	public String popRestoreBlock(int x, int y, int z) {
		return replaceBlocks.remove(new SerialBlockPos(x, y, z));
	}

	public boolean hasRestoreBlock(int x, int y, int z) {
		return replaceBlocks.containsKey(new SerialBlockPos(x, y, z));
	}

	public boolean delRemoveBlock(int x, int y, int z) {
		return removeBlocks.remove(new SerialBlockPos(x, y, z));
	}

	public void restore(Chunk c) {
		//TODO teleport all players out of the chunk
		for(SerialBlockPos entry: removeBlocks)
			c.getWorld().setBlockToAir(new BlockPos(entry.getX(), entry.getY(), entry.getZ()));
		for(Map.Entry<SerialBlockPos, String> entry: replaceBlocks.entrySet())
			c.getWorld().setBlockState(new BlockPos(entry.getKey().getX(), entry.getKey().getY(), entry.getKey().getZ()), BlockSerializeUtil.blockFromString(entry.getValue()));
	}

	public UUID getClan() {
		return clan;
	}

	public JsonObject toJsonObject() {
		JsonObject ret = new JsonObject();
		ret.addProperty("clan", clan.toString());
		JsonArray replaceBlocksMap = new JsonArray();
		for(Map.Entry<SerialBlockPos, String> entry: replaceBlocks.entrySet()) {
			JsonObject outputEntry = new JsonObject();
			outputEntry.add("key", entry.getKey().toJsonObject());
			outputEntry.addProperty("value", entry.getValue());
			replaceBlocksMap.add(outputEntry);
		}
		ret.add("replaceBlocks", replaceBlocksMap);
		JsonArray removeBlocksList = new JsonArray();
		for(SerialBlockPos pos: removeBlocks)
			removeBlocksList.add(pos.toJsonObject());
		ret.add("removeBlocks", removeBlocksList);

		return ret;
	}

	public NewChunkRestoreData(JsonObject obj){
		this.clan = UUID.fromString(obj.get("clan").getAsString());
		for(JsonElement e: obj.get("replaceBlocks").getAsJsonArray())
			this.replaceBlocks.put(new SerialBlockPos(e.getAsJsonObject().get("key").getAsJsonObject()), e.getAsJsonObject().get("value").getAsString());
		for(JsonElement e: obj.get("removeBlocks").getAsJsonArray())
			this.removeBlocks.add(new SerialBlockPos(e.getAsJsonObject()));
	}
}
