package the_fireplace.clans.raid;

import com.google.common.collect.Maps;
import net.minecraft.entity.player.EntityPlayerMP;
import the_fireplace.clans.clan.Clan;

import java.util.HashMap;
import java.util.Set;

public final class RaidingParties {
	private static HashMap<String, Raid> raids = Maps.newHashMap();
	private static HashMap<EntityPlayerMP, Raid> raidingPlayers = Maps.newHashMap();
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

	public static HashMap<Clan, Raid> getActiveraids() {
		return activeraids;
	}

	static void addRaid(String name, Raid raid){
		raids.put(name, raid);
	}

	static void removeRaid(Raid raid) {
		raids.remove(raid.getRaidName());
	}

	public static void addRaider(EntityPlayerMP raider, Raid raid){
		raidingPlayers.put(raider, raid);
	}

	public static void removeRaider(EntityPlayerMP raider){
		raidingPlayers.remove(raider);
	}
}
