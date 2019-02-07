package the_fireplace.clans.raid;

import com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.HashMap;
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

	public HashMap<SerialBlockPos, String> getBlocks() {
		return blocks;
	}

	public UUID getClan() {
		return clan;
	}
}
