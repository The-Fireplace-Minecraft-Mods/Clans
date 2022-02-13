package dev.the_fireplace.clans.config.defaults;

import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.clans.api.config.injectables.ChatCensorConfig;

@Implementation(name = "default")
public final class ChatCensorConfigDefaults implements ChatCensorConfig
{
    @Override
    public boolean censorClanNames() {
        return true;
    }

    @Override
    public boolean censorClanDescriptions() {
        return false;
    }

    @Override
    public boolean censorDynmapDetails() {
        return true;
    }
}
