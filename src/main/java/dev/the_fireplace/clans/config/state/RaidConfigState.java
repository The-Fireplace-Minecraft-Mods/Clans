package dev.the_fireplace.clans.config.state;

import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.clans.domain.config.RaidConfig;
import dev.the_fireplace.lib.api.io.interfaces.access.StorageReadBuffer;
import dev.the_fireplace.lib.api.io.interfaces.access.StorageWriteBuffer;
import dev.the_fireplace.lib.api.lazyio.injectables.ConfigStateManager;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;

@Implementation
@Singleton
public final class RaidConfigState extends ClansConfigState implements RaidConfig
{
    private final RaidConfig defaultConfig;

    private String maxRaidersFormula;
    private String maxRaidDurationFormula;
    private String raidPreparationTimeFormula;
    private String remainingTimeToGlowFormula;
    private String maxAttackerAbandonmentTimeFormula;
    private String maxDefenderDesertionTimeFormula;
    private String shieldAfterRaidFormula;
    private int initialShield;
    private boolean allowReclaimingTNT;
    private boolean enableRaidRollback;
    private boolean enableStealing;
    private double raidBreakSpeedMultiplier;
    private List<String> raidItemList;
    private boolean teleportToRaidStart;

    @Inject
    public RaidConfigState(ConfigStateManager configStateManager, @Named("default") RaidConfig defaultConfig) {
        this.defaultConfig = defaultConfig;
        configStateManager.initialize(this);
    }

    @Override
    public String getId() {
        return "raids";
    }

    @Override
    public void readFrom(StorageReadBuffer buffer) {
        maxRaidersFormula = buffer.readString("maxRaidersFormula", defaultConfig.getMaxRaidersFormula());
        maxRaidDurationFormula = buffer.readString("maxRaidDurationFormula", defaultConfig.getMaxRaidDurationFormula());
        raidPreparationTimeFormula = buffer.readString("raidPreparationTimeFormula", defaultConfig.getRaidPreparationTimeFormula());
        remainingTimeToGlowFormula = buffer.readString("remainingTimeToGlowFormula", defaultConfig.getRemainingTimeToGlowFormula());
        maxAttackerAbandonmentTimeFormula = buffer.readString("maxAttackerAbandonmentTimeFormula", defaultConfig.getMaxAttackerAbandonmentTimeFormula());
        maxDefenderDesertionTimeFormula = buffer.readString("maxDefenderDesertionTimeFormula", defaultConfig.getMaxDefenderDesertionTimeFormula());
    }

    @Override
    public void writeTo(StorageWriteBuffer buffer) {

    }

    @Override
    public String getMaxRaidersFormula() {
        return null;
    }

    @Override
    public String getMaxRaidDurationFormula() {
        return null;
    }

    @Override
    public String getRaidPreparationTimeFormula() {
        return null;
    }

    @Override
    public String getRemainingTimeToGlowFormula() {
        return null;
    }

    @Override
    public String getMaxAttackerAbandonmentTimeFormula() {
        return null;
    }

    @Override
    public String getMaxDefenderDesertionTimeFormula() {
        return null;
    }

    @Override
    public String getShieldAfterRaidFormula() {
        return null;
    }

    @Override
    public int getInitialShield() {
        return 0;
    }

    @Override
    public boolean allowReclaimingTNT() {
        return false;
    }

    @Override
    public boolean isEnableRaidRollback() {
        return false;
    }

    @Override
    public boolean isEnableStealing() {
        return false;
    }

    @Override
    public double getRaidBreakSpeedMultiplier() {
        return 0;
    }

    @Override
    public List<String> getRaidItemList() {
        return null;
    }

    @Override
    public boolean isTeleportToRaidStart() {
        return false;
    }
}
