package dev.the_fireplace.clans.entrypoints;

import com.google.inject.Injector;
import dev.the_fireplace.clans.config.ClansConfigScreenFactory;
import dev.the_fireplace.lib.api.client.entrypoints.ConfigGuiEntrypoint;
import dev.the_fireplace.lib.api.client.interfaces.ConfigGuiRegistry;

public final class ConfigGui implements ConfigGuiEntrypoint
{
    @Override
    public void registerConfigGuis(Injector injector, ConfigGuiRegistry configGuiRegistry) {
        ClansConfigScreenFactory configScreenFactory = injector.getInstance(ClansConfigScreenFactory.class);
        configGuiRegistry.register(configScreenFactory::getConfigScreen);
    }
}
