package the_fireplace.clans.raid;

import com.google.common.collect.Maps;
import net.minecraftforge.common.DimensionManager;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class RaidBlockPlacementDatabase implements Serializable {
	private static final long serialVersionUID = 0xFADE;

	private static RaidBlockPlacementDatabase instance = null;
	private static final String dataFileName = "raidblockplacement.dat";
	private static File saveDir = DimensionManager.getCurrentSaveRootDirectory();

	public static RaidBlockPlacementDatabase getInstance() {
		if(instance == null)
			readFromFile();
		return instance;
	}

	private HashMap<UUID, List<String>> placedBlocks = Maps.newHashMap();

	private static void readFromFile() {
		if (saveDir == null)
			saveDir = DimensionManager.getCurrentSaveRootDirectory();
		if (saveDir == null) {
			instance = new RaidBlockPlacementDatabase();
			return;
		}
		File f = new File(saveDir, dataFileName);
		if (f.exists()) {
			try {
				ObjectInputStream stream = new ObjectInputStream(new FileInputStream(f));
				instance = (RaidBlockPlacementDatabase) stream.readObject();
				stream.close();
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
				instance = new RaidBlockPlacementDatabase();
				f.delete();
			}
		}
		if (instance == null)
			instance = new RaidBlockPlacementDatabase();
		else
			NewRaidBlockPlacementDatabase.isChanged = true;
		for(Map.Entry<UUID, List<String>> entry: instance.placedBlocks.entrySet())
			NewRaidBlockPlacementDatabase.instance.placedBlocks.put(entry.getKey(), entry.getValue());
	}
}