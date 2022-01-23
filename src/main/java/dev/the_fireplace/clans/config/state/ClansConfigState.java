package dev.the_fireplace.clans.config.state;

import dev.the_fireplace.clans.ClansConstants;
import dev.the_fireplace.lib.api.lazyio.interfaces.Config;

public abstract class ClansConfigState implements Config
{
    @Override
    public String getSubfolderName() {
        return ClansConstants.MODID;
    }
}
