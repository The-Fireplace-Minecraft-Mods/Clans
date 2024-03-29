package the_fireplace.clans.legacy.model;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import the_fireplace.clans.clan.membership.ClanMembers;
import the_fireplace.clans.clan.metadata.ClanNames;
import the_fireplace.clans.clan.raids.ClanRaidStats;
import the_fireplace.clans.clan.raids.ClanShield;
import the_fireplace.clans.economy.Economy;
import the_fireplace.clans.legacy.ClansModContainer;
import the_fireplace.clans.legacy.cache.RaidingParties;
import the_fireplace.clans.legacy.util.FormulaParser;
import the_fireplace.clans.legacy.util.TextStyles;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;
import the_fireplace.clans.player.PlayerRaidStats;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Raid
{
    private Set<UUID> initAttackers, initDefenders = null;
    private final Map<UUID, Integer> attackers, defenders;
    private final UUID target;
    private int remainingSeconds = ClansModContainer.getConfig().getMaxRaidDuration() * 60;
    private double cost;
    private boolean isActive;

    public Raid(EntityPlayerMP starter, UUID targetClan) {
        attackers = Maps.newHashMap();
        initAttackers = Sets.newHashSet();
        defenders = Maps.newHashMap();
        addAttacker(starter);
        this.target = targetClan;
        cost = 0;
        RaidingParties.addRaid(target, this);
    }

    public void raiderVictory() {
        if (!isActive)//Triggered if raiders start then the defenders log out before the raid activates
        {
            RaidingParties.activateRaid(target);
        }
        RaidingParties.endRaid(target, true);
        double reward = FormulaParser.eval(ClansModContainer.getConfig().getWinRaidAmountFormula(), target, this, 0);
        reward -= Economy.deductPartialAmount(reward, target);
        reward /= initAttackers.size();
        for (UUID player : initAttackers) {
            Economy.addAmount(reward, player);
        }
        ClanShield.get(target).addShield(ClansModContainer.getConfig().getDefenseShield() * 60);
        ClanRaidStats.get(target).addLoss();

        for (UUID attacker : initAttackers) {
            PlayerRaidStats.incrementRaidWins(attacker);
        }
        for (UUID defender : initDefenders) {
            PlayerRaidStats.incrementRaidLosses(defender);
        }
    }

    public void defenderVictory() {
        if (!isActive)//Triggered if raiders start then log out before the raid activates
        {
            RaidingParties.activateRaid(target);
        }
        RaidingParties.endRaid(target, false);
        //Reward the defenders the cost of the raid
        Economy.addAmount(cost, target);
        ClanShield.get(target).addShield(ClansModContainer.getConfig().getDefenseShield() * 60);
        ClanRaidStats.get(target).addWin(this);

        for (UUID attacker : initAttackers) {
            PlayerRaidStats.incrementRaidLosses(attacker);
        }
        for (UUID defender : initDefenders) {
            PlayerRaidStats.incrementRaidWins(defender);
        }
    }

    /**
     * Returns the current set of raiders.
     * This does not include raiders who have died or deserted during the raid.
     */
    public Set<UUID> getAttackers() {
        return Collections.unmodifiableSet(attackers.keySet());
    }

    /**
     * Returns the set of people currently defending against the raid.
     * Clan members who have died or deserted while defending against the raid are not included in this set.
     * This will be empty if called before the raid starts, because it is not yet certain who will be defending against the raid.
     * If you want to make a guess at who will be defending, use {@link ClanMembers#getRaidDefenders()}
     */
    public Set<UUID> getDefenders() {
        return Collections.unmodifiableSet(defenders.keySet());
    }

    /**
     * Returns the initial set of raiders.
     * This includes raiders who have died or deserted during the raid.
     */
    public Set<UUID> getInitAttackers() {
        return Collections.unmodifiableSet(initAttackers);
    }

    /**
     * Returns the initial set of defenders.
     * This includes defenders who have died or deserted during the raid.
     */
    public Set<UUID> getInitDefenders() {
        return initDefenders != null ? Collections.unmodifiableSet(initDefenders) : Sets.newHashSet();
    }

    public int getAttackerCount() {
        return attackers.size();
    }

    public void addAttacker(EntityPlayer player) {
        this.attackers.put(player.getUniqueID(), 0);
        this.initAttackers.add(player.getUniqueID());
        RaidingParties.addRaider(player, this);
    }

    public boolean removeAttacker(UUID player) {
        boolean rm = this.attackers.remove(player) != null;
        if (rm) {
            RaidingParties.removeRaider(player);
            if (this.attackers.isEmpty()) {
                if (isActive) {
                    defenderVictory();
                } else {
                    RaidingParties.removeRaid(this);
                }
            }
        }
        return rm;
    }

    public UUID getTarget() {
        return target;
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }

    public boolean checkRaidEndTimer() {
        if (remainingSeconds == ClansModContainer.getConfig().getRemainingTimeToGlow() * 60) {
            for (UUID member : defenders.keySet()) {
                EntityPlayerMP d2 = ClansModContainer.getMinecraftHelper().getServer().getPlayerList().getPlayerByUUID(member);
                //noinspection ConstantConditions
                if (d2 != null) {
                    d2.sendMessage(TranslationUtil.getTranslation(d2.getUniqueID(), "clans.raid.glowing.defender", ClanNames.get(target).getName(), ClansModContainer.getConfig().getRemainingTimeToGlow(), attackers.size()).setStyle(TextStyles.YELLOW));
                }
            }
            for (UUID member : getAttackers()) {
                EntityPlayerMP m2 = ClansModContainer.getMinecraftHelper().getServer().getPlayerList().getPlayerByUUID(member);
                //noinspection ConstantConditions
                if (m2 != null) {
                    m2.sendMessage(TranslationUtil.getTranslation(m2.getUniqueID(), "clans.raid.glowing.attacker", ClanNames.get(target).getName(), ClansModContainer.getConfig().getRemainingTimeToGlow(), defenders.size()).setStyle(TextStyles.YELLOW));
                }
            }
        }
        if (remainingSeconds-- <= ClansModContainer.getConfig().getRemainingTimeToGlow() * 60) {
            for (UUID defender : defenders.keySet()) {
                EntityPlayerMP d2 = ClansModContainer.getMinecraftHelper().getServer().getPlayerList().getPlayerByUUID(defender);
                //noinspection ConstantConditions
                if (d2 != null) {
                    d2.addPotionEffect(new PotionEffect(MobEffects.GLOWING, 40));
                }
            }
        }
        return remainingSeconds <= 0;
    }

    public int getAttackerAbandonmentTime(EntityPlayer member) {
        return attackers.get(member.getUniqueID());
    }

    public void incrementAttackerAbandonmentTime(EntityPlayer member) {
        attackers.put(member.getUniqueID(), attackers.get(member.getUniqueID()) + 1);
        if (attackers.get(member.getUniqueID()) > ClansModContainer.getConfig().getMaxAttackerAbandonmentTime()) {
            removeAttacker(member.getUniqueID());
            member.sendMessage(TranslationUtil.getTranslation(member.getUniqueID(), "clans.raid.rmtimer.rm_attacker", ClanNames.get(target).getName()).setStyle(TextStyles.YELLOW));
        } else if (attackers.get(member.getUniqueID()) == 1) {
            member.sendMessage(TranslationUtil.getTranslation(member.getUniqueID(), "clans.raid.rmtimer.warn_attacker", ClanNames.get(target).getName(), ClansModContainer.getConfig().getMaxAttackerAbandonmentTime()).setStyle(TextStyles.YELLOW));
        }
    }

    public void resetAttackerAbandonmentTime(EntityPlayer member) {
        attackers.put(member.getUniqueID(), 0);
    }

    public int getDefenderAbandonmentTime(EntityPlayer member) {
        return defenders.get(member.getUniqueID());
    }

    public void incrementDefenderAbandonmentTime(EntityPlayer defender) {
        if (defender == null) {
            return;
        }
        defenders.put(defender.getUniqueID(), defenders.get(defender.getUniqueID()) + 1);
        if (defenders.get(defender.getUniqueID()) > ClansModContainer.getConfig().getMaxClanDesertionTime()) {
            removeDefender(defender.getUniqueID());
            defender.sendMessage(TranslationUtil.getTranslation(defender.getUniqueID(), "clans.raid.rmtimer.rm_defender", ClanNames.get(target).getName()).setStyle(TextStyles.YELLOW));
        } else if (defenders.get(defender.getUniqueID()) == 1) {
            defender.sendMessage(TranslationUtil.getTranslation(defender.getUniqueID(), "clans.raid.rmtimer.warn_defender", ClanNames.get(target).getName(), ClansModContainer.getConfig().getMaxClanDesertionTime()).setStyle(TextStyles.YELLOW));
        }
    }

    public void resetDefenderAbandonmentTime(EntityPlayer defender) {
        defenders.put(defender.getUniqueID(), 0);
    }

    public void setDefenders(Iterable<EntityPlayerMP> defenders) {
        for (EntityPlayerMP defender : defenders) {
            this.defenders.put(defender.getUniqueID(), 0);
        }
    }

    public void removeDefender(UUID player) {
        defenders.remove(player);
        if (defenders.size() <= 0 && isActive) {
            raiderVictory();
        }
    }

    public double getCost() {
        return cost;
    }

    public boolean isActive() {
        return isActive;
    }

    public void activate() {
        isActive = true;
        setDefenders(ClanMembers.get(target).getOnlineMemberRanks().keySet());
        initAttackers = Collections.unmodifiableSet(initAttackers);
        initDefenders = Collections.unmodifiableSet(defenders.keySet());
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    /**
     * Get the average Win-Loss Ratio for the initial set of attackers in the party
     */
    public double getPartyWlr() {
        double avgWlr = 0;
        for (UUID raider : initAttackers) {
            avgWlr += PlayerRaidStats.getRaidWLR(raider);
        }
        avgWlr /= initAttackers.size();
        return avgWlr;
    }
}
