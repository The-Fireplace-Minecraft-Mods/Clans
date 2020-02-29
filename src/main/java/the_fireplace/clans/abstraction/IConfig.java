package the_fireplace.clans.abstraction;

import the_fireplace.clans.model.Clan;

import java.util.List;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public interface IConfig {
    //General clan config
    boolean isMultipleClanLeaders();
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

    //General mod configuration
    String getLocale();
    List<String> getTolerableFakePlayers();
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
    List<String> getLockableBlocks();

    //Wilderness guard
    boolean isProtectWilderness();
    int getMinWildernessY();

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
    List<String> getRaidItemList();
    boolean isTeleportToRaidStart();

    //Costs, rewards, and multipliers
    int getFormClanCost();
    int getFormClanBankAmount();
    int getClaimChunkCost();
    int getReducedChunkClaimCost();
    int getReducedCostClaimCount();
    int getStartRaidCost();
    boolean isStartRaidMultiplier();
    int getWinRaidAmount();
    boolean isWinRaidMultiplierClaims();
    boolean isWinRaidMultiplierPlayers();
    int getClanUpkeepDays();
    int getClanUpkeepCost();
    boolean isMultiplyUpkeepClaims();
    boolean isMultiplyUpkeepMembers();
    boolean isDisbandNoUpkeep();
    boolean isIncreasingRewards();
    double getKDRThreshold();
    String getIncreasedMultiplierFormula();
    String getDecreasedMultiplierFormula();

    //Clan finance management
    boolean isLeaderWithdrawFunds();
    boolean isLeaderRecieveDisbandFunds();
    int getChargeRentDays();
    boolean isEvictNonpayers();
    boolean isEvictNonpayerAdmins();
    int getMaxRent();
    boolean isMultiplyMaxRentClaims();

    //Dynmap
    int getDynmapBorderWeight();
    double getDynmapBorderOpacity();
    double getDynmapFillOpacity();
}
