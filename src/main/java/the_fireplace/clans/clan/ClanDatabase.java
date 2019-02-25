package the_fireplace.clans.clan;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nullable;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public final class ClanDatabase implements Serializable {
	private static final long serialVersionUID = 0x696969;

	private static ClanDatabase instance = null;
	private static final String dataFileName = "clans.dat";
	private static File saveDir = DimensionManager.getCurrentSaveRootDirectory();

	public static ClanDatabase getInstance() {
		if(instance == null) {
			readFromFile();
			instance.opclan = new Clan();
		}
		return instance;
	}

	private HashMap<UUID, Clan> clans;
	private Clan opclan;

	private ClanDatabase(){
		clans = Maps.newHashMap();
	}

	public static Clan getOpClan() {
		Clan out = getInstance().opclan;
		if(out == null) {
			getInstance().opclan = new Clan();
			out = getInstance().opclan;
		}
		return out;
	}

	@Nullable
	public static Clan getClan(@Nullable UUID clanId){
		return getInstance().clans.get(clanId);
	}

	public static Collection<Clan> getClans(){
		return getInstance().clans.values();
	}

	static boolean addClan(UUID clanId, Clan clan){
		if(!getInstance().clans.containsKey(clanId)){
			getInstance().clans.put(clanId, clan);
			ClanCache.addName(clan);
			if(clan.getClanBanner() != null)
				ClanCache.addBanner(clan.getClanBanner());
			save();
			return true;
		}
		return false;
	}

	public static boolean removeClan(UUID clanId){
		if(getInstance().clans.containsKey(clanId)){
			Clan clan = getInstance().clans.remove(clanId);
			ClanCache.removeName(clan.getClanName());
			if(clan.getClanBanner() != null)
				ClanCache.removeBanner(clan.getClanBanner());
			for(UUID member: clan.getMembers().keySet())
				ClanCache.purgePlayerCache(member);
			save();
			return true;
		}
		return false;
	}

	/**
	 * An inefficient way to look up a player's clan. For efficiency, use {@link ClanCache#getPlayerClans(UUID)}
	 * @param player
	 * The player to get the clan of
	 * @return
	 * The player's clans, or an empty list if the player isn't in any
	 */
	static ArrayList<Clan> lookupPlayerClans(UUID player){
		ArrayList<Clan> clans = Lists.newArrayList();
		for(Clan clan : getInstance().clans.values())
			if(clan.getMembers().keySet().contains(player))
				clans.add(clan);
		return clans;
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
