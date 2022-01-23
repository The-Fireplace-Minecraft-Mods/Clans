package dev.the_fireplace.clans.domain.config;

import java.util.List;

public interface WorldProtectionConfig
{
    boolean shouldProtectWilderness();

    short getMinimumWildernessY();

    List<String> getClaimableDimensions();
}
