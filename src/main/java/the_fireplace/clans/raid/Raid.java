package the_fireplace.clans.raid;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayerMP;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.Clan;

import java.util.ArrayList;

public class Raid {
	private String raidName;
	private ArrayList<EntityPlayerMP> members, initMembers;
	private Clan target;
	private int remainingSeconds = Clans.cfg.maxRaidDuration * 60;
	private int attackerAbandonmentTime = 0, defenderAbandonmentTime = 0;
	private ArrayList<EntityPlayerMP> deadDefenders;
	private long cost;
	private boolean isActive;

	public Raid(String raidName, EntityPlayerMP starter, Clan targetClan, long raidCost){
		this.raidName = raidName;
		members = initMembers = Lists.newArrayList();
		addMember(starter);
		this.target = targetClan;
		cost = raidCost;
		RaidingParties.addRaid(raidName, this);
	}

	public void raiderVictory() {
		RaidingParties.endRaid(target);
		long reward = Clans.cfg.winRaidAmount;
		if(Clans.cfg.winRaidMultiplierClaims)
			reward *= target.getClaimCount();
		if(Clans.cfg.winRaidMultiplierPlayers)
			reward *= deadDefenders.size();
		reward -= Clans.getPaymentHandler().deductPartialAmount(reward, target.getClanId());
		long remainder = reward % initMembers.size();
		reward /= initMembers.size();
		for(EntityPlayerMP player: initMembers) {
			Clans.getPaymentHandler().ensureAccountExists(player.getUniqueID());
			Clans.getPaymentHandler().addAmount(reward, player.getUniqueID());
			if(remainder-- > 0)
				Clans.getPaymentHandler().addAmount(1, player.getUniqueID());
		}
		//TODO give the defenders their shield
		//TODO record this as a failed defense
	}

	public void defenderVictory() {
		RaidingParties.endRaid(target);
		//Reward the defenders the cost of the raid
		Clans.getPaymentHandler().addAmount(cost, target.getClanId());
		//TODO give the defenders their shield
		//TODO record this as a successful defense
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
		this.initMembers.add(player);
		RaidingParties.addRaider(player, this);
	}

	public boolean removeMember(EntityPlayerMP player) {
		boolean rm = this.members.remove(player);
		if(rm) {
			RaidingParties.removeRaider(player);
			if(this.members.isEmpty()) {
				if(isActive)
					defenderVictory();
				else
					RaidingParties.removeRaid(this);
			}
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

	public void addDeadDefender(EntityPlayerMP player) {
		if(!deadDefenders.contains(player)) {
			deadDefenders.add(player);
			if(deadDefenders.size() >= target.getOnlineMembers(player.server, player).size())
				raiderVictory();
		}
	}

	public long getCost() {
		return cost;
	}

	public boolean isActive() {
		return isActive;
	}

	public void activate() {
		isActive = true;
	}
}
