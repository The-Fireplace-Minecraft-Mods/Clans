package the_fireplace.clans.raid;

import com.google.common.collect.Maps;
import javafx.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;
import the_fireplace.clans.util.ChunkUtils;

import java.io.*;
import java.util.HashMap;

public final class RaidRestoreDatabase implements Serializable {
	private static final long serialVersionUID = 0x69696969;

	private static RaidRestoreDatabase instance = null;
	private static final String dataFileName = "raids.dat";
	private static File saveDir = DimensionManager.getCurrentSaveRootDirectory();

	public static RaidRestoreDatabase getInstance() {
		if(instance == null)
			load();
		return instance;
	}

	private HashMap<Pair<Integer, Pair<Integer, Integer>>, ChunkRestoreData> raidedChunks = Maps.newHashMap();

	public static void addBlock(int dim, Chunk c, BlockPos pos, String block) {
		Pair<Integer, Pair<Integer, Integer>> coords = new Pair<>(dim, new Pair<>(c.x, c.z));
		if(!getInstance().raidedChunks.containsKey(coords))
			getInstance().raidedChunks.put(coords, new ChunkRestoreData(ChunkUtils.getChunkOwner(c)));
		getInstance().raidedChunks.get(coords).addBlock(pos.getX(), pos.getY(), pos.getZ(), block);
		save();
	}

	public static ChunkRestoreData popChunkRestoreData(int dim, Chunk c) {
		return getInstance().raidedChunks.remove(new Pair<>(dim, new Pair<>(c.x, c.z)));
	}

	private static void load() {
		if (saveDir == null)
			saveDir = DimensionManager.getCurrentSaveRootDirectory();
		if (saveDir == null) {
			instance = new RaidRestoreDatabase();
			return;
		}
		File f = new File(saveDir, dataFileName);
		if (f.exists()) {
			try {
				ObjectInputStream stream = new ObjectInputStream(new FileInputStream(f));
				instance = (RaidRestoreDatabase) stream.readObject();
				stream.close();
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
				instance = new RaidRestoreDatabase();
				f.delete();
			}
		}
		if (instance == null)
			instance = new RaidRestoreDatabase();
	}

	@SuppressWarnings("Duplicates")
	public static void save() {
		try {
			if (saveDir == null)
				saveDir = DimensionManager.getCurrentSaveRootDirectory();
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(new File(saveDir, dataFileName)));
			out.writeObject(instance);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
