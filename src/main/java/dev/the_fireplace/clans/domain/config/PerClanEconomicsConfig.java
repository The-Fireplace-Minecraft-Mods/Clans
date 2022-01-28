package dev.the_fireplace.clans.domain.config;

public interface PerClanEconomicsConfig
{
    String getClaimChunkCostFormula();

    int getChargeUpkeepFrequencyInDays();

    String getUpkeepCostFormula();

    boolean shouldDisbandWhenUnableToPayUpkeep();

    //Clan finance management
    boolean canLeaderWithdrawFunds();

    boolean shouldLeaderReceiveDisbandFunds();

    int getChargeRentFrequencyInDays();

    boolean shouldEvictNonpayingMembers();

    boolean shouldEvictNonpayingAdmins();

    String getMaxRentFormula();

    String getDisbandFeeFormula();
}
