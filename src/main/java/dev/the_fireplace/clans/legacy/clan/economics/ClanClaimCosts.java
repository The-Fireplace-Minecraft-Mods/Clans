package dev.the_fireplace.clans.legacy.clan.economics;

import dev.the_fireplace.clans.economy.Economy;
import dev.the_fireplace.clans.legacy.ClansModContainer;
import dev.the_fireplace.clans.legacy.clan.admin.AdminControlledClanSettings;
import dev.the_fireplace.clans.legacy.clan.land.ClanClaimCount;
import dev.the_fireplace.clans.legacy.util.FormulaParser;

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
     * Gets the cost of one claim when the clan has a certain number of claims
     */
    public double getNextClaimCost(long currentClaimCount) {
        AdminControlledClanSettings clanSettings = AdminControlledClanSettings.get(clan);
        if (clanSettings.hasCustomClaimCost()) {
            return clanSettings.getCustomClaimCost();
        }
        return shouldUseReducedClaimCost(currentClaimCount)
            ? ClansModContainer.getConfig().getReducedChunkClaimCost()
            : FormulaParser.eval(ClansModContainer.getConfig().getClaimChunkCostFormula(), clan, 0);
    }

    private boolean shouldUseReducedClaimCost(long currentClaimCount) {
        return currentClaimCount < ClansModContainer.getConfig().getReducedCostClaimCount();
    }

    public void refundClaim() {
        Economy.addAmount(getNextClaimCost(ClanClaimCount.get(clan).getClaimCount()), clan);
    }

    public boolean payForClaim() {
        return Economy.deductAmount(getNextClaimCost(ClanClaimCount.get(clan).getClaimCount()), clan);
    }
}