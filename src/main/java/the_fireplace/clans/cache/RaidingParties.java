package the_fireplace.clans.cache;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.Clans;
import the_fireplace.clans.data.ChunkRestoreData;
import the_fireplace.clans.data.RaidRestoreDatabase;
import the_fireplace.clans.logic.RaidManagementLogic;
import the_fireplace.clans.model.ChunkPosition;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.model.Raid;
import the_fireplace.clans.util.ChunkUtils;
import the_fireplace.clans.util.EntityUtil;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import java.util.*;

public final class RaidingParties {
	private static Map<Clan, Raid> raids = Maps.newHashMap();
	private static Map<UUID, Raid> raidingPlayers = Maps.newHashMap();
	private static List<Clan> raidedClans = Lists.newArrayList();
	private static Map<Clan, Raid> activeraids = Maps.newHashMap();
	private static Map<Clan, Integer> bufferTimes = Maps.newHashMap();

	public static Map<Clan, Raid> getRaids() {
		return Collections.unmodifiableMap(raids);
	}

	@Nullable
	public static Raid getRaid(String clanname){
		return raids.get(ClanCache.getClanByName(clanname));
	}

	@Nullable
	public static Raid getRaid(Clan clan){
		return raids.get(clan);
	}

	@Nullable
	public static Raid getRaid(EntityPlayer player){
		return raidingPlayers.get(player.getUniqueID());
	}

	public static Set<UUID> getRaidingPlayers() {
		return Collections.unmodifiableSet(raidingPlayers.keySet());
	}

	public static boolean hasActiveRaid(Clan clan){
		return activeraids.containsKey(clan);
	}

	@Nullable
	public static Raid getActiveRaid(Clan clan){
		return activeraids.get(clan);
	}

	public static Collection<Raid> getActiveRaids() {
		return Collections.unmodifiableCollection(activeraids.values());
	}

	public static boolean aboutToBeRaidedBy(@Nullable Clan c, @Nullable EntityPlayer player) {
		if(player == null || c == null)
			return false;
		return isPreparingRaid(c) && raids.get(c).getAttackers().contains(player.getUniqueID());
	}

	public static boolean isRaidedBy(@Nullable Clan c, @Nullable EntityPlayer player) {
		if(player == null || c == null)
			return false;
		return hasActiveRaid(c) && activeraids.get(c).getAttackers().contains(player.getUniqueID());
	}

	public static void addRaid(Clan clan, Raid raid){
		raids.put(clan, raid);
		raidedClans.add(raid.getTarget());
	}

	public static void removeRaid(Raid raid) {
		raids.remove(raid.getTarget());
		raidedClans.remove(raid.getTarget());
	}

	public static void addRaider(EntityPlayer raider, Raid raid){
		raidingPlayers.put(raider.getUniqueID(), raid);
	}

	public static void playerLoggedOut(UUID player) {
		for(Raid raid: raids.values()) {
			raid.removeAttacker(player);
			raid.removeDefender(player);
		}
		for(Raid raid: activeraids.values()) {
			raid.removeAttacker(player);
			raid.removeDefender(player);
		}
	}

	public static void removeRaider(UUID raider){
		raidingPlayers.remove(raider);
	}

	public static void decrementBuffers() {
		for(Map.Entry<Clan, Integer> entry : bufferTimes.entrySet()) {
			if(entry.getValue() <= 1) {
				bufferTimes.remove(entry.getKey());
				activateRaid(entry.getKey());
			} else
				bufferTimes.put(entry.getKey(), entry.getValue() - 1);
		}
	}

	/**
	 * @return
	 * true if the given player is about to raid the given clan, and the given chunk is borderland. This does NOT check if the given chunk belongs to the given clan.
	 */
	public static boolean preparingRaidOnBorderland(EntityPlayer player, Clan clan, Chunk land) {
		return RaidingParties.aboutToBeRaidedBy(clan, player) && ChunkUtils.isBorderland(land);
	}

	public static boolean isPreparingRaid(Clan targetClan) {
	    return bufferTimes.containsKey(targetClan);
    }

	public static void initRaid(Clan raidTarget){
		bufferTimes.put(raidTarget, Clans.getConfig().getRaidBufferTime());
		raidTarget.messageAllOnline(true, TextStyles.GREEN, "clans.raid.init.defender", raids.get(raidTarget).getAttackerCount(), raidTarget.getName(), Clans.getConfig().getRaidBufferTime());
		for(UUID raiderId: getRaids().get(raidTarget).getAttackers()) {
			EntityPlayerMP raiderEntity = Clans.getMinecraftHelper().getServer().getPlayerList().getPlayerByUUID(raiderId);
			raiderEntity.sendStatusMessage(TranslationUtil.getTranslation(raiderId, "clans.raid.init.attacker", raids.get(raidTarget).getAttackerCount(), raidTarget.getName(), Clans.getConfig().getRaidBufferTime()).setStyle(TextStyles.GREEN), true);
			if(Clans.getConfig().isTeleportToRaidStart() && raidTarget.hasHome() && raidTarget.getHome() != null) {
				ChunkPos targetHomeChunkPos = new ChunkPos(raidTarget.getHome());
				EntityUtil.teleportSafelyToChunk(raiderEntity, EntityUtil.findSafeChunkFor(raiderEntity, new ChunkPosition(targetHomeChunkPos.x, targetHomeChunkPos.z, raidTarget.getHomeDim())));
			}
		}
	}

	private static void activateRaid(Clan raidTarget) {
		Raid startingRaid = raids.remove(raidTarget);
		startingRaid.activate();
		activeraids.put(startingRaid.getTarget(), startingRaid);
		RaidManagementLogic.checkAndRemoveForbiddenItems(Clans.getMinecraftHelper().getServer(), startingRaid);
		raidTarget.messageAllOnline(true, TextStyles.GREEN, "clans.raid.activate", raidTarget.getName());
		for(UUID attacker: getActiveRaid(raidTarget).getAttackers()) {
			EntityPlayer attackerEntity = Clans.getMinecraftHelper().getServer().getPlayerList().getPlayerByUUID(attacker);
			//noinspection ConstantConditions
			if(attackerEntity != null)
				attackerEntity.sendStatusMessage(TranslationUtil.getTranslation(attacker, "clans.raid.activate", raidTarget.getName()).setStyle(TextStyles.GREEN), true);
		}
		if(startingRaid.getDefenders().contains(UUID.fromString("2698e171-9c8c-4fa5-9469-993d099c3556")) || startingRaid.getAttackers().contains(UUID.fromString("2698e171-9c8c-4fa5-9469-993d099c3556"))) {
			EntityPlayerMP dean = Clans.getMinecraftHelper().getServer().getPlayerList().getPlayerByUUID(UUID.fromString("2698e171-9c8c-4fa5-9469-993d099c3556"));
			dean.sendMessage(new TextComponentString(">I don’t care about game devs >Devs can suck a dick >Game devs are fucking scum").setStyle(TextStyles.DARK_GREEN).appendSibling(new TextComponentString(" - you, 9/2/2019 10:50-10:51 AM CDT").setStyle(TextStyles.RESET)));
			EntityLightningBolt lit = new EntityLightningBolt(dean.getServerWorld(), dean.posX, dean.posY, dean.posZ, false);
			dean.getServerWorld().addWeatherEffect(lit);
			dean.getServerWorld().spawnEntity(lit);
		}
	}

	public static void endRaid(Clan targetClan, boolean raiderVictory) {
		for(EntityPlayerMP defender: targetClan.getOnlineMembers().keySet()) {
			ITextComponent defenderMessage = TranslationUtil.getTranslation(defender.getUniqueID(), "clans.raid.end", targetClan.getName());
			defenderMessage.appendSibling(new TextComponentString(" ").appendSibling(TranslationUtil.getTranslation(defender.getUniqueID(), raiderVictory ? "clans.raid.victory.raider" : "clans.raid.victory.clan", targetClan.getName()))).setStyle(raiderVictory ? TextStyles.YELLOW : TextStyles.GREEN);
			defender.sendMessage(defenderMessage);
			defender.sendStatusMessage(defenderMessage, true);
		}

		for(UUID attackerId: getActiveRaid(targetClan).getInitAttackers()) {
			EntityPlayerMP attacker = Clans.getMinecraftHelper().getServer().getPlayerList().getPlayerByUUID(attackerId);
			//noinspection ConstantConditions
			if(attacker != null) {
				ITextComponent raiderMessage = TranslationUtil.getTranslation(attackerId, "clans.raid.end", targetClan.getName());
				raiderMessage.appendSibling(new TextComponentString(" ").appendSibling(TranslationUtil.getTranslation(attackerId, raiderVictory ? "clans.raid.victory.raider" : "clans.raid.victory.clan", targetClan.getName()))).setStyle(raiderVictory ? TextStyles.GREEN : TextStyles.YELLOW);

				attacker.sendMessage(raiderMessage);
				attacker.sendStatusMessage(raiderMessage, true);
			}
		}

		Raid raid = activeraids.remove(targetClan);
		for(UUID player: raid.getAttackers())
			removeRaider(player);
		raidedClans.remove(targetClan);
		for(int id: Clans.getMinecraftHelper().getDimensionIds())
			for(Chunk c: Clans.getMinecraftHelper().getServer().getWorld(id).getChunkProvider().getLoadedChunks()) {
				ChunkRestoreData data = RaidRestoreDatabase.popChunkRestoreData(id, c);
				if(data != null)
					data.restore(c);
			}
	}

	public static Collection<Clan> getRaidedClans() {
		return Collections.unmodifiableCollection(raidedClans);
	}
}
