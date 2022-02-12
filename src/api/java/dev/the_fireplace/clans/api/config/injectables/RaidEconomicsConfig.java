package dev.the_fireplace.clans.api.config.injectables;

public interface RaidEconomicsConfig
{
    String getStartRaidCostFormula();

    String getWinRaidRewardFormula();

    boolean isIncreasingRewards();

    double getMinimumWinLossRatioForWeaknessFactorReduction();

    String getIncreasedWeaknessFactorFormula();

    String getDecreasedWeaknessFactorFormula();
}
