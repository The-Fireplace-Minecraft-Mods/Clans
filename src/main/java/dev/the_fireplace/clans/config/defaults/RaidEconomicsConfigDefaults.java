package dev.the_fireplace.clans.config.defaults;

import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.clans.domain.config.RaidEconomicsConfig;

@Implementation(name = "default")
public final class RaidEconomicsConfigDefaults implements RaidEconomicsConfig
{
    @Override
    public String getStartRaidCostFormula() {
        return "0";
    }

    @Override
    public String getWinRaidAmountFormula() {
        return "0";
    }

    @Override
    public boolean isIncreasingRewards() {
        return true;
    }

    @Override
    public double getWLRThreshold() {
        return 0.66;
    }

    @Override
    public String getIncreasedWeaknessFactorFormula() {
        return "m^1.05";
    }

    @Override
    public String getDecreasedWeaknessFactorFormula() {
        return "m^0.95";
    }
}
