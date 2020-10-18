package the_fireplace.clans.cache;

import com.google.common.collect.Sets;
import io.netty.util.internal.ConcurrentSet;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import the_fireplace.clans.commands.CommandClan;
import the_fireplace.clans.commands.CommandOpClan;
import the_fireplace.clans.commands.CommandRaid;
import the_fireplace.clans.data.ClanDatabase;
import the_fireplace.clans.data.PlayerData;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.util.TextStyles;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class ClanCache {
	private static final Map<UUID, Set<Clan>> playerClans = new ConcurrentHashMap<>();
	private static final Map<String, Clan> clanNames = new ConcurrentHashMap<>();
	private static final Set<String> clanBanners = new ConcurrentSet<>();
	private static final Map<Clan, BlockPos> clanHomes = new ConcurrentHashMap<>();

	private static final Set<UUID> buildAdmins = new ConcurrentSet<>();

	//Map of Clan ID -> List of invited players
	private static final Map<UUID, Set<UUID>> invitedPlayers = new ConcurrentHashMap<>();

	private static final Set<String> forbiddenClanNames = Sets.newHashSet("wilderness", "underground", "opclan", "clan", "raid", "null");
	static {
		forbiddenClanNames.addAll(CommandClan.commands.keySet());
		forbiddenClanNames.addAll(CommandClan.aliases.keySet());
		forbiddenClanNames.addAll(CommandOpClan.commands.keySet());
		forbiddenClanNames.addAll(CommandOpClan.aliases.keySet());
		forbiddenClanNames.addAll(CommandRaid.commands.keySet());
		forbiddenClanNames.addAll(CommandRaid.aliases.keySet());
	}

	public static boolean isForbiddenClanName(String name) {
		return forbiddenClanNames.contains(name);
	}

	@Nullable
	public static Clan getClanById(@Nullable UUID clanID){
		return ClanDatabase.getClan(clanID);
	}

	@Nullable
	public static Clan getClanByName(String clanName){
		ensureNameCacheLoaded();
		return clanNames.get(clanName.toLowerCase());
	}

	public static Collection<Clan> getPlayerClans(@Nullable UUID player) {
		if(player == null)
			return Collections.unmodifiableCollection(Collections.emptySet());
		ensurePlayerClansCached(player);
		return Collections.unmodifiableSet(playerClans.get(player));
	}

	public static void addPlayerClan(UUID player, Clan clan) {
		ensurePlayerClansCached(player);
		playerClans.get(player).add(clan);
	}

	public static void removePlayerClan(UUID player, Clan clan) {
		ensurePlayerClansCached(player);
		playerClans.get(player).remove(clan);
	}

	private static void ensurePlayerClansCached(UUID player) {
		if(!playerClans.containsKey(player)) {
			Set<Clan> clansFromDb = new ConcurrentSet<>();
			clansFromDb.addAll(ClanDatabase.lookupPlayerClans(player));
			playerClans.put(player, clansFromDb);
		}
	}

	public static EnumRank getPlayerRank(UUID player, Clan clan) {
		return clan.getMembers().get(player);
	}

	public static boolean clanNameTaken(String clanName) {
		clanName = clanName.toLowerCase();
		ensureNameCacheLoaded();
		return forbiddenClanNames.contains(clanName) || clanNames.containsKey(clanName);
	}

	public static void addName(Clan nameClan){
		ensureNameCacheLoaded();
		clanNames.put(TextStyles.stripFormatting(nameClan.getName().toLowerCase()), nameClan);
	}

	public static Map<String, Clan> getClanNames() {
		ensureNameCacheLoaded();
		return Collections.unmodifiableMap(clanNames);
	}

	private static void ensureNameCacheLoaded() {
		if (clanNames.isEmpty())
			for (Clan clan : ClanDatabase.getClans())
				clanNames.put(TextStyles.stripFormatting(clan.getName().toLowerCase()), clan);
	}

	public static void removeName(String name){
		clanNames.remove(TextStyles.stripFormatting(name.toLowerCase()));
	}

	public static boolean clanBannerTaken(String clanBanner) {
		ensureBannerCacheLoaded();
		return clanBanners.contains(clanBanner.toLowerCase());
	}

	public static void addBanner(String banner) {
		ensureBannerCacheLoaded();
		clanBanners.add(banner.toLowerCase());
	}

	private static void ensureBannerCacheLoaded() {
		if (clanBanners.isEmpty())
			for (Clan clan : ClanDatabase.getClans())
				if (clan.getBanner() != null)
					clanBanners.add(clan.getBanner().toLowerCase());
	}

	public static void removeBanner(@Nullable String banner){
		if(banner != null)
			clanBanners.remove(banner.toLowerCase());
	}

	public static Map<Clan, BlockPos> getClanHomes() {
		ensureClanHomeCacheLoaded();
		return Collections.unmodifiableMap(clanHomes);
	}

	public static void setClanHome(Clan c, BlockPos home) {
		ensureClanHomeCacheLoaded();
		clanHomes.put(c, home);
	}

	private static void ensureClanHomeCacheLoaded() {
		if(clanHomes.isEmpty())
			for(Clan clan: ClanDatabase.getClans())
				if(clan.hasHome())
					clanHomes.put(clan, clan.getHome());
	}

	public static void clearClanHome(Clan c) {
		clanHomes.remove(c);
	}

	public static void removeClan(Clan c) {
		clearClanHome(c);
		for(UUID player: playerClans.keySet())
			removePlayerClan(player, c);
		removeName(c.getName());
		removeBanner(c.getBanner());
	}

	public static boolean toggleClaimAdmin(EntityPlayerMP admin){
		return toggleClaimAdmin(admin.getUniqueID());
	}

	public static boolean toggleClaimAdmin(UUID admin){
		if(buildAdmins.contains(admin)) {
			buildAdmins.remove(admin);
			return false;
		} else {
			buildAdmins.add(admin);
			return true;
		}
	}

	public static boolean isClaimAdmin(EntityPlayerMP admin) {
		return isClaimAdmin(admin.getUniqueID());
	}

	public static boolean isClaimAdmin(UUID admin) {
		return buildAdmins.contains(admin);
	}

	public static Collection<UUID> getInvitedPlayers(UUID clanId) {
		invitedPlayers.putIfAbsent(clanId, new ConcurrentSet<>());
		return invitedPlayers.get(clanId);
	}

	/**
	 * Load all player data on another thread. Only do this when the server is starting.
	 */
	public static void loadInvitedPlayers() {
		new Thread(() -> {
			File[] files = PlayerData.playerDataLocation.listFiles();
			if(files != null)
				for(File f: files) {
					try {
						UUID playerId = UUID.fromString(f.getName().replace(".json", ""));
						for(UUID clanId: PlayerData.getInvites(playerId))
							cacheInvite(clanId, playerId);
					} catch(IllegalArgumentException ignored) {}
				}
		}).start();
	}

	/**
	 * INTERNAL USE ONLY. This will not actually add the invite, it just adds it to cache. PlayerData.addInvite takes care of actually adding it.
	 */
	public static void cacheInvite(UUID clanId, UUID playerId) {
		invitedPlayers.putIfAbsent(clanId, new ConcurrentSet<>());
		invitedPlayers.get(clanId).add(playerId);
	}

	/**
	 * INTERNAL USE ONLY. This will not actually revoke the invite, it just removes it from cache. PlayerData.removeInvite takes care of actually removing it.
	 */
	public static void uncacheInvite(UUID clanId, UUID playerId) {
		invitedPlayers.putIfAbsent(clanId, new ConcurrentSet<>());
		invitedPlayers.get(clanId).remove(playerId);
	}
}
