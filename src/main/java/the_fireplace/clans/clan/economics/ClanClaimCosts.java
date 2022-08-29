package the_fireplace.clans.clan.economics;

import the_fireplace.clans.clan.admin.AdminControlledClanSettings;
import the_fireplace.clans.clan.land.ClanClaimCount;
import the_fireplace.clans.economy.Economy;
import the_fireplace.clans.legacy.ClansModContainer;
import the_fireplace.clans.legacy.util.FormulaParser;

import java.util.UUID;

public class ClanClaimCosts
{
    public static ClanClaimCosts get(UUID clan) {
        return new ClanClaimCosts(clan);
    }

    private final UUID clan;

    private ClanClaimCosts(UUID clan) {
        this.clan = clan;
    }

    /**
     * Gets the cost of the given claim number (e.g. the first claim is claim number 1, the second is 2, etc.)
     */
    public double getClaimCost(long claimNumber) {
        AdminControlledClanSettings clanSettings = AdminControlledClanSettings.get(clan);
        if (clanSettings.hasCustomClaimCost()) {
            return clanSettings.getCustomClaimCost();
        }

        FormulaParser.Overrides overrides = new FormulaParser.Overrides().setClaimCount(claimNumber);

        return shouldUseReducedClaimCost(claimNumber)
            ? ClansModContainer.getConfig().getReducedChunkClaimCost()
            : FormulaParser.eval(ClansModContainer.getConfig().getClaimChunkCostFormula(), clan, null, 0, overrides);
    }

    private boolean shouldUseReducedClaimCost(long claimNumber) {
        return claimNumber <= ClansModContainer.getConfig().getReducedCostClaimCount();
    }

    public void refundClaimBeforeOwnershipChange() {
        long claimNumber = ClanClaimCount.get(clan).getClaimCount();
        double claimCost = getClaimCost(claimNumber);
        Economy.addAmount(claimCost, clan);
    }

    public boolean attemptPayForClaimBeforeClaiming() {
        long claimNumber = ClanClaimCount.get(clan).getClaimCount() + 1;
        double claimCost = getClaimCost(claimNumber);
        return Economy.deductAmount(claimCost, clan);
    }
}