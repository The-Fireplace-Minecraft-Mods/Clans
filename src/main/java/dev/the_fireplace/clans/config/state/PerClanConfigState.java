package dev.the_fireplace.clans.config.state;

import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.clans.domain.config.PerClanConfig;
import dev.the_fireplace.lib.api.io.interfaces.access.StorageReadBuffer;
import dev.the_fireplace.lib.api.io.interfaces.access.StorageWriteBuffer;
import dev.the_fireplace.lib.api.lazyio.injectables.ConfigStateManager;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Implementation
public final class PerClanConfigState extends ClansConfigState implements PerClanConfig
{
    private final PerClanConfig defaultConfig;

    private int clanHomeWarmupTime;
    private int clanHomeCooldownTime;
    private String maxClaimCountFormula;
    private String defaultClanPrefix;
    private boolean isClanHomeFallbackSpawnpoint;

    @Inject
    public PerClanConfigState(ConfigStateManager configStateManager, @Named("default") PerClanConfig defaultConfig) {
        this.defaultConfig = defaultConfig;
        configStateManager.initialize(this);
    }

    @Override
    public String getId() {
        return "overridable_per_clan";
    }

    @Override
    public void readFrom(StorageReadBuffer buffer) {
        clanHomeWarmupTime = buffer.readInt("clanHomeWarmupTime", defaultConfig.getClanHomeWarmupTime());
        clanHomeCooldownTime = buffer.readInt("clanHomeCooldownTime", defaultConfig.getClanHomeCooldownTime());
        maxClaimCountFormula = buffer.readString("maxClaimCountFormula", defaultConfig.getMaxClaimCountFormula());
        defaultClanPrefix = buffer.readString("defaultClanPrefix", defaultConfig.getDefaultClanPrefix());
        isClanHomeFallbackSpawnpoint = buffer.readBool("isClanHomeFallbackSpawnpoint", defaultConfig.isClanHomeFallbackSpawnpoint());
    }

    @Override
    public void writeTo(StorageWriteBuffer buffer) {
        buffer.writeInt("clanHomeWarmupTime", clanHomeWarmupTime);
        buffer.writeInt("clanHomeCooldownTime", clanHomeCooldownTime);
        buffer.writeString("maxClaimCountFormula", maxClaimCountFormula);
        buffer.writeString("defaultClanPrefix", defaultClanPrefix);
        buffer.writeBool("isClanHomeFallbackSpawnpoint", isClanHomeFallbackSpawnpoint);
    }

    @Override
    public int getClanHomeWarmupTime() {
        return clanHomeWarmupTime;
    }

    public void setClanHomeWarmupTime(int clanHomeWarmupTime) {
        this.clanHomeWarmupTime = clanHomeWarmupTime;
    }

    @Override
    public int getClanHomeCooldownTime() {
        return clanHomeCooldownTime;
    }

    public void setClanHomeCooldownTime(int clanHomeCooldownTime) {
        this.clanHomeCooldownTime = clanHomeCooldownTime;
    }

    @Override
    public String getMaxClaimCountFormula() {
        return maxClaimCountFormula;
    }

    public void setMaxClaimCountFormula(String maxClaimCountFormula) {
        this.maxClaimCountFormula = maxClaimCountFormula;
    }

    @Override
    public String getDefaultClanPrefix() {
        return defaultClanPrefix;
    }

    public void setDefaultClanPrefix(String defaultClanPrefix) {
        this.defaultClanPrefix = defaultClanPrefix;
    }

    @Override
    public boolean isClanHomeFallbackSpawnpoint() {
        return isClanHomeFallbackSpawnpoint;
    }

    public void setClanHomeFallbackSpawnpoint(boolean clanHomeFallbackSpawnpoint) {
        isClanHomeFallbackSpawnpoint = clanHomeFallbackSpawnpoint;
    }
}
