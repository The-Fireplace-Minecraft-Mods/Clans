package dev.the_fireplace.clans.domain.config;

public interface RaidEconomicsConfig
{
    String getStartRaidCostFormula();

    String getWinRaidAmountFormula();

    boolean isIncreasingRewards();

    double getWLRThreshold();

    String getIncreasedWeaknessFactorFormula();

    String getDecreasedWeaknessFactorFormula();
}
