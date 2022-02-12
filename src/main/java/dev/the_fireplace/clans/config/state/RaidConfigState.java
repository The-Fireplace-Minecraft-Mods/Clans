package dev.the_fireplace.clans.config.state;

import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.clans.api.config.injectables.RaidConfig;
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
        return "raid";
    }

    @Override
    public void readFrom(StorageReadBuffer buffer) {
        maxRaidersFormula = buffer.readString("maxRaidersFormula", defaultConfig.getMaxRaidersFormula());
        maxRaidDurationFormula = buffer.readString("maxRaidDurationFormula", defaultConfig.getMaxRaidDurationFormula());
        raidPreparationTimeFormula = buffer.readString("raidPreparationTimeFormula", defaultConfig.getRaidPreparationTimeFormula());
        remainingTimeToGlowFormula = buffer.readString("remainingTimeToGlowFormula", defaultConfig.getRemainingTimeToGlowFormula());
        maxAttackerAbandonmentTimeFormula = buffer.readString("maxAttackerAbandonmentTimeFormula", defaultConfig.getMaxAttackerAbandonmentTimeFormula());
        maxDefenderDesertionTimeFormula = buffer.readString("maxDefenderDesertionTimeFormula", defaultConfig.getMaxDefenderDesertionTimeFormula());
        shieldAfterRaidFormula = buffer.readString("shieldAfterRaidFormula", defaultConfig.getShieldAfterRaidFormula());
        initialShield = buffer.readInt("initialShield", defaultConfig.getInitialShield());
        allowReclaimingTNT = buffer.readBool("allowReclaimingTNT", defaultConfig.allowReclaimingTNT());
        enableRaidRollback = buffer.readBool("enableRaidRollback", defaultConfig.isEnableRaidRollback());
        enableStealing = buffer.readBool("enableStealing", defaultConfig.isEnableStealing());
        raidBreakSpeedMultiplier = buffer.readDouble("raidBreakSpeedMultiplier", defaultConfig.getRaidBreakSpeedMultiplier());
        raidItemList = buffer.readStringList("raidItemList", defaultConfig.getRaidItemList());
        teleportToRaidStart = buffer.readBool("teleportToRaidStart", defaultConfig.isTeleportToRaidStart());
    }

    @Override
    public void writeTo(StorageWriteBuffer buffer) {
        buffer.writeString("maxRaidersFormula", maxRaidersFormula);
        buffer.writeString("maxRaidDurationFormula", maxRaidDurationFormula);
        buffer.writeString("raidPreparationTimeFormula", raidPreparationTimeFormula);
        buffer.writeString("remainingTimeToGlowFormula", remainingTimeToGlowFormula);
        buffer.writeString("maxAttackerAbandonmentTimeFormula", maxAttackerAbandonmentTimeFormula);
        buffer.writeString("maxDefenderDesertionTimeFormula", maxDefenderDesertionTimeFormula);
        buffer.writeString("shieldAfterRaidFormula", shieldAfterRaidFormula);
        buffer.writeInt("initialShield", initialShield);
        buffer.writeBool("allowReclaimingTNT", allowReclaimingTNT);
        buffer.writeBool("enableRaidRollback", enableRaidRollback);
        buffer.writeBool("enableStealing", enableStealing);
        buffer.writeDouble("raidBreakSpeedMultiplier", raidBreakSpeedMultiplier);
        buffer.writeStringList("raidItemList", raidItemList);
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
    public String getMaxRaidDurationFormula() {
        return maxRaidDurationFormula;
    }

    public void setMaxRaidDurationFormula(String maxRaidDurationFormula) {
        this.maxRaidDurationFormula = maxRaidDurationFormula;
    }

    @Override
    public String getRaidPreparationTimeFormula() {
        return raidPreparationTimeFormula;
    }

    public void setRaidPreparationTimeFormula(String raidPreparationTimeFormula) {
        this.raidPreparationTimeFormula = raidPreparationTimeFormula;
    }

    @Override
    public String getRemainingTimeToGlowFormula() {
        return remainingTimeToGlowFormula;
    }

    public void setRemainingTimeToGlowFormula(String remainingTimeToGlowFormula) {
        this.remainingTimeToGlowFormula = remainingTimeToGlowFormula;
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
    public boolean allowReclaimingTNT() {
        return allowReclaimingTNT;
    }

    public void setAllowReclaimingTNT(boolean allowReclaimingTNT) {
        this.allowReclaimingTNT = allowReclaimingTNT;
    }

    @Override
    public boolean isEnableRaidRollback() {
        return enableRaidRollback;
    }

    public void setEnableRaidRollback(boolean enableRaidRollback) {
        this.enableRaidRollback = enableRaidRollback;
    }

    @Override
    public boolean isEnableStealing() {
        return enableStealing;
    }

    public void setEnableStealing(boolean enableStealing) {
        this.enableStealing = enableStealing;
    }

    @Override
    public double getRaidBreakSpeedMultiplier() {
        return raidBreakSpeedMultiplier;
    }

    public void setRaidBreakSpeedMultiplier(double raidBreakSpeedMultiplier) {
        this.raidBreakSpeedMultiplier = raidBreakSpeedMultiplier;
    }

    @Override
    public List<String> getRaidItemList() {
        return raidItemList;
    }

    public void setRaidItemList(List<String> raidItemList) {
        this.raidItemList = raidItemList;
    }

    @Override
    public boolean isTeleportToRaidStart() {
        return teleportToRaidStart;
    }

    public void setTeleportToRaidStart(boolean teleportToRaidStart) {
        this.teleportToRaidStart = teleportToRaidStart;
    }
}
