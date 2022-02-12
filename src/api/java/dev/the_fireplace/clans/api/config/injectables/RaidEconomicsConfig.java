package dev.the_fireplace.clans.api.config.injectables;

public interface RaidEconomicsConfig
{
    String getStartCostFormula();

    String getWinRewardFormula();

    boolean isIncreasingRewards();

    double getMinimumWinLossRatioForWeaknessFactorReduction();

    String getIncreasedWeaknessFactorFormula();

    String getDecreasedWeaknessFactorFormula();
}
