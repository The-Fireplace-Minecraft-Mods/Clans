package the_fireplace.clans.clan;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public final class ClanCache {
	private static HashMap<UUID, Clan> playerClans = Maps.newHashMap();
	private static HashMap<UUID, EnumRank> playerRanks = Maps.newHashMap();
	private static HashMap<String, Clan> clanNames = Maps.newHashMap();
	private static ArrayList<String> clanBanners = Lists.newArrayList();
	private static HashMap<UUID, Clan> clanInvites = Maps.newHashMap();

	@Nullable
	public static Clan getClan(UUID clanID){
		return ClanDatabase.getClan(clanID);
	}

	@Nullable
	public static Clan getClan(String clanName){
		if(clanNames.isEmpty())
			for(Clan clan: ClanDatabase.getClans())
				clanNames.put(clan.getClanName(), clan);
		return clanNames.get(clanName);
	}

	@Nullable
	public static Clan getPlayerClan(UUID player) {
		if(playerClans.containsKey(player))
			return playerClans.get(player);
		playerClans.put(player, ClanDatabase.lookupPlayerClan(player));
		return playerClans.get(player);
	}

	@Nonnull
	public static EnumRank getPlayerRank(UUID player) {
		if(playerRanks.get(player) != null)
			return playerRanks.get(player);
		Clan c = getPlayerClan(player);
		if(c != null)
			playerRanks.put(player, c.getMembers().get(player));
		else
			playerRanks.put(player, EnumRank.NOCLAN);
		return playerRanks.get(player);
	}

	public static boolean clanNameTaken(String clanName) {
		if(clanNames.isEmpty())
			for(Clan clan: ClanDatabase.getClans())
				clanNames.put(clan.getClanName(), clan);
		return clanName.toLowerCase().equals("wilderness") || clanName.toLowerCase().equals("underground") || clanNames.containsKey(clanName);
	}

	public static boolean clanBannerTaken(String clanBanner) {
		if(clanBanners.isEmpty())
			for(Clan clan: ClanDatabase.getClans())
				if(clan.getClanBanner() != null)
					clanBanners.add(clan.getClanBanner());
		return clanBanners.contains(clanBanner);
	}

	static void addBanner(String banner) {
		if(clanBanners.isEmpty())
			for(Clan clan: ClanDatabase.getClans())
				if(clan.getClanBanner() != null)
					clanBanners.add(clan.getClanBanner());
		clanBanners.add(banner);
	}

	static void removeBanner(String banner){
		clanBanners.remove(banner);
	}

	static void addName(Clan nameClan){
		if(clanNames.isEmpty())
			for(Clan clan: ClanDatabase.getClans())
				clanNames.put(clan.getClanName(), clan);
		clanNames.put(nameClan.getClanName(), nameClan);
	}

	static void removeName(String name){
		clanNames.remove(name);
	}

	public static boolean inviteToClan(UUID player, Clan clan) {
		if(!clanInvites.containsKey(player)) {
			clanInvites.put(player, clan);
			return true;
		}
		return false;
	}

	@Nullable
	public static Clan getInvite(UUID player) {
		return clanInvites.get(player);
	}

	public static void purgePlayerCache(UUID player) {
		playerClans.remove(player);
		playerRanks.remove(player);
		clanInvites.remove(player);
	}

	public static void updateRank(UUID player, EnumRank newRank) {
		playerRanks.put(player, newRank);
	}

	public static void removeInvite(UUID player) {
		clanInvites.remove(player);
	}
}
