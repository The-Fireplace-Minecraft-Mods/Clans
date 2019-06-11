package the_fireplace.clans.raid;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.NewClan;
import the_fireplace.clans.util.TextStyles;

import java.util.*;

public class Raid {
	private ArrayList<UUID> initAttackers;
	private HashMap<UUID, Integer> attackers, defenders;
	private NewClan target;
	private int remainingSeconds = Clans.cfg.maxRaidDuration * 60;
	private long cost;
	private boolean isActive;

	public Raid(UUID starter, NewClan targetClan){
		attackers = Maps.newHashMap();
		initAttackers = Lists.newArrayList();
		defenders = Maps.newHashMap();
		addAttacker(starter);
		this.target = targetClan;
		cost = 0;
		RaidingParties.addRaid(target, this);
	}

	public void raiderVictory() {
		RaidingParties.endRaid(target, true);
		long reward = Clans.cfg.winRaidAmount;
		if(Clans.cfg.winRaidMultiplierClaims)
			reward *= target.getClaimCount();
		if(Clans.cfg.winRaidMultiplierPlayers)
			reward *= defenders.size();
		reward -= Clans.getPaymentHandler().deductPartialAmount(reward, target.getClanId());
		long remainder = reward % initAttackers.size();
		reward /= initAttackers.size();
		for(UUID player: initAttackers) {
			Clans.getPaymentHandler().ensureAccountExists(player);
			Clans.getPaymentHandler().addAmount(reward, player);
			if(remainder-- > 0)
				Clans.getPaymentHandler().addAmount(1, player);
		}
		target.addShield(Clans.cfg.defenseShield * 60);
		target.addLoss();
	}

	public void defenderVictory() {
		RaidingParties.endRaid(target, false);
		//Reward the defenders the cost of the raid
		Clans.getPaymentHandler().addAmount(cost, target.getClanId());
		target.addShield(Clans.cfg.defenseShield * 60);
		target.addWin();
	}

	public Set<UUID> getAttackers() {
		return attackers.keySet();
	}

	public Set<UUID> getDefenders() {
		return defenders.keySet();
	}

	public ArrayList<UUID> getInitAttackers() {
		return initAttackers;
	}

	public int getAttackerCount(){
		return attackers.size();
	}

	public void addAttacker(UUID player) {
		this.attackers.put(player, 0);
		this.initAttackers.add(player);
		RaidingParties.addRaider(player, this);
	}

	public boolean removeAttacker(UUID player) {
		boolean rm = this.attackers.remove(player) != null;
		if(rm) {
			RaidingParties.removeRaider(player);
			if(this.attackers.isEmpty()) {
				if(isActive)
					defenderVictory();
				else
					RaidingParties.removeRaid(this);
			}
		}
		return rm;
	}

	public NewClan getTarget() {
		return target;
	}

	public int getRemainingSeconds() {
		return remainingSeconds;
	}

	public boolean checkRaidEndTimer() {
		if(remainingSeconds == Clans.cfg.remainingTimeToGlow * 60) {
			for(UUID defender: defenders.keySet()) {
				EntityPlayerMP d2 = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByUUID(defender);
				if(d2 != null)
					d2.sendMessage(new TextComponentTranslation("The raid against %s has %s minutes remaining! You will glow until the raid ends! There are %s raiders still alive.", target.getClanName(), Clans.cfg.remainingTimeToGlow, attackers.size()).setStyle(TextStyles.YELLOW));
			}
			for(UUID member: getAttackers()) {
				EntityPlayerMP m2 = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByUUID(member);
				if(m2 != null)
					m2.sendMessage(new TextComponentTranslation("The raid against %s has %s minutes remaining! The %s remaining defending players will glow until the raid ends!", target.getClanName(), Clans.cfg.remainingTimeToGlow, defenders.size()).setStyle(TextStyles.YELLOW));
			}
		}
		if(remainingSeconds-- <= Clans.cfg.remainingTimeToGlow * 60)
			for(UUID defender: defenders.keySet()) {
				EntityPlayerMP d = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByUUID(defender);
				if(d != null)
					d.addPotionEffect(new PotionEffect(MobEffects.GLOWING, 40));
			}
		return remainingSeconds <= 0;
	}

	public int getAttackerAbandonmentTime(UUID member) {
		return attackers.get(member);
	}

	public void incrementAttackerAbandonmentTime(EntityPlayerMP member) {
		attackers.put(member.getUniqueID(), attackers.get(member.getUniqueID()) + 1);
		if(attackers.get(member.getUniqueID()) > Clans.cfg.maxAttackerAbandonmentTime * 2) {//Times two because this is called every half second
			removeAttacker(member.getUniqueID());
			member.sendMessage(new TextComponentString("You have been removed from your raid because you spent too long outside the target's territory.").setStyle(TextStyles.YELLOW));
		} else if(attackers.get(member.getUniqueID()) == 1)
			member.sendMessage(new TextComponentString("You are not in the target clan's territory. If you stay outside it for longer than "+Clans.cfg.maxAttackerAbandonmentTime+" seconds, you will be removed from the raiding party.").setStyle(TextStyles.YELLOW));
	}

	public void resetAttackerAbandonmentTime(UUID member) {
		attackers.put(member, 0);
	}

	public int getDefenderAbandonmentTime(UUID member) {
		return defenders.get(member);
	}

	public void incrementDefenderAbandonmentTime(EntityPlayerMP defender) {
		if(defender == null)
			return;
		defenders.put(defender.getUniqueID(), attackers.get(defender.getUniqueID()) + 1);
		if(defenders.get(defender.getUniqueID()) > Clans.cfg.maxClanDesertionTime * 2)//Times two because this is called every half second
			removeDefender(defender.getUniqueID());
		else if(defenders.get(defender.getUniqueID()) == 1)
			defender.sendMessage(new TextComponentString("You have left your clan's territory. If you stay outside it for longer than "+Clans.cfg.maxClanDesertionTime+" seconds, you will be considered dead when determining if your clan wins the raid.").setStyle(TextStyles.YELLOW));
	}

	public void resetDefenderAbandonmentTime(UUID defender) {
		defenders.put(defender, 0);
	}

	public void setDefenders(Iterable<EntityPlayerMP> defenders) {
		for(EntityPlayerMP defender: defenders)
			this.defenders.put(defender.getUniqueID(), 0);
	}

	public void removeDefender(UUID player) {
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
		setDefenders(target.getOnlineMembers().keySet());
	}

	public void setCost(long cost) {
		this.cost = cost;
	}
}
