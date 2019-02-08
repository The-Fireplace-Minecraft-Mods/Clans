package the_fireplace.clans.raid;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.util.MinecraftColors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class Raid {
	private String raidName;
	private ArrayList<EntityPlayerMP> initMembers;
	private HashMap<EntityPlayerMP, Integer> members, defenders;
	private Clan target;
	private int remainingSeconds = Clans.cfg.maxRaidDuration * 60;
	private long cost;
	private boolean isActive;

	public Raid(String raidName, EntityPlayerMP starter, Clan targetClan, long raidCost){
		this.raidName = raidName;
		members = Maps.newHashMap();
		initMembers = Lists.newArrayList();
		defenders = Maps.newHashMap();
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
			reward *= defenders.size();
		reward -= Clans.getPaymentHandler().deductPartialAmount(reward, target.getClanId());
		long remainder = reward % initMembers.size();
		reward /= initMembers.size();
		for(EntityPlayerMP player: initMembers) {
			Clans.getPaymentHandler().ensureAccountExists(player.getUniqueID());
			Clans.getPaymentHandler().addAmount(reward, player.getUniqueID());
			if(remainder-- > 0)
				Clans.getPaymentHandler().addAmount(1, player.getUniqueID());
		}
		target.addShield(Clans.cfg.defenseShield * 60);
		target.addLoss();
	}

	public void defenderVictory() {
		RaidingParties.endRaid(target);
		//Reward the defenders the cost of the raid
		Clans.getPaymentHandler().addAmount(cost, target.getClanId());
		target.addShield(Clans.cfg.defenseShield * 60);
		target.addWin();
	}

	public Set<EntityPlayerMP> getMembers() {
		return members.keySet();
	}

	public String getRaidName() {
		return raidName;
	}

	public int getMemberCount(){
		return members.size();
	}

	public void addMember(EntityPlayerMP player) {
		this.members.put(player, 0);
		this.initMembers.add(player);
		RaidingParties.addRaider(player, this);
	}

	public boolean removeMember(EntityPlayerMP player) {
		boolean rm = this.members.remove(player) != null;
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

	public int getAttackerAbandonmentTime(EntityPlayerMP member) {
		return members.get(member);
	}

	public void incrementAttackerAbandonmentTime(EntityPlayerMP member) {
		members.put(member, members.get(member) + 1);
		if(members.get(member) > Clans.cfg.maxAttackerAbandonmentTime * 2) {//Times two because this is called every half second
			removeMember(member);
			member.sendMessage(new TextComponentString(MinecraftColors.YELLOW + "You have been removed from your raid because you spent too long outside the target's territory."));
		} else if(members.get(member) == 1)
			member.sendMessage(new TextComponentString(MinecraftColors.YELLOW + "You are not in the target clan's territory. If you stay outside it for longer than "+Clans.cfg.maxAttackerAbandonmentTime+" seconds, you will be removed from the raiding party."));
	}

	public void resetAttackerAbandonmentTime(EntityPlayerMP member) {
		members.put(member, 0);
	}

	public int getDefenderAbandonmentTime(EntityPlayerMP member) {
		return defenders.get(member);
	}

	public void incrementDefenderAbandonmentTime(EntityPlayerMP defender) {
		defenders.put(defender, members.get(defender) + 1);
		if(defenders.get(defender) > Clans.cfg.maxClanDesertionTime * 2)//Times two because this is called every half second
			removeDefender(defender);
		else if(defenders.get(defender) == 1)
			defender.sendMessage(new TextComponentString(MinecraftColors.YELLOW + "You have left your clan's territory. If you stay outside it for longer than "+Clans.cfg.maxClanDesertionTime+" seconds, you will be considered dead when determining if your clan wins the raid."));
	}

	public void resetDefenderAbandonmentTime(EntityPlayerMP defender) {
		defenders.put(defender, 0);
	}

	public void setDefenders(Iterable<EntityPlayerMP> defenders) {
		for(EntityPlayerMP defender: defenders)
			this.defenders.put(defender, 0);
	}

	public void removeDefender(EntityPlayerMP player) {
		defenders.remove(player);
		if(defenders.size() <= 0)
			raiderVictory();
	}

	public long getCost() {
		return cost;
	}

	public boolean isActive() {
		return isActive;
	}

	void activate() {
		isActive = true;
		setDefenders(target.getOnlineMembers(FMLCommonHandler.instance().getMinecraftServerInstance(), null).keySet());//TODO test that this works
	}
}
