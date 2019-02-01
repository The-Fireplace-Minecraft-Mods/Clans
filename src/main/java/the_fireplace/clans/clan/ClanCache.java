package the_fireplace.clans.clan;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public final class ClanCache {
	private static HashMap<UUID, Clan> playerClans = Maps.newHashMap();
	private static HashMap<UUID, EnumRank> playerRanks = Maps.newHashMap();
	private static ArrayList<String> clanNames = Lists.newArrayList();
	private static ArrayList<String> clanBanners = Lists.newArrayList();

	public static Clan getClan(UUID clanID){
		return ClanDatabase.getClan(clanID);
	}

	public static Clan getPlayerClan(UUID player){
		if(playerClans.containsKey(player))
			return playerClans.get(player);
		playerClans.put(player, ClanDatabase.lookupPlayerClan(player));
		return playerClans.get(player);
	}

	public static EnumRank getPlayerRank(UUID player){
		if(playerRanks.get(player) != null)
			return playerRanks.get(player);
		playerRanks.put(player, getPlayerClan(player).getMembers().get(player));
		return playerRanks.get(player);
	}

	public static boolean clanNameTaken(String clanName){
		if(clanNames.isEmpty())
			for(Clan clan: ClanDatabase.getClans())
				clanNames.add(clan.getClanName());
		return clanNames.contains(clanName);
	}

	public static boolean clanBannerTaken(String clanBanner){
		if(clanBanners.isEmpty())
			for(Clan clan: ClanDatabase.getClans())
				if(clan.getClanBanner() != null)
					clanBanners.add(clan.getClanBanner());
		return clanBanners.contains(clanBanner);
	}

	static void addBanner(String banner){
		if(clanBanners.isEmpty())
			for(Clan clan: ClanDatabase.getClans())
				if(clan.getClanBanner() != null)
					clanBanners.add(clan.getClanBanner());
		clanBanners.add(banner);
	}

	static void removeBanner(String banner){
		clanBanners.remove(banner);
	}

	static void addName(String name){
		if(clanNames.isEmpty())
			for(Clan clan: ClanDatabase.getClans())
				clanNames.add(clan.getClanName());
		clanNames.add(name);
	}

	static void removeName(String name){
		clanNames.remove(name);
	}

	public static void purgePlayerCache(UUID player){
		playerClans.remove(player);
		playerRanks.remove(player);
	}
}
