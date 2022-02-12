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
    public int getChargeUpkeepFrequencyInDays() {
        return 0;
    }

    @Override
    public String getUpkeepCostFormula() {
        return "0";
    }

    @Override
    public boolean shouldDisbandWhenUnableToPayUpkeep() {
        return false;
    }

    @Override
    public boolean canLeaderWithdrawFunds() {
        return false;
    }

    @Override
    public boolean shouldLeaderReceiveDisbandFunds() {
        return false;
    }

    @Override
    public int getChargeRentFrequencyInDays() {
        return 0;
    }

    @Override
    public boolean shouldKickNonpayingMembers() {
        return false;
    }

    @Override
    public boolean shouldKickNonpayingAdmins() {
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
