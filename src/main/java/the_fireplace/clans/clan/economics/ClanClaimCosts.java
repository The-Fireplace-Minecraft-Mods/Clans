package the_fireplace.clans.clan.economics;

import the_fireplace.clans.clan.admin.AdminControlledClanSettings;
import the_fireplace.clans.clan.land.ClanClaims;
import the_fireplace.clans.economy.Economy;
import the_fireplace.clans.legacy.ClansModContainer;
import the_fireplace.clans.legacy.util.FormulaParser;

import java.util.UUID;

public class ClanClaimCosts {
    public static ClanClaimCosts get(UUID clan) {
        return new ClanClaimCosts(clan);
    }

    private final UUID clan;

    private ClanClaimCosts(UUID clan) {
        this.clan = clan;
    }

    /**
     * Gets the cost of one claim when the clan has a certain number of claims
     */
    public double getNextClaimCost(long currentClaimCount) {
        AdminControlledClanSettings clanSettings = AdminControlledClanSettings.get(clan);
        if (clanSettings.hasCustomClaimCost())
            return clanSettings.getCustomClaimCost();
        return shouldUseReducedClaimCost(currentClaimCount)
            ? ClansModContainer.getConfig().getReducedChunkClaimCost()
            : FormulaParser.eval(ClansModContainer.getConfig().getClaimChunkCostFormula(), clan, 0);
    }

    private boolean shouldUseReducedClaimCost(long currentClaimCount) {
        return currentClaimCount < ClansModContainer.getConfig().getReducedCostClaimCount();
    }

    public void refundClaim() {
        Economy.addAmount(getNextClaimCost(ClanClaims.get(clan).getClaimCount()), clan);
    }

    public boolean payForClaim() {
        return Economy.deductAmount(getNextClaimCost(ClanClaims.get(clan).getClaimCount()), clan);
    }
}