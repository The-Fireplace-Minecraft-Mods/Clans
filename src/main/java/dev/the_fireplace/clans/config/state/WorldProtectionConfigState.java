package dev.the_fireplace.clans.config.state;

import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.clans.api.config.injectables.WorldProtectionConfig;
import dev.the_fireplace.lib.api.io.interfaces.access.StorageReadBuffer;
import dev.the_fireplace.lib.api.io.interfaces.access.StorageWriteBuffer;
import dev.the_fireplace.lib.api.lazyio.injectables.ConfigStateManager;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;

@Implementation
@Singleton
public final class WorldProtectionConfigState extends ClansConfigState implements WorldProtectionConfig
{
    private final WorldProtectionConfig defaultConfig;

    private boolean shouldProtectWilderness;
    private short minimumWildernessY;
    private List<String> claimableDimensions;

    @Inject
    public WorldProtectionConfigState(ConfigStateManager configStateManager, @Named("default") WorldProtectionConfig defaultConfig) {
        this.defaultConfig = defaultConfig;
        configStateManager.initialize(this);
    }

    @Override
    public String getId() {
        return "world_protection";
    }

    @Override
    public void readFrom(StorageReadBuffer buffer) {
        shouldProtectWilderness = buffer.readBool("shouldProtectWilderness", defaultConfig.shouldProtectWilderness());
        minimumWildernessY = buffer.readShort("minimumWildernessY", defaultConfig.getMinimumWildernessY());
        claimableDimensions = buffer.readStringList("claimableDimensions", defaultConfig.getClaimableDimensions());
    }

    @Override
    public void writeTo(StorageWriteBuffer buffer) {
        buffer.writeBool("shouldProtectWilderness", defaultConfig.shouldProtectWilderness());
        buffer.writeShort("minimumWildernessY", defaultConfig.getMinimumWildernessY());
        buffer.writeStringList("claimableDimensions", defaultConfig.getClaimableDimensions());
    }

    @Override
    public boolean shouldProtectWilderness() {
        return shouldProtectWilderness;
    }

    public void setShouldProtectWilderness(boolean shouldProtectWilderness) {
        this.shouldProtectWilderness = shouldProtectWilderness;
    }

    @Override
    public short getMinimumWildernessY() {
        return minimumWildernessY;
    }

    public void setMinimumWildernessY(short minimumWildernessY) {
        this.minimumWildernessY = minimumWildernessY;
    }

    @Override
    public List<String> getClaimableDimensions() {
        return claimableDimensions;
    }

    public void setClaimableDimensions(List<String> claimableDimensions) {
        this.claimableDimensions = claimableDimensions;
    }
}
