package dev.the_fireplace.clans.config.defaults;

import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.clans.api.config.injectables.RaidConfig;

@Implementation(name = "default")
public final class RaidConfigDefaults implements RaidConfig
{
    @Override
    public String getMaxRaidersFormula() {
        return "d";
    }

    @Override
    public String getMaxDurationFormula() {
        return "30 * 60";
    }

    @Override
    public String getPreparationTimeFormula() {
        return "90";
    }

    @Override
    public String getDefenderGlowTimeFormula() {
        return "10 * 60";
    }

    @Override
    public String getMaxAttackerAbandonmentTimeFormula() {
        return "30";
    }

    @Override
    public String getMaxDefenderDesertionTimeFormula() {
        return "60";
    }

    @Override
    public String getShieldAfterRaidFormula() {
        return "5 * 24";
    }

    @Override
    public int getInitialShield() {
        return 3 * 24;
    }

    @Override
    public boolean preventReclaimingTNT() {
        return false;
    }

    @Override
    public boolean isEnableRollback() {
        return true;
    }

    @Override
    public boolean isEnableStealing() {
        return false;
    }

    @Override
    public double getBreakSpeedMultiplier() {
        return 1;
    }

    @Override
    public boolean isTeleportToRaidStart() {
        return true;
    }
}
