package dev.the_fireplace.clans.api.config.injectable;

import java.util.UUID;

public interface FormulaParser
{
    double parseForClan(String formula, UUID clanId);

    double parseForRaid(String formula, UUID targetClanId);
}
