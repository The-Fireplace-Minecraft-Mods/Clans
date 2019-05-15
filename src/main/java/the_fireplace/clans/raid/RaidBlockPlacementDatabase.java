package the_fireplace.clans.raid;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import the_fireplace.clans.Clans;

import java.io.*;
import java.util.*;

public final class RaidBlockPlacementDatabase implements Serializable {
	private static final long serialVersionUID = 0xFADE;

	private static RaidBlockPlacementDatabase instance = null;
	private static final String dataFileName = "raidblockplacement.dat";
	private static File saveDir = Clans.getDataDir();

	public static RaidBlockPlacementDatabase getInstance() {
		if(instance == null)
			readFromFile();
		return instance;
	}

	private HashMap<UUID, List<String>> placedBlocks = Maps.newHashMap();

	private static void readFromFile() {
		if (saveDir == null)
			saveDir = Clans.getDataDir();
		File f = new File(saveDir, dataFileName);
		if (f.exists()) {
			try {
				ObjectInputStream stream = new ObjectInputStream(new FileInputStream(f));
				instance = (RaidBlockPlacementDatabase) stream.readObject();
				stream.close();
				f.delete();
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