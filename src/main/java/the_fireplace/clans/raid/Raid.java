package the_fireplace.clans.raid;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayerMP;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.Clan;

import java.util.ArrayList;

public class Raid {
	private String raidName;
	private ArrayList<EntityPlayerMP> members;
	private Clan target;
	private int remainingSeconds = Clans.cfg.maxRaidDuration * 60;
	private int attackerAbandonmentTime = 0;
	private int defenderAbandonmentTime = 0;
	private ArrayList<EntityPlayerMP> deadDefenders;

	private long cost;

	public Raid(String raidName, EntityPlayerMP starter, Clan targetClan, long raidCost){
		this.raidName = raidName;
		members = Lists.newArrayList();
		addMember(starter);
		this.target = targetClan;
		cost = raidCost;
		RaidingParties.addRaid(raidName, this);
	}

	public ArrayList<EntityPlayerMP> getMembers() {
		return members;
	}

	public String getRaidName() {
		return raidName;
	}

	public int getMemberCount(){
		return members.size();
	}

	public void addMember(EntityPlayerMP player) {
		this.members.add(player);
		RaidingParties.addRaider(player, this);
	}

	public boolean removeMember(EntityPlayerMP player) {
		boolean rm = this.members.remove(player);
		if(rm) {
			RaidingParties.removeRaider(player);
			if(this.members.isEmpty())
				RaidingParties.removeRaid(this);
		}
		return rm;
	}

	public Clan getTarget() {
		return target;
	}

	public int getRemainingSeconds() {
		return remainingSeconds;
	}

	public boolean checkRaidEndTimer() {
		remainingSeconds -= 1;
		return remainingSeconds <= 0;
	}

	public int getAttackerAbandonmentTime() {
		return attackerAbandonmentTime;
	}

	public void incrementAttackerAbandonmentTime() {
		attackerAbandonmentTime += 1;
	}

	public int getDefenderAbandonmentTime() {
		return defenderAbandonmentTime;
	}

	public ArrayList<EntityPlayerMP> getDeadDefenders() {
		return deadDefenders;
	}

	public long getCost() {
		return cost;
	}
}
