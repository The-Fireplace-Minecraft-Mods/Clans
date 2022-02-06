package dev.the_fireplace.clans.config.state;

import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.clans.domain.config.RaidEconomicsConfig;
import dev.the_fireplace.lib.api.io.interfaces.access.StorageReadBuffer;
import dev.the_fireplace.lib.api.io.interfaces.access.StorageWriteBuffer;
import dev.the_fireplace.lib.api.lazyio.injectables.ConfigStateManager;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Implementation
@Singleton
public final class RaidEconomicsConfigState extends ClansConfigState implements RaidEconomicsConfig
{
    private final RaidEconomicsConfig defaultConfig;

    private String startRaidCostFormula;
    private String winRaidRewardFormula;
    private boolean increasingRewards;
    private double minimumWinLossRatioForWeaknessFactorReduction;
    private String increasedWeaknessFactorFormula;
    private String decreasedWeaknessFactorFormula;

    @Inject
    public RaidEconomicsConfigState(ConfigStateManager configStateManager, @Named("default") RaidEconomicsConfig defaultConfig) {
        this.defaultConfig = defaultConfig;
        configStateManager.initialize(this);
    }

    @Override
    public String getId() {
        return "raid-economics";
    }

    @Override
    public void readFrom(StorageReadBuffer buffer) {
        startRaidCostFormula = buffer.readString("startRaidCostFormula", defaultConfig.getStartRaidCostFormula());
        winRaidRewardFormula = buffer.readString("winRaidRewardFormula", defaultConfig.getWinRaidRewardFormula());
        increasingRewards = buffer.readBool("increasingRewards", defaultConfig.isIncreasingRewards());
        minimumWinLossRatioForWeaknessFactorReduction = buffer.readDouble("minimumWinLossRatioForWeaknessFactorReduction", defaultConfig.getMinimumWinLossRatioForWeaknessFactorReduction());
        increasedWeaknessFactorFormula = buffer.readString("increasedWeaknessFactorFormula", defaultConfig.getIncreasedWeaknessFactorFormula());
        decreasedWeaknessFactorFormula = buffer.readString("decreasedWeaknessFactorFormula", defaultConfig.getDecreasedWeaknessFactorFormula());
    }

    @Override
    public void writeTo(StorageWriteBuffer buffer) {
        buffer.writeString("startRaidCostFormula", startRaidCostFormula);
        buffer.writeString("winRaidRewardFormula", winRaidRewardFormula);
        buffer.writeBool("increasingRewards", increasingRewards);
        buffer.writeDouble("minimumWinLossRatioForWeaknessFactorReduction", minimumWinLossRatioForWeaknessFactorReduction);
        buffer.writeString("increasedWeaknessFactorFormula", increasedWeaknessFactorFormula);
        buffer.writeString("decreasedWeaknessFactorFormula", decreasedWeaknessFactorFormula);
    }

    @Override
    public String getStartRaidCostFormula() {
        return startRaidCostFormula;
    }

    public void setStartRaidCostFormula(String startRaidCostFormula) {
        this.startRaidCostFormula = startRaidCostFormula;
    }

    @Override
    public String getWinRaidRewardFormula() {
        return winRaidRewardFormula;
    }

    public void setWinRaidRewardFormula(String winRaidRewardFormula) {
        this.winRaidRewardFormula = winRaidRewardFormula;
    }

    @Override
    public boolean isIncreasingRewards() {
        return increasingRewards;
    }

    public void setIncreasingRewards(boolean increasingRewards) {
        this.increasingRewards = increasingRewards;
    }

    @Override
    public double getMinimumWinLossRatioForWeaknessFactorReduction() {
        return minimumWinLossRatioForWeaknessFactorReduction;
    }

    public void setMinimumWinLossRatioForWeaknessFactorReduction(double minimumWinLossRatioForWeaknessFactorReduction) {
        this.minimumWinLossRatioForWeaknessFactorReduction = minimumWinLossRatioForWeaknessFactorReduction;
    }

    @Override
    public String getIncreasedWeaknessFactorFormula() {
        return increasedWeaknessFactorFormula;
    }

    public void setIncreasedWeaknessFactorFormula(String increasedWeaknessFactorFormula) {
        this.increasedWeaknessFactorFormula = increasedWeaknessFactorFormula;
    }

    @Override
    public String getDecreasedWeaknessFactorFormula() {
        return decreasedWeaknessFactorFormula;
    }

    public void setDecreasedWeaknessFactorFormula(String decreasedWeaknessFactorFormula) {
        this.decreasedWeaknessFactorFormula = decreasedWeaknessFactorFormula;
    }
}
