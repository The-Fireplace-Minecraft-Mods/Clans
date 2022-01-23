package dev.the_fireplace.clans.domain.config;

import java.util.Collection;

public interface PerClanProtectionConfig
{
    boolean isForceConnectedClaims();

    boolean isEnableBorderlands();

    boolean isPreventMobsOnClaims();

    boolean isPreventMobsOnBorderlands();

    boolean isChainTNT();

    Collection<String> getLockableBlocks();
}
