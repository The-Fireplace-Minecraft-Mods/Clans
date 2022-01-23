package dev.the_fireplace.clans.domain.config;

public interface PerClanEconomicsConfig
{
    String getClaimChunkCostFormula();

    int getClanUpkeepDays();

    String getClanUpkeepCostFormula();

    boolean isDisbandNoUpkeep();

    //Clan finance management
    boolean isLeaderWithdrawFunds();

    boolean isLeaderReceiveDisbandFunds();

    int getChargeRentDays();

    boolean isEvictNonpayers();

    boolean isEvictNonpayerAdmins();

    String getMaxRentFormula();

    String getDisbandFeeFormula();
}
