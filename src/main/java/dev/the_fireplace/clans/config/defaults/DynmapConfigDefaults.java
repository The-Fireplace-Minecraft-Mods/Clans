package dev.the_fireplace.clans.config.defaults;

import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.clans.api.config.injectables.DynmapConfig;

@Implementation(name = "default")
public final class DynmapConfigDefaults implements DynmapConfig
{
    @Override
    public int getDynmapBorderWeight() {
        return 0;
    }

    @Override
    public double getDynmapBorderOpacity() {
        return 0.9;
    }

    @Override
    public double getDynmapFillOpacity() {
        return 0.75;
    }

    @Override
    public int getMaxDisplayedConnectedClaims() {
        return Integer.MAX_VALUE;
    }
}
