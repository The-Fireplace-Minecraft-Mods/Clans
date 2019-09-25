package the_fireplace.clans.model;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import the_fireplace.clans.Clans;
import the_fireplace.clans.cache.RaidingParties;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Raid {
	private Set<UUID> initAttackers;
	private Map<UUID, Integer> attackers, defenders;
	private Clan target;
	private int remainingSeconds = Clans.getConfig().getMaxRaidDuration() * 60;
	private long cost;
	private boolean isActive;

	public Raid(EntityPlayerMP starter, Clan targetClan){
		attackers = Maps.newHashMap();
		initAttackers = Sets.newHashSet();
		defenders = Maps.newHashMap();
		addAttacker(starter);
		this.target = targetClan;
		cost = 0;
		RaidingParties.addRaid(target, this);
	}

	public void raiderVictory() {
		RaidingParties.endRaid(target, true);
		long reward = Clans.getConfig().getWinRaidAmount();
		if(Clans.getConfig().isWinRaidMultiplierClaims())
			reward *= target.getClaimCount();
		if(Clans.getConfig().isWinRaidMultiplierPlayers())
			reward *= defenders.size();
		reward -= Clans.getPaymentHandler().deductPartialAmount(reward, target.getId());
		long remainder = reward % initAttackers.size();
		reward /= initAttackers.size();
		for(UUID player: initAttackers) {
			Clans.getPaymentHandler().ensureAccountExists(player);
			Clans.getPaymentHandler().addAmount(reward, player);
			if(remainder-- > 0)
				Clans.getPaymentHandler().addAmount(1, player);
		}
		target.addShield(Clans.getConfig().getDefenseShield() * 60);
		target.addLoss();
	}

	public void defenderVictory() {
		RaidingParties.endRaid(target, false);
		//Reward the defenders the cost of the raid
		Clans.getPaymentHandler().addAmount(cost, target.getId());
		target.addShield(Clans.getConfig().getDefenseShield() * 60);
		target.addWin();
	}

	public Set<UUID> getAttackers() {
		return Collections.unmodifiableSet(attackers.keySet());
	}

	public Set<UUID> getDefenders() {
		return Collections.unmodifiableSet(defenders.keySet());
	}

	public Set<UUID> getInitAttackers() {
		return Collections.unmodifiableSet(initAttackers);
	}

	public int getAttackerCount(){
		return attackers.size();
	}

	public void addAttacker(EntityPlayer player) {
		this.attackers.put(player.getUniqueID(), 0);
		this.initAttackers.add(player.getUniqueID());
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

	public Clan getTarget() {
		return target;
	}

	public int getRemainingSeconds() {
		return remainingSeconds;
	}

	public boolean checkRaidEndTimer() {
		if(remainingSeconds == Clans.getConfig().getRemainingTimeToGlow() * 60) {
			for(UUID member: defenders.keySet()) {
				EntityPlayerMP d2 = Clans.getMinecraftHelper().getServer().getPlayerList().getPlayerByUUID(member);
				//noinspection ConstantConditions
				if (d2 != null)
					d2.sendMessage(TranslationUtil.getTranslation(d2.getUniqueID(), "clans.raid.glowing.defender", target.getName(), Clans.getConfig().getRemainingTimeToGlow(), attackers.size()).setStyle(TextStyles.YELLOW));
			}
			for(UUID member: getAttackers()) {
				EntityPlayerMP m2 = Clans.getMinecraftHelper().getServer().getPlayerList().getPlayerByUUID(member);
				//noinspection ConstantConditions
				if(m2 != null)
					m2.sendMessage(TranslationUtil.getTranslation(m2.getUniqueID(), "clans.raid.glowing.attacker", target.getName(), Clans.getConfig().getRemainingTimeToGlow(), defenders.size()).setStyle(TextStyles.YELLOW));
			}
		}
		if(remainingSeconds-- <= Clans.getConfig().getRemainingTimeToGlow() * 60)
			for(UUID defender: defenders.keySet()) {
				EntityPlayerMP d2 = Clans.getMinecraftHelper().getServer().getPlayerList().getPlayerByUUID(defender);
				//noinspection ConstantConditions
				if(d2 != null)
					d2.addPotionEffect(new PotionEffect(MobEffects.GLOWING, 40));
			}
		return remainingSeconds <= 0;
	}

	public int getAttackerAbandonmentTime(EntityPlayer member) {
		return attackers.get(member.getUniqueID());
	}

	public void incrementAttackerAbandonmentTime(EntityPlayer member) {
		attackers.put(member.getUniqueID(), attackers.get(member.getUniqueID()) + 1);
		if(attackers.get(member.getUniqueID()) > Clans.getConfig().getMaxAttackerAbandonmentTime()) {
			removeAttacker(member.getUniqueID());
			member.sendMessage(TranslationUtil.getTranslation(member.getUniqueID(), "clans.raid.rmtimer.rm_attacker", target.getName()).setStyle(TextStyles.YELLOW));
		} else if(attackers.get(member.getUniqueID()) == 1)
			member.sendMessage(TranslationUtil.getTranslation(member.getUniqueID(), "clans.raid.rmtimer.warn_attacker", target.getName(), Clans.getConfig().getMaxAttackerAbandonmentTime()).setStyle(TextStyles.YELLOW));
	}

	public void resetAttackerAbandonmentTime(EntityPlayer member) {
		attackers.put(member.getUniqueID(), 0);
	}

	public int getDefenderAbandonmentTime(EntityPlayer member) {
		return defenders.get(member.getUniqueID());
	}

	public void incrementDefenderAbandonmentTime(EntityPlayer defender) {
		if(defender == null)
			return;
		defenders.put(defender.getUniqueID(), defenders.get(defender.getUniqueID()) + 1);
		if(defenders.get(defender.getUniqueID()) > Clans.getConfig().getMaxClanDesertionTime()) {
			removeDefender(defender.getUniqueID());
			defender.sendMessage(TranslationUtil.getTranslation(defender.getUniqueID(), "clans.raid.rmtimer.rm_defender", target.getName()).setStyle(TextStyles.YELLOW));
		} else if(defenders.get(defender.getUniqueID()) == 1)
			defender.sendMessage(TranslationUtil.getTranslation(defender.getUniqueID(), "clans.raid.rmtimer.warn_defender", target.getName(), Clans.getConfig().getMaxClanDesertionTime()).setStyle(TextStyles.YELLOW));
	}

	public void resetDefenderAbandonmentTime(EntityPlayer defender) {
		defenders.put(defender.getUniqueID(), 0);
	}

	public void setDefenders(Iterable<EntityPlayerMP> defenders) {
		for(EntityPlayerMP defender: defenders)
			this.defenders.put(defender.getUniqueID(), 0);
	}

	public void removeDefender(UUID player) {
		defenders.remove(player);
		if(defenders.size() <= 0 && isActive)
			raiderVictory();
	}

	public long getCost() {
		return cost;
	}

	public boolean isActive() {
		return isActive;
	}

	public void activate() {
		isActive = true;
		setDefenders(target.getOnlineMembers().keySet());
		initAttackers = Collections.unmodifiableSet(initAttackers);
	}

	public void setCost(long cost) {
		this.cost = cost;
	}
}
