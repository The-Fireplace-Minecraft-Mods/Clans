package dev.the_fireplace.clans.api.config.injectables;

import java.util.List;

public interface WorldProtectionConfig
{
    boolean shouldProtectWilderness();

    short getMinimumWildernessY();

    List<String> getClaimableDimensions();
}
