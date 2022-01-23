package dev.the_fireplace.clans.config.defaults;

import com.google.common.collect.Lists;
import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.clans.domain.config.WorldProtectionConfig;

import java.util.List;

@Implementation(name = "default")
public final class WorldProtectionConfigDefaults implements WorldProtectionConfig
{
    @Override
    public boolean shouldProtectWilderness() {
        return false;
    }

    @Override
    public short getMinimumWildernessY() {
        return 64;
    }

    @Override
    public List<String> getClaimableDimensions() {
        return Lists.newArrayList("overworld", "the_nether");
    }
}
