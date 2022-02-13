package dev.the_fireplace.clans.config.state;

import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.clans.api.config.injectables.ChatCensorConfig;
import dev.the_fireplace.lib.api.io.interfaces.access.StorageReadBuffer;
import dev.the_fireplace.lib.api.io.interfaces.access.StorageWriteBuffer;
import dev.the_fireplace.lib.api.lazyio.injectables.ConfigStateManager;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Implementation
@Singleton
public final class ChatCensorConfigState extends ClansConfigState implements ChatCensorConfig
{
    private final ChatCensorConfig defaultConfig;

    private boolean censorClanNames;
    private boolean censorClanDescriptions;
    private boolean censorDynmapDetails;

    @Inject
    public ChatCensorConfigState(ConfigStateManager configStateManager, @Named("default") ChatCensorConfig defaultConfig) {
        this.defaultConfig = defaultConfig;
        configStateManager.initialize(this);
    }

    @Override
    public String getId() {
        return "chatCensor";
    }

    @Override
    public void readFrom(StorageReadBuffer buffer) {
        censorClanNames = buffer.readBool("censorClanNames", defaultConfig.censorClanNames());
        censorClanDescriptions = buffer.readBool("censorClanDescriptions", defaultConfig.censorClanDescriptions());
        censorDynmapDetails = buffer.readBool("censorDynmapDetails", defaultConfig.censorDynmapDetails());
    }

    @Override
    public void writeTo(StorageWriteBuffer buffer) {
        buffer.writeBool("censorClanNames", censorClanNames);
        buffer.writeBool("censorClanDescriptions", censorClanDescriptions);
        buffer.writeBool("censorDynmapDetails", censorDynmapDetails);
    }

    @Override
    public boolean censorClanNames() {
        return censorClanNames;
    }

    public void setCensorClanNames(boolean censorClanNames) {
        this.censorClanNames = censorClanNames;
    }

    @Override
    public boolean censorClanDescriptions() {
        return censorClanDescriptions;
    }

    public void setCensorClanDescriptions(boolean censorClanDescriptions) {
        this.censorClanDescriptions = censorClanDescriptions;
    }

    @Override
    public boolean censorDynmapDetails() {
        return censorDynmapDetails;
    }

    public void setCensorDynmapDetails(boolean censorDynmapDetails) {
        this.censorDynmapDetails = censorDynmapDetails;
    }
}
