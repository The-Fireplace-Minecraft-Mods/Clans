package the_fireplace.clans.raid;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import the_fireplace.clans.clan.Clan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public final class RaidingParties {
	private static HashMap<String, Raid> raids = Maps.newHashMap();
	private static HashMap<EntityPlayerMP, Raid> raidingPlayers = Maps.newHashMap();
	private static ArrayList<Clan> raidedClans = Lists.newArrayList();
	private static HashMap<Clan, Raid> activeraids = Maps.newHashMap();

	public static HashMap<String, Raid> getRaids() {
		return raids;
	}

	public static Raid getRaid(String name){
		return raids.get(name);
	}

	public static Raid getRaid(EntityPlayerMP player){
		return raidingPlayers.get(player);
	}

	public static Set<EntityPlayerMP> getRaidingPlayers() {
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

	public static boolean isRaidedBy(Clan c, EntityPlayer player) {
		//noinspection SuspiciousMethodCalls
		return hasActiveRaid(c) && activeraids.get(c).getMembers().contains(player);
	}

	static void addRaid(String name, Raid raid){
		raids.put(name, raid);
		raidedClans.add(raid.getTarget());
	}

	static void removeRaid(Raid raid) {
		raids.remove(raid.getRaidName());
		raidedClans.remove(raid.getTarget());
	}

	public static void addRaider(EntityPlayerMP raider, Raid raid){
		raidingPlayers.put(raider, raid);
	}

	public static void removeRaider(EntityPlayerMP raider){
		raidingPlayers.remove(raider);
	}

	public static void initRaid(String raidName){
		Raid startingRaid = raids.remove(raidName);
		startingRaid.activate();
		activeraids.put(startingRaid.getTarget(), startingRaid);
	}

	public static void endRaid(Clan targetClan) {
		Raid raid = activeraids.remove(targetClan);
		for(EntityPlayerMP player: raid.getMembers())
			removeRaider(player);
		raidedClans.remove(targetClan);
		for(int id: DimensionManager.getIDs())
			for(Chunk c: FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(id).getChunkProvider().getLoadedChunks()) {
				ChunkRestoreData data = RaidRestoreDatabase.popChunkRestoreData(id, c);
				if(data != null)
					data.restore(c);
			}
	}

	public static ArrayList<Clan> getRaidedClans() {
		return raidedClans;
	}
}
