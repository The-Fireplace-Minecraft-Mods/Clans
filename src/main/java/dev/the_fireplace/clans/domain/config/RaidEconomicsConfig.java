package dev.the_fireplace.clans.domain.config;

public interface RaidEconomicsConfig
{
    String getStartRaidCostFormula();

    String getWinRaidRewardFormula();

    boolean isIncreasingRewards();

    double getMinimumWinLossRatioForWeaknessFactorReduction();

    String getIncreasedWeaknessFactorFormula();

    String getDecreasedWeaknessFactorFormula();
}
