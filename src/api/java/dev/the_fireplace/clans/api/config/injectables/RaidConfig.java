package dev.the_fireplace.clans.api.config.injectables;

public interface RaidConfig
{
    String getMaxRaidersFormula();

    String getMaxDurationFormula();

    String getPreparationTimeFormula();

    String getDefenderGlowTimeFormula();

    String getMaxAttackerAbandonmentTimeFormula();

    String getMaxDefenderDesertionTimeFormula();

    String getShieldAfterRaidFormula();

    int getInitialShield();

    boolean preventReclaimingTNT();

    boolean isEnableRollback();

    boolean isEnableStealing();

    double getBreakSpeedMultiplier();

    boolean isTeleportToRaidStart();
}
