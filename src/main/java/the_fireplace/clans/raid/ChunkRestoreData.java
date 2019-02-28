package the_fireplace.clans.raid;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;
import the_fireplace.clans.util.BlockSerializeUtil;

import java.io.Serializable;
import java.util.*;

public class ChunkRestoreData implements Serializable {
	private static final long serialVersionUID = 0xEA7F00D;

	private UUID clan;
	private HashMap<SerialBlockPos, String> replaceBlocks = Maps.newHashMap();
	private ArrayList<SerialBlockPos> removeBlocks = Lists.newArrayList();

	public ChunkRestoreData(UUID clan) {
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

	public boolean delRemoveBlock(int x, int y, int z) {
		return removeBlocks.remove(new SerialBlockPos(x, y, z));
	}

	public void restore(IChunk c) {
		//TODO teleport all players out of the chunk
		for(SerialBlockPos entry: removeBlocks)
			Objects.requireNonNull(c.getWorldForge()).setBlockState(new BlockPos(entry.getX(), entry.getY(), entry.getZ()), Blocks.AIR.getDefaultState(), 2);
		for(Map.Entry<SerialBlockPos, String> entry: replaceBlocks.entrySet())
			Objects.requireNonNull(c.getWorldForge()).setBlockState(new BlockPos(entry.getKey().getX(), entry.getKey().getY(), entry.getKey().getZ()), BlockSerializeUtil.blockFromString(entry.getValue()), 2);
	}

	public UUID getClan() {
		return clan;
	}
}
