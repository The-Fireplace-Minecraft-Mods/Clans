package the_fireplace.clans.raid;

import com.google.common.collect.Maps;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.IChunk;
import the_fireplace.clans.Clans;
import the_fireplace.clans.util.ChunkUtils;
import the_fireplace.clans.util.Pair;

import java.io.*;
import java.util.HashMap;

public final class RaidRestoreDatabase implements Serializable {
	private static final long serialVersionUID = 0x69696969;

	private static RaidRestoreDatabase instance = null;
	private static final String dataFileName = "raids.dat";
	private static File saveDir = Clans.getDataDir();

	public static RaidRestoreDatabase getInstance() {
		if(instance == null)
			load();
		return instance;
	}

	private HashMap<Pair<Integer, Pair<Integer, Integer>>, ChunkRestoreData> raidedChunks = Maps.newHashMap();

	@SuppressWarnings("Duplicates")
	public static void addRestoreBlock(int dim, IChunk c, BlockPos pos, String block) {
		Pair<Integer, Pair<Integer, Integer>> coords = new Pair<>(dim, new Pair<>(c.getPos().x, c.getPos().z));
		if(!getInstance().raidedChunks.containsKey(coords))
			getInstance().raidedChunks.put(coords, new ChunkRestoreData(ChunkUtils.getChunkOwner(c)));
		getInstance().raidedChunks.get(coords).addRestoreBlock(pos.getX(), pos.getY(), pos.getZ(), block);
		save();
	}

	public static String popRestoreBlock(int dim, IChunk c, BlockPos pos) {
		Pair<Integer, Pair<Integer, Integer>> coords = new Pair<>(dim, new Pair<>(c.getPos().x, c.getPos().z));
		if(!getInstance().raidedChunks.containsKey(coords))
			return null;
		String block = getInstance().raidedChunks.get(coords).popRestoreBlock(pos.getX(), pos.getY(), pos.getZ());
		if(block != null)
			save();
		return block;
	}

	@SuppressWarnings("Duplicates")
	public static void addRemoveBlock(int dim, IChunk c, BlockPos pos) {
		Pair<Integer, Pair<Integer, Integer>> coords = new Pair<>(dim, new Pair<>(c.getPos().x, c.getPos().z));
		if(!getInstance().raidedChunks.containsKey(coords))
			getInstance().raidedChunks.put(coords, new ChunkRestoreData(ChunkUtils.getChunkOwner(c)));
		getInstance().raidedChunks.get(coords).addRemoveBlock(pos.getX(), pos.getY(), pos.getZ());
		save();
	}

	public static boolean delRemoveBlock(int dim, IChunk c, BlockPos pos) {
		Pair<Integer, Pair<Integer, Integer>> coords = new Pair<>(dim, new Pair<>(c.getPos().x, c.getPos().z));
		if(!getInstance().raidedChunks.containsKey(coords))
			return false;
		boolean block = getInstance().raidedChunks.get(coords).delRemoveBlock(pos.getX(), pos.getY(), pos.getZ());
		if(block)
			save();
		return block;
	}

	public static ChunkRestoreData popChunkRestoreData(int dim, IChunk c) {
		ChunkRestoreData d = getInstance().raidedChunks.remove(new Pair<>(dim, new Pair<>(c.getPos().x, c.getPos().z)));
		if(d != null)
			save();
		return d;
	}

	private static void load() {
		if (saveDir == null)
			saveDir = Clans.getDataDir();
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
				saveDir = Clans.getDataDir();
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(new File(saveDir, dataFileName)));
			out.writeObject(instance);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
