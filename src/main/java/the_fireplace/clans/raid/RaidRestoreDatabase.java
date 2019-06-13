package the_fireplace.clans.raid;

import com.google.common.collect.Maps;
import the_fireplace.clans.Clans;
import the_fireplace.clans.util.ChunkPosition;
import the_fireplace.clans.util.Pair;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

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

	private static void load() {
		if (saveDir == null)
			saveDir = Clans.getDataDir();
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
				f.delete();
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
				instance = new RaidRestoreDatabase();
				f.delete();
			}
		}
		if (instance == null)
			instance = new RaidRestoreDatabase();
		else
			NewRaidRestoreDatabase.isChanged = true;
		for(Map.Entry<Pair<Integer, Pair<Integer, Integer>>, ChunkRestoreData> entry: instance.raidedChunks.entrySet())
			NewRaidRestoreDatabase.instance.raidedChunks.put(new ChunkPosition(entry.getKey().getValue2().getValue1(), entry.getKey().getValue2().getValue2(), entry.getKey().getValue1()), new NewChunkRestoreData(entry.getValue().toJsonObject()));
	}
}
