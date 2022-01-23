package dev.the_fireplace.clans.domain.config;

public interface PerClanConfig
{
    int getClanHomeWarmupTime();

    int getClanHomeCooldownTime();

    String getMaxClaimCountFormula();

    String getDefaultClanPrefix();

    boolean isClanHomeFallbackSpawnpoint();
}
