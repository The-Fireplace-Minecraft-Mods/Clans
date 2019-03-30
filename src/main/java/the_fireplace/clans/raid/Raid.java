package the_fireplace.clans.raid;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.NewClan;
import the_fireplace.clans.util.TextStyles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class Raid {
	private ArrayList<UUID> initMembers;
	private HashMap<UUID, Integer> members, defenders;
	private NewClan target;
	private int remainingSeconds = Clans.cfg.maxRaidDuration * 60;
	private long cost;
	private boolean isActive;

	public Raid(EntityPlayerMP starter, NewClan targetClan){
		members = Maps.newHashMap();
		initMembers = Lists.newArrayList();
		defenders = Maps.newHashMap();
		addMember(starter);
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
		long remainder = reward % initMembers.size();
		reward /= initMembers.size();
		for(UUID player: initMembers) {
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

	public Set<UUID> getMembers() {
		return members.keySet();
	}

	public ArrayList<UUID> getInitMembers() {
		return initMembers;
	}

	public int getMemberCount(){
		return members.size();
	}

	public void addMember(EntityPlayerMP player) {
		this.members.put(player.getUniqueID(), 0);
		this.initMembers.add(player.getUniqueID());
		RaidingParties.addRaider(player, this);
	}

	public boolean removeMember(EntityPlayerMP player) {
		boolean rm = this.members.remove(player.getUniqueID()) != null;
		if(rm) {
			RaidingParties.removeRaider(player.getUniqueID());
			if(this.members.isEmpty()) {
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
			for(UUID member: defenders.keySet())
				FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(member).sendMessage(new TextComponentTranslation("The raid against %s has %s minutes remaining! You will glow until the raid ends! There are %s raiders still alive.", target.getClanName(), Clans.cfg.remainingTimeToGlow, members.size()).setStyle(TextStyles.YELLOW));
			for(UUID member: getMembers())
				FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(member).sendMessage(new TextComponentTranslation("The raid against %s has %s minutes remaining! The %s remaining defending players will glow until the raid ends!", target.getClanName(), Clans.cfg.remainingTimeToGlow, defenders.size()).setStyle(TextStyles.YELLOW));
		}
		if(remainingSeconds-- <= Clans.cfg.remainingTimeToGlow * 60)
			for(UUID defender: defenders.keySet())
				FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(defender).addPotionEffect(new PotionEffect(MobEffects.GLOWING, 40));
		return remainingSeconds <= 0;
	}

	public int getAttackerAbandonmentTime(EntityPlayerMP member) {
		return members.get(member.getUniqueID());
	}

	public void incrementAttackerAbandonmentTime(EntityPlayerMP member) {
		members.put(member.getUniqueID(), members.get(member.getUniqueID()) + 1);
		if(members.get(member.getUniqueID()) > Clans.cfg.maxAttackerAbandonmentTime * 2) {//Times two because this is called every half second
			removeMember(member);
			member.sendMessage(new TextComponentString("You have been removed from your raid because you spent too long outside the target's territory.").setStyle(TextStyles.YELLOW));
		} else if(members.get(member.getUniqueID()) == 1)
			member.sendMessage(new TextComponentTranslation("You are not in the target clan's territory. If you stay outside it for longer than %s seconds, you will be removed from the raiding party.", Clans.cfg.maxAttackerAbandonmentTime).setStyle(TextStyles.YELLOW));
	}

	public void resetAttackerAbandonmentTime(EntityPlayerMP member) {
		members.put(member.getUniqueID(), 0);
	}

	public int getDefenderAbandonmentTime(EntityPlayerMP member) {
		return defenders.get(member.getUniqueID());
	}

	public void incrementDefenderAbandonmentTime(EntityPlayerMP defender) {
		if(defender == null)
			return;
		defenders.put(defender.getUniqueID(), members.get(defender.getUniqueID()) + 1);
		if(defenders.get(defender.getUniqueID()) > Clans.cfg.maxClanDesertionTime * 2)//Times two because this is called every half second
			removeDefender(defender);
		else if(defenders.get(defender.getUniqueID()) == 1)
			defender.sendMessage(new TextComponentTranslation("You have left your clan's territory. If you stay outside it for longer than %s seconds, you will be considered dead when determining if your clan wins the raid.", Clans.cfg.maxClanDesertionTime).setStyle(TextStyles.YELLOW));
	}

	public void resetDefenderAbandonmentTime(EntityPlayerMP defender) {
		defenders.put(defender.getUniqueID(), 0);
	}

	public void setDefenders(Iterable<EntityPlayerMP> defenders) {
		for(EntityPlayerMP defender: defenders)
			this.defenders.put(defender.getUniqueID(), 0);
	}

	public void removeDefender(EntityPlayerMP player) {
		defenders.remove(player.getUniqueID());
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
		setDefenders(target.getOnlineMembers().keySet());//TODO test that this works
	}

	public void setCost(long cost) {
		this.cost = cost;
	}
}
