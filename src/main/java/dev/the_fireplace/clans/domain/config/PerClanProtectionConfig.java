package dev.the_fireplace.clans.domain.config;

public interface PerClanProtectionConfig
{
    boolean isForceConnectedClaims();

    boolean isEnableBorderlands();

    boolean isPreventMobsOnClaims();

    boolean isPreventMobsOnBorderlands();

    boolean isAllowTntChainingOnClaims();
}
