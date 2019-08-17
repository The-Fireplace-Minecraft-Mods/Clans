package the_fireplace.clans.abstraction;

import java.util.List;

public interface IConfig {
    //General clan config
    boolean isMultipleClanLeaders();
    int getMaxNameLength();
    boolean isAllowMultiClanMembership();
    int getClanHomeWarmupTime();
    int getClanHomeCooldownTime();
    int getMaxClaims();
    boolean isMultiplyMaxClaimsByPlayers();
    boolean isShowDefaultClanInChat();

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
    boolean allowBreakProtection();
    boolean allowPlaceProtection();
    boolean allowInjuryProtection();
    boolean allowInteractionProtection();
    boolean isChainTNT();

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
    boolean disableRaidRollback();
    boolean enableStealing();
    double getRaidBreakSpeedMultiplier();

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
