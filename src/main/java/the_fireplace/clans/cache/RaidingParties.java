package the_fireplace.clans.cache;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.Clans;
import the_fireplace.clans.data.ChunkRestoreData;
import the_fireplace.clans.data.RaidRestoreDatabase;
import the_fireplace.clans.logic.RaidManagementLogic;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.model.Raid;
import the_fireplace.clans.util.ChunkUtils;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import java.util.*;

public final class RaidingParties {
	private static HashMap<Clan, Raid> raids = Maps.newHashMap();
	private static HashMap<UUID, Raid> raidingPlayers = Maps.newHashMap();
	private static ArrayList<Clan> raidedClans = Lists.newArrayList();
	private static HashMap<Clan, Raid> activeraids = Maps.newHashMap();
	private static HashMap<Clan, Integer> bufferTimes = Maps.newHashMap();

	public static HashMap<Clan, Raid> getRaids() {
		return raids;
	}

	public static Raid getRaid(String name){
		return raids.get(ClanCache.getClanByName(name));
	}

	public static Raid getRaid(Clan clan){
		return raids.get(clan);
	}

	public static Raid getRaid(EntityPlayer player){
		return raidingPlayers.get(player.getUniqueID());
	}

	public static Set<UUID> getRaidingPlayers() {
		return Sets.newHashSet(raidingPlayers.keySet());
	}

	public static boolean hasActiveRaid(Clan clan){
		return activeraids.containsKey(clan);
	}

	public static Raid getActiveRaid(Clan clan){
		return activeraids.get(clan);
	}

	public static Collection<Raid> getActiveRaids() {
		return Sets.newHashSet(activeraids.values());
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
		raidTarget.messageAllOnline(TextStyles.GREEN, "clans.raid.init.defender", raids.get(raidTarget).getAttackerCount(), raidTarget.getClanName(), Clans.getConfig().getRaidBufferTime());
		for(UUID attacker: getRaids().get(raidTarget).getAttackers())
			Clans.getMinecraftHelper().getServer().getPlayerList().getPlayerByUUID(attacker).sendMessage(TranslationUtil.getTranslation(attacker, "clans.raid.init.attacker", raids.get(raidTarget).getAttackerCount(), raidTarget.getClanName(), Clans.getConfig().getRaidBufferTime()).setStyle(TextStyles.GREEN));
	}

	private static void activateRaid(Clan raidTarget) {
		Raid startingRaid = raids.remove(raidTarget);
		startingRaid.activate();
		activeraids.put(startingRaid.getTarget(), startingRaid);
		RaidManagementLogic.checkAndRemoveForbiddenItems(Clans.getMinecraftHelper().getServer(), startingRaid);
		raidTarget.messageAllOnline(TextStyles.GREEN, "clans.raid.activate", raidTarget.getClanName());
		for(UUID attacker: getActiveRaid(raidTarget).getAttackers())
			Clans.getMinecraftHelper().getServer().getPlayerList().getPlayerByUUID(attacker).sendMessage(TranslationUtil.getTranslation(attacker, "clans.raid.activate", raidTarget.getClanName()).setStyle(TextStyles.GREEN));
	}

	public static void endRaid(Clan targetClan, boolean raiderVictory) {
		for(EntityPlayerMP defender: targetClan.getOnlineMembers().keySet()) {
			ITextComponent defenderMessage = TranslationUtil.getTranslation(defender.getUniqueID(), "clans.raid.end", targetClan.getClanName());
			defenderMessage.appendSibling(new TextComponentString(" ").appendSibling(TranslationUtil.getTranslation(defender.getUniqueID(), raiderVictory ? "clans.raid.victory.raider" : "clans.raid.victory.clan"))).setStyle(raiderVictory ? TextStyles.YELLOW : TextStyles.GREEN);
			defender.sendMessage(defenderMessage);
		}

		for(UUID attackerId: getActiveRaid(targetClan).getInitAttackers()) {
			EntityPlayerMP attacker = Clans.getMinecraftHelper().getServer().getPlayerList().getPlayerByUUID(attackerId);
			//noinspection ConstantConditions
			if(attacker != null) {
				ITextComponent raiderMessage = TranslationUtil.getTranslation(attackerId, "clans.raid.end", targetClan.getClanName());
				raiderMessage.appendSibling(new TextComponentString(" ").appendSibling(TranslationUtil.getTranslation(attackerId, raiderVictory ? "clans.raid.victory.raider" : "clans.raid.victory.clan"))).setStyle(raiderVictory ? TextStyles.GREEN : TextStyles.YELLOW);

				attacker.sendMessage(raiderMessage);
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
		return raidedClans;
	}
}
