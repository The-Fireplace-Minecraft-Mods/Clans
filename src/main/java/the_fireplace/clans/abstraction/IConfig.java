package the_fireplace.clans.abstraction;

public interface IConfig {
    boolean isMultipleClanLeaders();

    int getMaxNameLength();

    boolean isAllowMultiClanMembership();

    int getClanHomeWarmupTime();

    int getClanHomeCooldownTime();

    int getMaxClaims();

    boolean isMultiplyMaxClaimsByPlayers();

    boolean isShowDefaultClanInChat();

    String getLocale();

    String[] getTolerableFakePlayers();

    boolean isFakePlayerDump();

    boolean isForgePermissionPrecedence();

    int getMinClanHomeDist();

    double getInitialClaimSeparationMultiplier();

    boolean isEnforceInitialClaimSeparation();

    boolean isForceConnectedClaims();

    String getConnectedClaimCheck();

    boolean isEnableBorderlands();

    boolean isPreventMobsOnClaims();

    boolean isPreventMobsOnBorderlands();

    boolean isProtectWilderness();

    int getMinWildernessY();

    boolean isChainTNT();

    int getMaxRaidersOffset();

    int getMaxRaidDuration();

    int getRaidBufferTime();

    int getRemainingTimeToGlow();

    int getMaxAttackerAbandonmentTime();

    int getMaxClanDesertionTime();

    int getDefenseShield();

    int getInitialShield();

    boolean isNoReclaimTNT();

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

    boolean disableRaidRollback();

    int getClanUpkeepDays();

    int getClanUpkeepCost();

    boolean isMultiplyUpkeepClaims();

    boolean isMultiplyUpkeepMembers();

    boolean isDisbandNoUpkeep();

    boolean isLeaderWithdrawFunds();

    boolean isLeaderRecieveDisbandFunds();

    int getChargeRentDays();

    boolean isEvictNonpayers();

    boolean isEvictNonpayerAdmins();

    int getMaxRent();

    boolean isMultiplyMaxRentClaims();

    int getDynmapBorderWeight();

    double getDynmapBorderOpacity();

    double getDynmapFillOpacity();

    double getRaidBreakSpeedMultiplier();

    boolean allowBreakProtection();

    boolean allowPlaceProtection();

    boolean allowInjuryProtection();

    boolean allowInteractionProtection();
}
