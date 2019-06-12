package the_fireplace.clans.raid;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.fml.common.FMLCommonHandler;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class Raid {
	private ArrayList<UUID> initAttackers;
	private HashMap<UUID, Integer> attackers, defenders;
	private Clan target;
	private int remainingSeconds = Clans.cfg.maxRaidDuration * 60;
	private long cost;
	private boolean isActive;

	public Raid(EntityPlayerMP starter, Clan targetClan){
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

	public void addAttacker(EntityPlayerMP player) {
		this.attackers.put(player.getUniqueID(), 0);
		this.initAttackers.add(player.getUniqueID());
		RaidingParties.addRaider(player, this);
	}

	public boolean removeAttacker(EntityPlayerMP player) {
		boolean rm = this.attackers.remove(player.getUniqueID()) != null;
		if(rm) {
			RaidingParties.removeRaider(player.getUniqueID());
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
		if(remainingSeconds == Clans.cfg.remainingTimeToGlow * 60) {
			for(UUID member: defenders.keySet()) {
				EntityPlayerMP d2 = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(member);
				//noinspection ConstantConditions
				if (d2 != null)
					d2.sendMessage(TranslationUtil.getTranslation(d2.getUniqueID(), "clans.raid.glowing.defender", target.getClanName(), Clans.cfg.remainingTimeToGlow, attackers.size()).setStyle(TextStyles.YELLOW));
			}
			for(UUID member: getAttackers()) {
				EntityPlayerMP m2 = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(member);
				//noinspection ConstantConditions
				if(m2 != null)
					m2.sendMessage(TranslationUtil.getTranslation(m2.getUniqueID(), "clans.raid.glowing.attacker", target.getClanName(), Clans.cfg.remainingTimeToGlow, defenders.size()).setStyle(TextStyles.YELLOW));
			}
		}
		if(remainingSeconds-- <= Clans.cfg.remainingTimeToGlow * 60)
			for(UUID defender: defenders.keySet()) {
				EntityPlayerMP d2 = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(defender);
				//noinspection ConstantConditions
				if(d2 != null)
					d2.addPotionEffect(new PotionEffect(MobEffects.GLOWING, 40));
			}
		return remainingSeconds <= 0;
	}

	public int getAttackerAbandonmentTime(EntityPlayerMP member) {
		return attackers.get(member.getUniqueID());
	}

	public void incrementAttackerAbandonmentTime(EntityPlayerMP member) {
		attackers.put(member.getUniqueID(), attackers.get(member.getUniqueID()) + 1);
		if(attackers.get(member.getUniqueID()) > Clans.cfg.maxAttackerAbandonmentTime * 2) {//Times two because this is called every half second
			removeAttacker(member);
			member.sendMessage(TranslationUtil.getTranslation(member.getUniqueID(), "clans.raid.rmtimer.rm_attacker", target.getClanName()).setStyle(TextStyles.YELLOW));
		} else if(attackers.get(member.getUniqueID()) == 1)
			member.sendMessage(TranslationUtil.getTranslation(member.getUniqueID(), "clans.raid.rmtimer.warn_attacker", target.getClanName(), Clans.cfg.maxAttackerAbandonmentTime).setStyle(TextStyles.YELLOW));
	}

	public void resetAttackerAbandonmentTime(EntityPlayerMP member) {
		attackers.put(member.getUniqueID(), 0);
	}

	public int getDefenderAbandonmentTime(EntityPlayerMP member) {
		return defenders.get(member.getUniqueID());
	}

	public void incrementDefenderAbandonmentTime(EntityPlayerMP defender) {
		if(defender == null)
			return;
		defenders.put(defender.getUniqueID(), attackers.get(defender.getUniqueID()) + 1);
		if(defenders.get(defender.getUniqueID()) > Clans.cfg.maxClanDesertionTime * 2) {//Times two because this is called every half second
			removeDefender(defender);
			defender.sendMessage(TranslationUtil.getTranslation(defender.getUniqueID(), "clans.raid.rmtimer.rm_defender", Clans.cfg.maxClanDesertionTime).setStyle(TextStyles.YELLOW));
		} else if(defenders.get(defender.getUniqueID()) == 1)
			defender.sendMessage(TranslationUtil.getTranslation(defender.getUniqueID(), "clans.raid.rmtimer.warn_defender", Clans.cfg.maxClanDesertionTime).setStyle(TextStyles.YELLOW));
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
