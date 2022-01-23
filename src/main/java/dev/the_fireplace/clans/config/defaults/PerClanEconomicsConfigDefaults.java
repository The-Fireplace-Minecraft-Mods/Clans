package dev.the_fireplace.clans.config.defaults;

import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.clans.domain.config.PerClanEconomicsConfig;

@Implementation(name = "default")
public final class PerClanEconomicsConfigDefaults implements PerClanEconomicsConfig
{
    @Override
    public String getClaimChunkCostFormula() {
        return "0";
    }

    @Override
    public int getClanUpkeepDays() {
        return 0;
    }

    @Override
    public String getClanUpkeepCostFormula() {
        return "0";
    }

    @Override
    public boolean isDisbandNoUpkeep() {
        return false;
    }

    @Override
    public boolean isLeaderWithdrawFunds() {
        return false;
    }

    @Override
    public boolean isLeaderReceiveDisbandFunds() {
        return false;
    }

    @Override
    public int getChargeRentDays() {
        return 0;
    }

    @Override
    public boolean isEvictNonpayers() {
        return false;
    }

    @Override
    public boolean isEvictNonpayerAdmins() {
        return false;
    }

    @Override
    public String getMaxRentFormula() {
        return "0";
    }

    @Override
    public String getDisbandFeeFormula() {
        return "0";
    }
}
