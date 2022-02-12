package dev.the_fireplace.clans.config.defaults;

import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.clans.api.config.injectables.GlobalClanConfig;

@Implementation(name = "default")
public final class GlobalClanConfigDefaults implements GlobalClanConfig
{
    @Override
    public boolean allowMultipleLeaders() {
        return true;
    }

    @Override
    public int getMaximumNameLength() {
        return 32;
    }

    @Override
    public boolean allowMultipleClanMembership() {
        return true;
    }

    @Override
    public String getNewPlayerDefaultClan() {
        return "";
    }

    @Override
    public double getFormationCost() {
        return 0;
    }

    @Override
    public double getInitialBankAmount() {
        return 0;
    }

    @Override
    public int getMinimumDistanceBetweenHomes() {
        return 320;
    }

    @Override
    public double getInitialClaimSeparationDistanceMultiplier() {
        return 1.25;
    }

    @Override
    public boolean enforceInitialClaimSeparation() {
        return true;
    }
}
