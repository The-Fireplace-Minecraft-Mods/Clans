package the_fireplace.clans.cache;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import the_fireplace.clans.data.ClanDatabase;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.model.OrderedPair;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class ClanCache {
	private static HashMap<UUID, ArrayList<Clan>> playerClans = Maps.newHashMap();
	private static HashMap<String, Clan> clanNames = Maps.newHashMap();
	private static ArrayList<String> clanBanners = Lists.newArrayList();
	private static HashMap<UUID, Clan> clanInvites = Maps.newHashMap();
	private static HashMap<Clan, BlockPos> clanHomes = Maps.newHashMap();

	private static ArrayList<UUID> buildAdmins = Lists.newArrayList();
	private static HashMap<UUID, Clan> clanChattingPlayers = Maps.newHashMap();

	//Maps of (Player Unique ID) -> (Clan)
	private static HashMap<UUID, Clan> autoAbandonClaims = Maps.newHashMap();
	private static HashMap<UUID, Clan> autoClaimLands = Maps.newHashMap();
	private static HashMap<UUID, Boolean> opAutoAbandonClaims = Maps.newHashMap();
	private static HashMap<UUID, OrderedPair<Clan, Boolean>> opAutoClaimLands = Maps.newHashMap();

	public static final ArrayList<String> forbiddenClanNames = Lists.newArrayList("wilderness", "underground", "opclan", "clan", "banner", "b", "details", "d", "disband", "form", "create", "claim", "c", "abandonclaim", "ac", "map", "m", "invite", "i", "kick", "accept", "decline", "leave", "promote", "demote", "sethome", "setbanner", "setname", "info", "setdescription", "setdesc", "setdefault", "home", "h", "trapped", "t", "help", "balance", "af", "addfunds", "deposit", "takefunds", "withdraw", "setrent", "finances", "setshield", "buildadmin", "ba", "playerinfo", "pi", "list", "fancymap", "fm");

	@Nullable
	public static Clan getClanById(@Nullable UUID clanID){
		return ClanDatabase.getClan(clanID);
	}

	@Nullable
	public static Clan getClanByName(String clanName){
		if(clanNames.isEmpty())
			for(Clan clan: ClanDatabase.getClans())
				clanNames.put(clan.getClanName().toLowerCase(), clan);
		return clanNames.get(clanName.toLowerCase());
	}

	public static ArrayList<Clan> getPlayerClans(@Nullable UUID player) {
		if(player == null)
			return Lists.newArrayList();
		if(playerClans.containsKey(player))
			return (playerClans.get(player) != null ? playerClans.get(player) : Lists.newArrayList());
		playerClans.put(player, ClanDatabase.lookupPlayerClans(player));
		return (playerClans.get(player) != null ? Lists.newArrayList(playerClans.get(player)) : Lists.newArrayList());
	}

	public static EnumRank getPlayerRank(UUID player, Clan clan) {
		return clan.getMembers().get(player);
	}

	public static boolean clanNameTaken(String clanName) {
		clanName = clanName.toLowerCase();
		if(clanNames.isEmpty())
			for(Clan clan: ClanDatabase.getClans())
				clanNames.put(clan.getClanName().toLowerCase(), clan);
		return forbiddenClanNames.contains(clanName) || clanNames.containsKey(clanName);
	}

	public static boolean clanBannerTaken(String clanBanner) {
		if(clanBanners.isEmpty())
			for(Clan clan: ClanDatabase.getClans())
				if(clan.getClanBanner() != null)
					clanBanners.add(clan.getClanBanner().toLowerCase());
		return clanBanners.contains(clanBanner.toLowerCase());
	}

	public static void addBanner(String banner) {
		if(clanBanners.isEmpty())
			for(Clan clan: ClanDatabase.getClans())
				if(clan.getClanBanner() != null)
					clanBanners.add(clan.getClanBanner().toLowerCase());
		clanBanners.add(banner.toLowerCase());
	}

	public static void removeBanner(String banner){
		clanBanners.remove(banner.toLowerCase());
	}

	public static HashMap<String, Clan> getClanNames() {
		return Maps.newHashMap(clanNames);
	}

	public static void addName(Clan nameClan){
		if(clanNames.isEmpty())
			for(Clan clan: ClanDatabase.getClans())
				clanNames.put(clan.getClanName().toLowerCase(), clan);
		clanNames.put(nameClan.getClanName().toLowerCase(), nameClan);
	}

	public static void removeName(String name){
		clanNames.remove(name.toLowerCase());
	}

	public static boolean inviteToClan(UUID player, Clan clan) {
		if(!clanInvites.containsKey(player)) {
			clanInvites.put(player, clan);
			return true;
		}
		return false;
	}

	public static void addPlayerClan(UUID player, Clan clan) {
		getPlayerClans(player);
		playerClans.get(player).add(clan);
	}

	public static void removePlayerClan(UUID player, Clan clan) {
		getPlayerClans(player);
		playerClans.get(player).remove(clan);
	}

	@Nullable
	public static Clan getInvite(UUID player) {
		return clanInvites.get(player);
	}

	@Nullable
	public static Clan removeInvite(UUID player) {
		return clanInvites.remove(player);
	}

	public static HashMap<Clan, BlockPos> getClanHomes() {
		if(clanHomes.isEmpty())
			for(Clan clan: ClanDatabase.getClans())
				if(clan.hasHome())
					clanHomes.put(clan, clan.getHome());
		return Maps.newHashMap(clanHomes);
	}

	public static void setClanHome(Clan c, BlockPos home) {
		if(clanHomes.isEmpty())
			for(Clan clan: ClanDatabase.getClans())
				clanHomes.put(clan, clan.getHome());
		clanHomes.put(c, home);
	}

	public static void clearClanHome(Clan c) {
		clanHomes.remove(c);
	}

	public static void removeClan(Clan c) {
		clearClanHome(c);
		for(Map.Entry<UUID, Clan> clanInvite: clanInvites.entrySet())
			if(clanInvite.getValue().equals(c))
				clanInvites.remove(clanInvite.getKey());
		for(UUID player: playerClans.keySet())
			removePlayerClan(player, c);
		removeName(c.getClanName());
		removeBanner(c.getClanBanner());
	}

	public static boolean toggleClaimAdmin(EntityPlayerMP admin){
		if(buildAdmins.contains(admin.getUniqueID())) {
			buildAdmins.remove(admin.getUniqueID());
			return false;
		} else {
			buildAdmins.add(admin.getUniqueID());
			return true;
		}
	}

	public static boolean isClaimAdmin(EntityPlayerMP admin) {
		return buildAdmins.contains(admin.getUniqueID());
	}

	public static void updateChat(UUID uuid, Clan clan) {
		if(getClanChattingPlayers().containsKey(uuid) && getClanChattingPlayers().get(uuid).equals(clan))
			getClanChattingPlayers().remove(uuid);
		else
			getClanChattingPlayers().put(uuid, clan);
	}

	public static HashMap<UUID, Clan> getClanChattingPlayers() {
		return clanChattingPlayers;
	}

	public static HashMap<UUID, Clan> getAutoAbandonClaims() {
		return autoAbandonClaims;
	}

	public static HashMap<UUID, Clan> getAutoClaimLands() {
		return autoClaimLands;
	}

	public static HashMap<UUID, Boolean> getOpAutoAbandonClaims() {
		return opAutoAbandonClaims;
	}

	public static HashMap<UUID, OrderedPair<Clan, Boolean>> getOpAutoClaimLands() {
		return opAutoClaimLands;
	}
}
