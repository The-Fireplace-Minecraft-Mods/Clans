package dev.the_fireplace.clans.config.defaults;

import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.clans.domain.config.PerClanProtectionConfig;

@Implementation(name = "default")
public final class PerClanProtectionConfigDefaults implements PerClanProtectionConfig
{
    @Override
    public boolean isForceConnectedClaims() {
        return true;
    }

    @Override
    public boolean isEnableBorderlands() {
        return true;
    }

    @Override
    public boolean isPreventMobsOnClaims() {
        return true;
    }

    @Override
    public boolean isPreventMobsOnBorderlands() {
        return true;
    }

    @Override
    public boolean isAllowTntChainingOnClaims() {
        return true;
    }
}
