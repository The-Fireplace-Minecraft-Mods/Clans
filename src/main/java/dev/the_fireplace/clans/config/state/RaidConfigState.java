package dev.the_fireplace.clans.config.state;

import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.clans.api.config.injectables.RaidConfig;
import dev.the_fireplace.lib.api.io.interfaces.access.StorageReadBuffer;
import dev.the_fireplace.lib.api.io.interfaces.access.StorageWriteBuffer;
import dev.the_fireplace.lib.api.lazyio.injectables.ConfigStateManager;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Implementation
@Singleton
public final class RaidConfigState extends ClansConfigState implements RaidConfig
{
    private final RaidConfig defaultConfig;

    private String maxRaidersFormula;
    private String maxDurationFormula;
    private String preparationTimeFormula;
    private String defenderGlowTimeFormula;
    private String maxAttackerAbandonmentTimeFormula;
    private String maxDefenderDesertionTimeFormula;
    private String shieldAfterRaidFormula;
    private int initialShield;
    private boolean preventReclaimingTnt;
    private boolean enableRollback;
    private boolean enableStealing;
    private double breakSpeedMultiplier;
    private boolean teleportToRaidStart;

    @Inject
    public RaidConfigState(ConfigStateManager configStateManager, @Named("default") RaidConfig defaultConfig) {
        this.defaultConfig = defaultConfig;
        configStateManager.initialize(this);
    }

    @Override
    public String getId() {
        return "raid";
    }

    @Override
    public void readFrom(StorageReadBuffer buffer) {
        maxRaidersFormula = buffer.readString("maxRaidersFormula", defaultConfig.getMaxRaidersFormula());
        maxDurationFormula = buffer.readString("maxDurationFormula", defaultConfig.getMaxDurationFormula());
        preparationTimeFormula = buffer.readString("preparationTimeFormula", defaultConfig.getPreparationTimeFormula());
        defenderGlowTimeFormula = buffer.readString("defenderGlowTimeFormula", defaultConfig.getDefenderGlowTimeFormula());
        maxAttackerAbandonmentTimeFormula = buffer.readString("maxAttackerAbandonmentTimeFormula", defaultConfig.getMaxAttackerAbandonmentTimeFormula());
        maxDefenderDesertionTimeFormula = buffer.readString("maxDefenderDesertionTimeFormula", defaultConfig.getMaxDefenderDesertionTimeFormula());
        shieldAfterRaidFormula = buffer.readString("shieldAfterRaidFormula", defaultConfig.getShieldAfterRaidFormula());
        initialShield = buffer.readInt("initialShield", defaultConfig.getInitialShield());
        preventReclaimingTnt = buffer.readBool("preventReclaimingTnt", defaultConfig.preventReclaimingTNT());
        enableRollback = buffer.readBool("enableRollback", defaultConfig.isEnableRollback());
        enableStealing = buffer.readBool("enableStealing", defaultConfig.isEnableStealing());
        breakSpeedMultiplier = buffer.readDouble("breakSpeedMultiplier", defaultConfig.getBreakSpeedMultiplier());
        teleportToRaidStart = buffer.readBool("teleportToRaidStart", defaultConfig.isTeleportToRaidStart());
    }

    @Override
    public void writeTo(StorageWriteBuffer buffer) {
        buffer.writeString("maxRaidersFormula", maxRaidersFormula);
        buffer.writeString("maxDurationFormula", maxDurationFormula);
        buffer.writeString("preparationTimeFormula", preparationTimeFormula);
        buffer.writeString("defenderGlowTimeFormula", defenderGlowTimeFormula);
        buffer.writeString("maxAttackerAbandonmentTimeFormula", maxAttackerAbandonmentTimeFormula);
        buffer.writeString("maxDefenderDesertionTimeFormula", maxDefenderDesertionTimeFormula);
        buffer.writeString("shieldAfterRaidFormula", shieldAfterRaidFormula);
        buffer.writeInt("initialShield", initialShield);
        buffer.writeBool("preventReclaimingTnt", preventReclaimingTnt);
        buffer.writeBool("enableRollback", enableRollback);
        buffer.writeBool("enableStealing", enableStealing);
        buffer.writeDouble("breakSpeedMultiplier", breakSpeedMultiplier);
        buffer.writeBool("teleportToRaidStart", teleportToRaidStart);
    }

    @Override
    public String getMaxRaidersFormula() {
        return maxRaidersFormula;
    }

    public void setMaxRaidersFormula(String maxRaidersFormula) {
        this.maxRaidersFormula = maxRaidersFormula;
    }

    @Override
    public String getMaxDurationFormula() {
        return maxDurationFormula;
    }

    public void setMaxDurationFormula(String maxDurationFormula) {
        this.maxDurationFormula = maxDurationFormula;
    }

    @Override
    public String getPreparationTimeFormula() {
        return preparationTimeFormula;
    }

    public void setPreparationTimeFormula(String preparationTimeFormula) {
        this.preparationTimeFormula = preparationTimeFormula;
    }

    @Override
    public String getDefenderGlowTimeFormula() {
        return defenderGlowTimeFormula;
    }

    public void setDefenderGlowTimeFormula(String defenderGlowTimeFormula) {
        this.defenderGlowTimeFormula = defenderGlowTimeFormula;
    }

    @Override
    public String getMaxAttackerAbandonmentTimeFormula() {
        return maxAttackerAbandonmentTimeFormula;
    }

    public void setMaxAttackerAbandonmentTimeFormula(String maxAttackerAbandonmentTimeFormula) {
        this.maxAttackerAbandonmentTimeFormula = maxAttackerAbandonmentTimeFormula;
    }

    @Override
    public String getMaxDefenderDesertionTimeFormula() {
        return maxDefenderDesertionTimeFormula;
    }

    public void setMaxDefenderDesertionTimeFormula(String maxDefenderDesertionTimeFormula) {
        this.maxDefenderDesertionTimeFormula = maxDefenderDesertionTimeFormula;
    }

    @Override
    public String getShieldAfterRaidFormula() {
        return shieldAfterRaidFormula;
    }

    public void setShieldAfterRaidFormula(String shieldAfterRaidFormula) {
        this.shieldAfterRaidFormula = shieldAfterRaidFormula;
    }

    @Override
    public int getInitialShield() {
        return initialShield;
    }

    public void setInitialShield(int initialShield) {
        this.initialShield = initialShield;
    }

    @Override
    public boolean preventReclaimingTNT() {
        return preventReclaimingTnt;
    }

    public void setPreventReclaimingTnt(boolean preventReclaimingTnt) {
        this.preventReclaimingTnt = preventReclaimingTnt;
    }

    @Override
    public boolean isEnableRollback() {
        return enableRollback;
    }

    public void setEnableRollback(boolean enableRollback) {
        this.enableRollback = enableRollback;
    }

    @Override
    public boolean isEnableStealing() {
        return enableStealing;
    }

    public void setEnableStealing(boolean enableStealing) {
        this.enableStealing = enableStealing;
    }

    @Override
    public double getBreakSpeedMultiplier() {
        return breakSpeedMultiplier;
    }

    public void setBreakSpeedMultiplier(double breakSpeedMultiplier) {
        this.breakSpeedMultiplier = breakSpeedMultiplier;
    }

    @Override
    public boolean isTeleportToRaidStart() {
        return teleportToRaidStart;
    }

    public void setTeleportToRaidStart(boolean teleportToRaidStart) {
        this.teleportToRaidStart = teleportToRaidStart;
    }
}
