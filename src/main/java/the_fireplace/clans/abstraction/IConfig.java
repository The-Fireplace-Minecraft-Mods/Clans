package the_fireplace.clans.abstraction;

import the_fireplace.clans.model.Clan;

import java.util.Collection;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public interface IConfig {
    //General clan config
    boolean allowsMultipleClanLeaders();
    int getMaxNameLength();
    boolean isAllowMultiClanMembership();
    int getClanHomeWarmupTime();
    int getClanHomeCooldownTime();
    /**
     * This should not be directly used, use {@link Clan#getMaxClaimCount()} instead.
     */
    int getMaxClaims();
    boolean isMultiplyMaxClaimsByPlayers();
    String getDefaultClanPrefix();
    String getServerDefaultClan();
    String getDisbandFeeFormula();
    boolean isClanHomeFallbackSpawnpoint();

    //General mod configuration
    String getLocale();
    Collection<String> getTolerableFakePlayers();
    boolean isFakePlayerDump();
    boolean isForgePermissionPrecedence();

    //Clan guard
    int getMinClanHomeDist();
    double getInitialClaimSeparationMultiplier();
    boolean isEnforceInitialClaimSeparation();
    boolean isForceConnectedClaims();
    String getConnectedClaimCheck();
    boolean isEnableBorderlands();
    boolean isPreventMobsOnClaims();
    boolean isPreventMobsOnBorderlands();
    boolean allowBuildProtection();
    boolean allowInjuryProtection();
    boolean allowInteractionProtection();
    boolean isChainTNT();
    Collection<String> getLockableBlocks();

    //Wilderness guard
    boolean isProtectWilderness();
    int getMinWildernessY();
    Collection<String> getClaimableDimensions();

    //Raid configuration
    int getMaxRaidersOffset();
    int getMaxRaidDuration();
    int getRaidBufferTime();
    int getRemainingTimeToGlow();
    int getMaxAttackerAbandonmentTime();
    int getMaxClanDesertionTime();
    int getDefenseShield();
    int getInitialShield();
    boolean isNoReclaimTNT();
    boolean isDisableRaidRollback();
    boolean isEnableStealing();
    double getRaidBreakSpeedMultiplier();
    Collection<String> getRaidItemList();
    boolean isTeleportToRaidStart();

    //Costs, rewards, and multipliers
    double getFormClanCost();
    double getFormClanBankAmount();
    String getClaimChunkCostFormula();
    double getReducedChunkClaimCost();
    int getReducedCostClaimCount();
    String getStartRaidCostFormula();
    String getWinRaidAmountFormula();
    int getClanUpkeepDays();
    String getClanUpkeepCostFormula();
    boolean isDisbandNoUpkeep();
    boolean isIncreasingRewards();
    double getWLRThreshold();
    String getIncreasedMultiplierFormula();
    String getDecreasedMultiplierFormula();

    //Clan finance management
    boolean isLeaderWithdrawFunds();
    boolean isLeaderRecieveDisbandFunds();
    int getChargeRentDays();
    boolean isEvictNonpayers();
    boolean isEvictNonpayerAdmins();
    String getMaxRentFormula();

    //Dynmap
    int getDynmapBorderWeight();
    double getDynmapBorderOpacity();
    double getDynmapFillOpacity();
}
