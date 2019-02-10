package the_fireplace.clans.raid;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.DimensionManager;

import java.io.*;
import java.util.*;

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

	public static HashMap<UUID, List<ItemStack>> getPlacedBlocks() {
		return getInstance().placedBlocks;
	}

	private HashMap<UUID, List<ItemStack>> placedBlocks = Maps.newHashMap();

	public static boolean hasPlacedBlocks(UUID player){
		return getPlacedBlocks().containsKey(player) && !getPlacedBlocks().get(player).isEmpty();
	}

	public void addPlacedBlock(UUID player, ItemStack payout){
		if(!placedBlocks.containsKey(player))
			placedBlocks.put(player, Lists.newArrayList());
		placedBlocks.get(player).add(payout);
		saveToFile();
	}

	public static List<ItemStack> getPlacedBlocks(UUID player){
		return hasPlacedBlocks(player) ? getInstance().placedBlocks.get(player) : Lists.newArrayList();
	}

	public void removePlacedBlocks(UUID player, Collection<ItemStack> toRemove){
		placedBlocks.get(player).removeAll(toRemove);
	}

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
	}

	@SuppressWarnings("Duplicates")
	private static void saveToFile() {
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