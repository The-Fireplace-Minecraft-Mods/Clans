package dev.the_fireplace.clans.config.state;

import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.clans.domain.config.PerClanProtectionConfig;
import dev.the_fireplace.lib.api.io.interfaces.access.StorageReadBuffer;
import dev.the_fireplace.lib.api.io.interfaces.access.StorageWriteBuffer;
import dev.the_fireplace.lib.api.lazyio.injectables.ConfigStateManager;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;

@Implementation
@Singleton
public final class PerClanProtectionConfigState extends ClansConfigState implements PerClanProtectionConfig
{
    private final PerClanProtectionConfig defaultConfig;

    private boolean forceConnectedClaims;
    private boolean enableBorderlands;
    private boolean preventMobsOnClaims;
    private boolean preventMobsOnBorderlands;
    private boolean allowTntChainingOnClaims;
    private List<String> lockableBlocks;

    @Inject
    public PerClanProtectionConfigState(ConfigStateManager configStateManager, @Named("default") PerClanProtectionConfig defaultConfig) {
        this.defaultConfig = defaultConfig;
        configStateManager.initialize(this);
    }

    @Override
    public String getId() {
        return "overridable_per_clan_economics";
    }

    @Override
    public void readFrom(StorageReadBuffer buffer) {
        forceConnectedClaims = buffer.readBool("forceConnectedClaims", defaultConfig.isForceConnectedClaims());
        enableBorderlands = buffer.readBool("enableBorderlands", defaultConfig.isEnableBorderlands());
        preventMobsOnClaims = buffer.readBool("preventMobsOnClaims", defaultConfig.isPreventMobsOnClaims());
        preventMobsOnBorderlands = buffer.readBool("preventMobsOnBorderlands", defaultConfig.isPreventMobsOnBorderlands());
        allowTntChainingOnClaims = buffer.readBool("allowTntChainingOnClaims", defaultConfig.isAllowTntChainingOnClaims());
        lockableBlocks = buffer.readStringList("lockableBlocks", defaultConfig.getLockableBlocks());
    }

    @Override
    public void writeTo(StorageWriteBuffer buffer) {
        buffer.writeBool("forceConnectedClaims", forceConnectedClaims);
        buffer.writeBool("enableBorderlands", enableBorderlands);
        buffer.writeBool("preventMobsOnClaims", preventMobsOnClaims);
        buffer.writeBool("preventMobsOnBorderlands", preventMobsOnBorderlands);
        buffer.writeBool("allowTntChainingOnClaims", allowTntChainingOnClaims);
        buffer.writeStringList("lockableBlocks", lockableBlocks);
    }

    @Override
    public boolean isForceConnectedClaims() {
        return forceConnectedClaims;
    }

    @Override
    public boolean isEnableBorderlands() {
        return enableBorderlands;
    }

    @Override
    public boolean isPreventMobsOnClaims() {
        return preventMobsOnClaims;
    }

    @Override
    public boolean isPreventMobsOnBorderlands() {
        return preventMobsOnBorderlands;
    }

    @Override
    public boolean isAllowTntChainingOnClaims() {
        return allowTntChainingOnClaims;
    }

    @Override
    public List<String> getLockableBlocks() {
        return lockableBlocks;
    }
}
