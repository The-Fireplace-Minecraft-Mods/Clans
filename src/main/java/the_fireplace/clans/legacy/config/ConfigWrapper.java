package the_fireplace.clans.legacy.config;

import the_fireplace.clans.legacy.abstraction.IConfig;

import java.util.Collection;

public class ConfigWrapper implements IConfig {
    @Override
    public boolean allowsMultipleClanLeaders() {
        return Config.getInstance().clan.multipleClanLeaders;
    }

    @Override
    public int getMaxNameLength() {
        return Config.getInstance().generalClan.maxNameLength;
    }

    @Override
    public boolean isAllowMultiClanMembership() {
        return Config.getInstance().generalClan.allowMultiClanMembership;
    }

    @Override
    public int getClanHomeWarmupTime() {
        return Config.getInstance().clan.clanHomeWarmupTime;
    }

    @Override
    public int getClanHomeCooldownTime() {
        return Config.getInstance().clan.clanHomeCooldownTime;
    }

    @Override
    public int getMaxClaims() {
        return Config.getInstance().clan.maxClaims;
    }

    @Override
    public boolean isMultiplyMaxClaimsByPlayers() {
        return Config.getInstance().clan.multiplyMaxClaimsByPlayers;
    }

    @Override
    public String getDefaultClanPrefix() {
        return Config.getInstance().clan.chatPrefix;
    }

    @Override
    public String getServerDefaultClan() {
        return Config.getInstance().generalClan.serverDefaultClan;
    }

    @Override
    public String getDisbandFeeFormula() {
        return Config.getInstance().clan.disbandFeeFormula;
    }

    @Override
    public boolean isClanHomeFallbackSpawnpoint() {
        return Config.getInstance().clan.clanHomeFallbackSpawn;
    }

    @Override
    public String getLocale() {
        return Config.getInstance().general.locale;
    }

    @Override
    public Collection<String> getTolerableFakePlayers() {
        return Config.getInstance().general.tolerableFakePlayers;
    }

    @Override
    public boolean isFakePlayerDump() {
        return Config.getInstance().general.fakePlayerDump;
    }

    @Override
    public boolean isForgePermissionPrecedence() {
        return Config.getInstance().general.forgePermissionPrecedence;
    }

    @Override
    public int getMinClanHomeDist() {
        return Config.getInstance().protection.minClanHomeDist;
    }

    @Override
    public double getInitialClaimSeparationMultiplier() {
        return Config.getInstance().protection.initialClaimSeparationMultiplier;
    }

    @Override
    public boolean isEnforceInitialClaimSeparation() {
        return Config.getInstance().protection.enforceInitialClaimSeparation;
    }

    @Override
    public boolean isForceConnectedClaims() {
        return Config.getInstance().protection.forceConnectedClaims;
    }

    @Override
    public String getConnectedClaimCheck() {
        return Config.getInstance().protection.connectedClaimCheck;
    }

    @Override
    public boolean isEnableBorderlands() {
        return Config.getInstance().protection.enableBorderlands;
    }

    @Override
    public boolean isPreventMobsOnClaims() {
        return Config.getInstance().protection.preventMobsOnClaims;
    }

    @Override
    public boolean isPreventMobsOnBorderlands() {
        return Config.getInstance().protection.preventMobsOnBorderlands;
    }

    @Override
    public boolean allowBuildProtection() {
        return Config.getInstance().general.allowBuildProtection;
    }

    @Override
    public boolean allowInjuryProtection() {
        return Config.getInstance().general.allowInjuryProtection;
    }

    @Override
    public boolean allowInteractionProtection() {
        return Config.getInstance().general.allowInteractProtection;
    }

    @Override
    public boolean isChainTNT() {
        return Config.getInstance().protection.chainTNT;
    }

    @Override
    public Collection<String> getLockableBlocks() {
        return Config.getInstance().protection.lockableBlocks;
    }

    @Override
    public boolean shouldProtectWilderness() {
        return Config.getInstance().protection.protectWilderness;
    }

    @Override
    public int getMinWildernessY() {
        return Config.getInstance().protection.minWildernessY;
    }

    @Override
    public Collection<String> getClaimableDimensions() {
        return Config.getInstance().protection.claimableDimensions;
    }

    @Override
    public int getMaxRaidersOffset() {
        return Config.getInstance().raid.maxRaidersOffset;
    }

    @Override
    public int getMaxRaidDuration() {
        return Config.getInstance().raid.maxRaidDuration;
    }

    @Override
    public int getRaidBufferTime() {
        return Config.getInstance().raid.raidBufferTime;
    }

    @Override
    public int getRemainingTimeToGlow() {
        return Config.getInstance().raid.remainingTimeToGlow;
    }

    @Override
    public int getMaxAttackerAbandonmentTime() {
        return Config.getInstance().raid.maxAttackerAbandonmentTime;
    }

    @Override
    public int getMaxClanDesertionTime() {
        return Config.getInstance().raid.maxClanDesertionTime;
    }

    @Override
    public int getDefenseShield() {
        return Config.getInstance().raid.defenseShield;
    }

    @Override
    public int getInitialShield() {
        return Config.getInstance().raid.initialShield;
    }

    @Override
    public boolean isNoReclaimTNT() {
        return Config.getInstance().raid.noReclaimTNT;
    }

    @Override
    public boolean isDisableRaidRollback() {
        return Config.getInstance().raid.disableRaidRollback;
    }

    @Override
    public boolean isEnableStealing() {
        return Config.getInstance().raid.enableStealing;
    }

    @Override
    public double getRaidBreakSpeedMultiplier() {
        return Config.getInstance().raid.raidBreakSpeedMultiplier;
    }

    @Override
    public Collection<String> getRaidItemList() {
        return Config.getInstance().raid.raidItemList;
    }

    @Override
    public boolean isTeleportToRaidStart() {
        return Config.getInstance().raid.teleportOnRaidStart;
    }

    @Override
    public double getFormClanCost() {
        return Config.getInstance().generalClan.formClanCost;
    }

    @Override
    public double getFormClanBankAmount() {
        return Config.getInstance().generalClan.formClanBankAmount;
    }

    @Override
    public String getClaimChunkCostFormula() {
        return Config.getInstance().clan.claimChunkCostFormula;
    }

    @Override
    public double getReducedChunkClaimCost() {
        return Config.getInstance().clan.reducedClaimChunkCost;
    }

    @Override
    public int getReducedCostClaimCount() {
        return Config.getInstance().clan.reducedCostClaimCount;
    }

    @Override
    public String getStartRaidCostFormula() {
        return Config.getInstance().raid.startRaidCostFormula;
    }

    @Override
    public String getWinRaidAmountFormula() {
        return Config.getInstance().raid.winRaidAmountFormula;
    }

    @Override
    public int getClanUpkeepDays() {
        return Config.getInstance().clan.clanUpkeepDays;
    }

    @Override
    public String getClanUpkeepCostFormula() {
        return Config.getInstance().clan.clanUpkeepCostFormula;
    }

    @Override
    public boolean isDisbandNoUpkeep() {
        return Config.getInstance().clan.disbandNoUpkeep;
    }

    @Override
    public boolean isIncreasingRewards() {
        return Config.getInstance().raid.increasingRewards;
    }

    @Override
    public double getWLRThreshold() {
        return Config.getInstance().raid.wlrThreshold;
    }

    @Override
    public String getIncreasedWeaknessFactorFormula() {
        return Config.getInstance().raid.multiplierIncreaseFormula;
    }

    @Override
    public String getDecreasedWeaknessFactorFormula() {
        return Config.getInstance().raid.multiplierDecreaseFormula;
    }

    @Override
    public boolean isLeaderWithdrawFunds() {
        return Config.getInstance().clan.leaderWithdrawFunds;
    }

    @Override
    public boolean isLeaderRecieveDisbandFunds() {
        return Config.getInstance().clan.leaderRecieveDisbandFunds;
    }

    @Override
    public int getChargeRentDays() {
        return Config.getInstance().clan.chargeRentDays;
    }

    @Override
    public boolean isEvictNonpayers() {
        return Config.getInstance().clan.evictNonpayers;
    }

    @Override
    public boolean isEvictNonpayerAdmins() {
        return Config.getInstance().clan.evictNonpayerAdmins;
    }

    @Override
    public String getMaxRentFormula() {
        return Config.getInstance().clan.maxRentFormula;
    }

    @Override
    public int getDynmapBorderWeight() {
        return Config.getInstance().dynmap.dynmapBorderWeight;
    }

    @Override
    public double getDynmapBorderOpacity() {
        return Config.getInstance().dynmap.dynmapBorderOpacity;
    }

    @Override
    public double getDynmapFillOpacity() {
        return Config.getInstance().dynmap.dynmapFillOpacity;
    }
}
