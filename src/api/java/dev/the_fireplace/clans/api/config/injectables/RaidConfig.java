package dev.the_fireplace.clans.api.config.injectables;

import java.util.List;

public interface RaidConfig
{
    String getMaxRaidersFormula();

    String getMaxRaidDurationFormula();

    String getRaidPreparationTimeFormula();

    String getRemainingTimeToGlowFormula();

    String getMaxAttackerAbandonmentTimeFormula();

    String getMaxDefenderDesertionTimeFormula();

    String getShieldAfterRaidFormula();

    int getInitialShield();

    boolean allowReclaimingTNT();

    boolean isEnableRaidRollback();

    boolean isEnableStealing();

    double getRaidBreakSpeedMultiplier();

    List<String> getRaidItemList();

    boolean isTeleportToRaidStart();
}
