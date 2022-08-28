package the_fireplace.clans.clan.economics;

import the_fireplace.clans.clan.land.ClanClaimCount;
import the_fireplace.clans.clan.membership.ClanMembers;
import the_fireplace.clans.economy.Economy;
import the_fireplace.clans.legacy.ClansModContainer;

import java.util.UUID;

public class ClanEconomicFunctions
{
    public static ClanEconomicFunctions get(UUID clan) {
        return new ClanEconomicFunctions(clan);
    }

    private final UUID clan;

    private ClanEconomicFunctions(UUID clan) {
        this.clan = clan;
    }

    public double getLiquidValue() {
        double value = Economy.getBalance(clan);
        ClanClaimCount.get(clan).decrementCachedClaimCount();
        long tempClaimCount = ClanClaimCount.get(clan).getClaimCount();
        for (int claimIndex = 0; claimIndex < tempClaimCount; claimIndex++) {
            value += ClanClaimCosts.get(clan).getNextClaimCost(claimIndex);
            ClanClaimCount.get(clan).decrementCachedClaimCount();
        }
        return value;
    }

    public double divideFundsAmongLeaders(double totalAmountToPay) {
        return Economy.divideFundsAmongAccounts(totalAmountToPay, ClanMembers.get(clan).getLeaders());
    }

    public double divideFundsAmongAllMembers(double totalAmountToPay) {
        return Economy.divideFundsAmongAccounts(totalAmountToPay, ClanMembers.get(clan).getMemberRanks().keySet());
    }

    public void setInitialBalance() {
        // Ensure that the starting balance of the account is 0, to prevent "free money" from the creation of a new bank account
        if (Economy.getBalance(clan) != 0) {
            Economy.deductAmount(Economy.getBalance(clan), clan);
        }
        Economy.addAmount(ClansModContainer.getConfig().getFormClanBankAmount(), clan);
    }
}