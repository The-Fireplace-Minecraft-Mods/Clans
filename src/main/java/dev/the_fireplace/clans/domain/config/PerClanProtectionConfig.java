package dev.the_fireplace.clans.domain.config;

import java.util.List;

public interface PerClanProtectionConfig
{
    boolean isForceConnectedClaims();

    boolean isEnableBorderlands();

    boolean isPreventMobsOnClaims();

    boolean isPreventMobsOnBorderlands();

    boolean isAllowTntChainingOnClaims();

    List<String> getLockableBlocks();
}
