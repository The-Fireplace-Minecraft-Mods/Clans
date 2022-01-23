package dev.the_fireplace.clans.config.defaults;

import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.clans.domain.config.PerClanConfig;

@Implementation(name = "default")
public final class PerClanConfigDefaults implements PerClanConfig
{
    @Override
    public int getClanHomeWarmupTime() {
        return 0;
    }

    @Override
    public int getClanHomeCooldownTime() {
        return 0;
    }

    @Override
    public String getMaxClaimCountFormula() {
        return "-1";
    }

    @Override
    public String getDefaultClanPrefix() {
        return "[%s]";
    }

    @Override
    public boolean isClanHomeFallbackSpawnpoint() {
        return true;
    }
}
