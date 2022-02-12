package dev.the_fireplace.clans.api.config.injectables;

import java.util.UUID;

public interface FormulaParser
{
    double parseForClan(String formula, UUID clanId);

    double parseForRaid(String formula, UUID targetClanId);
}
