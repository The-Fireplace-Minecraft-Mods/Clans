package the_fireplace.clans.raid;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.util.BlockSerializeUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChunkRestoreData implements Serializable {
	private static final long serialVersionUID = 0xEA7F00D;

	private UUID clan;
	private HashMap<SerialBlockPos, String> replaceBlocks = Maps.newHashMap();
	private ArrayList<SerialBlockPos> removeBlocks = Lists.newArrayList();

	public ChunkRestoreData(UUID clan) {
		this.clan = clan;
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
}
