package the_fireplace.clans.raid;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.registries.ForgeRegistries;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;

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
		return raids.get(ClanCache.getClan(name));
	}

	public static Raid getRaid(Clan target){
		return raids.get(target);
	}

	public static Raid getRaid(EntityPlayerMP player){
		return raidingPlayers.get(player.getUniqueID());
	}

	public static Set<UUID> getRaidingPlayers() {
		return raidingPlayers.keySet();
	}

	public static boolean hasActiveRaid(Clan clan){
		return activeraids.containsKey(clan);
	}

	public static Raid getActiveRaid(Clan clan){
		return activeraids.get(clan);
	}

	public static Collection<Raid> getActiveRaids() {
		return activeraids.values();
	}

	public static boolean isRaidedBy(Clan c, UUID player) {
		return hasActiveRaid(c) && activeraids.get(c).getMembers().contains(player);
	}

	static void addRaid(Clan clan, Raid raid){
		raids.put(clan, raid);
		raidedClans.add(raid.getTarget());
	}

	static void removeRaid(Raid raid) {
		raids.remove(raid.getTarget());
		raidedClans.remove(raid.getTarget());
	}

	public static void addRaider(UUID raider, Raid raid){
		raidingPlayers.put(raider, raid);
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

	public static boolean isPreparingRaid(Clan targetClan) {
	    return bufferTimes.containsKey(targetClan);
    }

	public static void initRaid(Clan raidTarget) {
		bufferTimes.put(raidTarget, Clans.cfg.raidBufferTime);
		for(EntityPlayerMP member: raidTarget.getOnlineMembers().keySet())
			member.sendMessage(new TextComponentTranslation("A raiding party with %s members is preparing to raid %s.", raids.get(raidTarget).getMemberCount(), raidTarget.getClanName()));
	}

	private static void activateRaid(Clan raidTarget) {
		Raid startingRaid = raids.remove(raidTarget);
		startingRaid.activate();
		activeraids.put(startingRaid.getTarget(), startingRaid);
	}

	public static void endRaid(Clan targetClan) {
		Raid raid = activeraids.remove(targetClan);
		for(UUID player: raid.getMembers())
			removeRaider(player);
		raidedClans.remove(targetClan);
		for(ResourceLocation location: DimensionManager.getRegistry().getKeys()) {//TODO Find out if this works on modded dimensions
			DimensionType dimType = DimensionManager.getRegistry().get(location);
			for (Chunk c : ServerLifecycleHooks.getCurrentServer().getWorld(dimType).getChunkProvider().getLoadedChunks()) {
				ChunkRestoreData data = RaidRestoreDatabase.popChunkRestoreData(dimType.getId(), c);
				if (data != null)
					data.restore(c);
			}
		}
	}

	public static ArrayList<Clan> getRaidedClans() {
		return raidedClans;
	}
}
