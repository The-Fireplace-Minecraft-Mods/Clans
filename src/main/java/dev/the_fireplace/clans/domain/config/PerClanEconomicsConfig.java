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

    boolean shouldKickNonpayingMembers();

    boolean shouldKickNonpayingAdmins();

    String getMaxRentFormula();

    String getDisbandFeeFormula();
}
