package dev.the_fireplace.clans.domain.config;

public interface PerClanConfig
{
    int getHomeTeleportWarmupTime();

    int getHomeTeleportCooldownTime();

    String getMaxClaimCountFormula();

    String getChatPrefix();

    boolean isHomeFallbackSpawnpoint();
}
