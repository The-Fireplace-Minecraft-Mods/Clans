package the_fireplace.clans.raid;

import com.google.common.collect.Maps;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.util.BlockSerializeUtil;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChunkRestoreData implements Serializable {
	private static final long serialVersionUID = 0xEA7F00D;

	private UUID clan;
	private HashMap<SerialBlockPos, String> blocks = Maps.newHashMap();

	public ChunkRestoreData(UUID clan) {
		this.clan = clan;
	}

	public void addBlock(int x, int y, int z, String block) {
		blocks.put(new SerialBlockPos(x, y, z), block);
	}

	public String popBlock(int x, int y, int z) {
		return blocks.get(new SerialBlockPos(x, y, z));
	}

	public void restore(Chunk c) {
		//TODO teleport all players out of the chunk
		for(Map.Entry<SerialBlockPos, String> entry: blocks.entrySet())
			c.getWorld().setBlockState(new BlockPos(entry.getKey().getX(), entry.getKey().getY(), entry.getKey().getZ()), BlockSerializeUtil.blockFromString(entry.getValue()));
	}

	public UUID getClan() {
		return clan;
	}
}
