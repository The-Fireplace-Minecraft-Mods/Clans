package dev.the_fireplace.clans.config.state;

import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.clans.api.config.injectables.DynmapConfig;
import dev.the_fireplace.lib.api.io.interfaces.access.StorageReadBuffer;
import dev.the_fireplace.lib.api.io.interfaces.access.StorageWriteBuffer;
import dev.the_fireplace.lib.api.lazyio.injectables.ConfigStateManager;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Implementation
@Singleton
public final class DynmapConfigState extends ClansConfigState implements DynmapConfig
{
    private final DynmapConfig defaultConfig;

    private int dynmapBorderWeight;
    private double dynmapBorderOpacity;
    private double dynmapFillOpacity;
    private int maxDisplayedConnectedClaims;

    @Inject
    public DynmapConfigState(ConfigStateManager configStateManager, @Named("default") DynmapConfig defaultConfig) {
        this.defaultConfig = defaultConfig;
        configStateManager.initialize(this);
    }

    @Override
    public String getId() {
        return "dynmap";
    }

    @Override
    public void readFrom(StorageReadBuffer buffer) {
        dynmapBorderWeight = buffer.readInt("dynmapBorderWeight", defaultConfig.getDynmapBorderWeight());
        dynmapBorderOpacity = buffer.readDouble("dynmapBorderOpacity", defaultConfig.getDynmapBorderOpacity());
        dynmapFillOpacity = buffer.readDouble("dynmapFillOpacity", defaultConfig.getDynmapFillOpacity());
        maxDisplayedConnectedClaims = buffer.readInt("maxDisplayedConnectedClaims", defaultConfig.getMaxDisplayedConnectedClaims());
    }

    @Override
    public void writeTo(StorageWriteBuffer buffer) {
        buffer.writeInt("dynmapBorderWeight", defaultConfig.getDynmapBorderWeight());
        buffer.writeDouble("dynmapBorderOpacity", defaultConfig.getDynmapBorderOpacity());
        buffer.writeDouble("dynmapFillOpacity", defaultConfig.getDynmapFillOpacity());
        buffer.writeInt("maxDisplayedConnectedClaims", defaultConfig.getMaxDisplayedConnectedClaims());
    }

    @Override
    public int getDynmapBorderWeight() {
        return dynmapBorderWeight;
    }

    public void setDynmapBorderWeight(int dynmapBorderWeight) {
        this.dynmapBorderWeight = dynmapBorderWeight;
    }

    @Override
    public double getDynmapBorderOpacity() {
        return dynmapBorderOpacity;
    }

    public void setDynmapBorderOpacity(double dynmapBorderOpacity) {
        this.dynmapBorderOpacity = dynmapBorderOpacity;
    }

    @Override
    public double getDynmapFillOpacity() {
        return dynmapFillOpacity;
    }

    public void setDynmapFillOpacity(double dynmapFillOpacity) {
        this.dynmapFillOpacity = dynmapFillOpacity;
    }

    @Override
    public int getMaxDisplayedConnectedClaims() {
        return maxDisplayedConnectedClaims;
    }

    public void setMaxDisplayedConnectedClaims(int maxDisplayedConnectedClaims) {
        this.maxDisplayedConnectedClaims = maxDisplayedConnectedClaims;
    }
}
