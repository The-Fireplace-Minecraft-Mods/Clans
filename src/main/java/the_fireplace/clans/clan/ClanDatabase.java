package the_fireplace.clans.clan;

import com.google.common.collect.Maps;
import net.minecraftforge.common.DimensionManager;

import java.io.*;
import java.util.HashMap;
import java.util.UUID;

public final class ClanDatabase implements Serializable {

	private static ClanDatabase instance = null;
	private static final String dataFileName = "clans.dat";
	private static File saveDir = DimensionManager.getCurrentSaveRootDirectory();

	public static ClanDatabase getInstance() {
		if(instance == null)
			readFromFile();
		return instance;
	}

	private HashMap<UUID, Clan> clans;

	private ClanDatabase(){
		clans = Maps.newHashMap();
	}

	public static Clan getClan(UUID clanId){
		return getInstance().clans.get(clanId);
	}

	private static void readFromFile() {
		if (saveDir == null)
			saveDir = DimensionManager.getCurrentSaveRootDirectory();
		if (saveDir == null) {
			instance = new ClanDatabase();
			return;
		}
		File f = new File(saveDir, dataFileName);
		if (f.exists()) {
			try {
				ObjectInputStream stream = new ObjectInputStream(new FileInputStream(f));
				instance = (ClanDatabase) stream.readObject();
				stream.close();
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
				instance = new ClanDatabase();
				f.delete();
			}
		}
		if (instance == null)
			instance = new ClanDatabase();
	}

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
