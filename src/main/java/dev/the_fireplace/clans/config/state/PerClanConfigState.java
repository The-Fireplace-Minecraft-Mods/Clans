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

    private int homeTeleportWarmupTime;
    private int homeTeleportCooldownTime;
    private String maxClaimCountFormula;
    private String chatPrefix;
    private boolean isHomeFallbackSpawnpoint;

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
        homeTeleportWarmupTime = buffer.readInt("homeTeleportWarmupTime", defaultConfig.getHomeTeleportWarmupTime());
        homeTeleportCooldownTime = buffer.readInt("homeTeleportCooldownTime", defaultConfig.getHomeTeleportCooldownTime());
        maxClaimCountFormula = buffer.readString("maxClaimCountFormula", defaultConfig.getMaxClaimCountFormula());
        chatPrefix = buffer.readString("chatPrefix", defaultConfig.getChatPrefix());
        isHomeFallbackSpawnpoint = buffer.readBool("isClanHomeFallbackSpawnpoint", defaultConfig.isHomeFallbackSpawnpoint());
    }

    @Override
    public void writeTo(StorageWriteBuffer buffer) {
        buffer.writeInt("homeTeleportWarmupTime", homeTeleportWarmupTime);
        buffer.writeInt("homeTeleportCooldownTime", homeTeleportCooldownTime);
        buffer.writeString("maxClaimCountFormula", maxClaimCountFormula);
        buffer.writeString("chatPrefix", chatPrefix);
        buffer.writeBool("isHomeFallbackSpawnpoint", isHomeFallbackSpawnpoint);
    }

    @Override
    public int getHomeTeleportWarmupTime() {
        return homeTeleportWarmupTime;
    }

    public void setHomeTeleportWarmupTime(int homeTeleportWarmupTime) {
        this.homeTeleportWarmupTime = homeTeleportWarmupTime;
    }

    @Override
    public int getHomeTeleportCooldownTime() {
        return homeTeleportCooldownTime;
    }

    public void setHomeTeleportCooldownTime(int homeTeleportCooldownTime) {
        this.homeTeleportCooldownTime = homeTeleportCooldownTime;
    }

    @Override
    public String getMaxClaimCountFormula() {
        return maxClaimCountFormula;
    }

    public void setMaxClaimCountFormula(String maxClaimCountFormula) {
        this.maxClaimCountFormula = maxClaimCountFormula;
    }

    @Override
    public String getChatPrefix() {
        return chatPrefix;
    }

    public void setChatPrefix(String chatPrefix) {
        this.chatPrefix = chatPrefix;
    }

    @Override
    public boolean isHomeFallbackSpawnpoint() {
        return isHomeFallbackSpawnpoint;
    }

    public void setHomeFallbackSpawnpoint(boolean homeFallbackSpawnpoint) {
        isHomeFallbackSpawnpoint = homeFallbackSpawnpoint;
    }
}
