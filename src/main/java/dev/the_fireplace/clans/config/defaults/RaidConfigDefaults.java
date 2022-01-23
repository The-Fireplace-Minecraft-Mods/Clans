package dev.the_fireplace.clans.config.defaults;

import com.google.common.collect.Lists;
import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.clans.domain.config.RaidConfig;

import java.util.List;

@Implementation(name = "default")
public final class RaidConfigDefaults implements RaidConfig
{
    @Override
    public String getMaxRaidersFormula() {
        return "d";
    }

    @Override
    public String getMaxRaidDurationFormula() {
        return "30 * 60";
    }

    @Override
    public String getRaidPreparationTimeFormula() {
        return "90";
    }

    @Override
    public String getRemainingTimeToGlowFormula() {
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
    public boolean allowReclaimingTNT() {
        return false;
    }

    @Override
    public boolean isEnableRaidRollback() {
        return true;
    }

    @Override
    public boolean isEnableStealing() {
        return false;
    }

    @Override
    public double getRaidBreakSpeedMultiplier() {
        return 1;
    }

    @Override
    public List<String> getRaidItemList() {
        return Lists.newArrayList(
            "*",
            "minecraft:bedrock"
        );
    }

    @Override
    public boolean isTeleportToRaidStart() {
        return true;
    }
}
