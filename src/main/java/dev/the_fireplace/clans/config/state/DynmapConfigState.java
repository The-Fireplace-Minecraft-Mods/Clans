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

    private int borderWeight;
    private double borderOpacity;
    private double fillOpacity;

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
        borderWeight = buffer.readInt("borderWeight", defaultConfig.getBorderWeight());
        borderOpacity = buffer.readDouble("borderOpacity", defaultConfig.getBorderOpacity());
        fillOpacity = buffer.readDouble("fillOpacity", defaultConfig.getFillOpacity());
    }

    @Override
    public void writeTo(StorageWriteBuffer buffer) {
        buffer.writeInt("borderWeight", defaultConfig.getBorderWeight());
        buffer.writeDouble("borderOpacity", defaultConfig.getBorderOpacity());
        buffer.writeDouble("fillOpacity", defaultConfig.getFillOpacity());
    }

    @Override
    public int getBorderWeight() {
        return borderWeight;
    }

    public void setBorderWeight(int borderWeight) {
        this.borderWeight = borderWeight;
    }

    @Override
    public double getBorderOpacity() {
        return borderOpacity;
    }

    public void setBorderOpacity(double borderOpacity) {
        this.borderOpacity = borderOpacity;
    }

    @Override
    public double getFillOpacity() {
        return fillOpacity;
    }

    public void setFillOpacity(double fillOpacity) {
        this.fillOpacity = fillOpacity;
    }
}
