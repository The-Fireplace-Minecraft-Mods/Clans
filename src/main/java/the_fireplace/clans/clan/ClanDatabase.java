package the_fireplace.clans.clan;

import com.google.common.collect.Maps;
import net.minecraftforge.common.DimensionManager;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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

	static Collection<Clan> getClans(){
		return getInstance().clans.values();
	}

	static boolean addClan(UUID clanId, Clan clan){
		if(!getInstance().clans.containsKey(clanId)){
			getInstance().clans.put(clanId, clan);
			return true;
		}
		return false;
	}

	/**
	 * An inefficient way to look up a player's clan. For efficiency, use {@link ClanCache#getPlayerClan(UUID)}
	 * @param player
	 * The player to get the clan of
	 * @return
	 * The player's clan, or null if it doesn't have one.
	 */
	static Clan lookupPlayerClan(UUID player){
		for(Clan clan : getInstance().clans.values())
			if(clan.getMembers().keySet().contains(player))
				return clan;
		return null;
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
