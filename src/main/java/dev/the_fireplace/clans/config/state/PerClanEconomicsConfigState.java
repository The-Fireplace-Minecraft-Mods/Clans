package dev.the_fireplace.clans.config.state;

import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.clans.domain.config.PerClanEconomicsConfig;
import dev.the_fireplace.lib.api.io.interfaces.access.StorageReadBuffer;
import dev.the_fireplace.lib.api.io.interfaces.access.StorageWriteBuffer;
import dev.the_fireplace.lib.api.lazyio.injectables.ConfigStateManager;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Implementation
@Singleton
public final class PerClanEconomicsConfigState extends ClansConfigState implements PerClanEconomicsConfig
{
    private final PerClanEconomicsConfig defaultConfig;

    private String chunkClaimCostFormula;
    private int chargeUpkeepFrequencyInDays;
    private String upkeepCostFormula;
    private int chargeRentFrequencyInDays;
    private String maxRentFormula;
    private boolean disbandWhenUnableToPayUpkeep;
    private boolean leaderCanWithdrawFunds;
    private boolean leaderShouldReceiveDisbandFunds;
    private boolean kickNonpayingMembers;
    private boolean kickNonpayingAdmins;
    private String disbandFeeFormula;

    @Inject
    public PerClanEconomicsConfigState(ConfigStateManager configStateManager, @Named("default") PerClanEconomicsConfig defaultConfig) {
        this.defaultConfig = defaultConfig;
        configStateManager.initialize(this);
    }

    @Override
    public String getId() {
        return "overridable_per_clan_economics";
    }

    @Override
    public void readFrom(StorageReadBuffer buffer) {
        chunkClaimCostFormula = buffer.readString("chunkClaimCostFormula", defaultConfig.getClaimChunkCostFormula());
        chargeUpkeepFrequencyInDays = buffer.readInt("chargeUpkeepFrequencyInDays", defaultConfig.getChargeUpkeepFrequencyInDays());
        upkeepCostFormula = buffer.readString("upkeepCostFormula", defaultConfig.getUpkeepCostFormula());
        chargeRentFrequencyInDays = buffer.readInt("chargeRentFrequencyInDays", defaultConfig.getChargeRentFrequencyInDays());
        maxRentFormula = buffer.readString("maxRentFormula", defaultConfig.getMaxRentFormula());
        disbandWhenUnableToPayUpkeep = buffer.readBool("disbandWhenUnableToPayUpkeep", defaultConfig.shouldDisbandWhenUnableToPayUpkeep());
        leaderCanWithdrawFunds = buffer.readBool("leaderCanWithdrawFunds", defaultConfig.canLeaderWithdrawFunds());
        leaderShouldReceiveDisbandFunds = buffer.readBool("leaderShouldReceiveDisbandFunds", defaultConfig.shouldLeaderReceiveDisbandFunds());
        kickNonpayingMembers = buffer.readBool("kickNonpayingMembers", defaultConfig.shouldKickNonpayingMembers());
        kickNonpayingAdmins = buffer.readBool("kickNonpayingAdmins", defaultConfig.shouldKickNonpayingAdmins());
        disbandFeeFormula = buffer.readString("disbandFeeFormula", defaultConfig.getDisbandFeeFormula());
    }

    @Override
    public void writeTo(StorageWriteBuffer buffer) {
        buffer.writeString("chunkClaimCostFormula", chunkClaimCostFormula);
        buffer.writeInt("chargeUpkeepFrequencyInDays", chargeUpkeepFrequencyInDays);
        buffer.writeString("upkeepCostFormula", upkeepCostFormula);
        buffer.writeInt("chargeRentFrequencyInDays", chargeRentFrequencyInDays);
        buffer.writeString("maxRentFormula", maxRentFormula);
        buffer.writeBool("disbandWhenUnableToPayUpkeep", disbandWhenUnableToPayUpkeep);
        buffer.writeBool("leaderCanWithdrawFunds", leaderCanWithdrawFunds);
        buffer.writeBool("leaderShouldReceiveDisbandFunds", leaderShouldReceiveDisbandFunds);
        buffer.writeBool("kickNonpayingMembers", kickNonpayingMembers);
        buffer.writeBool("kickNonpayingAdmins", kickNonpayingAdmins);
        buffer.writeString("disbandFeeFormula", disbandFeeFormula);
    }

    @Override
    public String getClaimChunkCostFormula() {
        return chunkClaimCostFormula;
    }

    public void setClaimChunkCostFormula(String chunkClaimCostFormula) {
        this.chunkClaimCostFormula = chunkClaimCostFormula;
    }

    @Override
    public int getChargeUpkeepFrequencyInDays() {
        return chargeUpkeepFrequencyInDays;
    }

    public void setChargeUpkeepFrequencyInDays(int chargeUpkeepFrequencyInDays) {
        this.chargeUpkeepFrequencyInDays = chargeUpkeepFrequencyInDays;
    }

    @Override
    public String getUpkeepCostFormula() {
        return upkeepCostFormula;
    }

    public void setUpkeepCostFormula(String upkeepCostFormula) {
        this.upkeepCostFormula = upkeepCostFormula;
    }

    @Override
    public boolean shouldDisbandWhenUnableToPayUpkeep() {
        return disbandWhenUnableToPayUpkeep;
    }

    public void setDisbandWhenUnableToPayUpkeep(boolean disbandWhenUnableToPayUpkeep) {
        this.disbandWhenUnableToPayUpkeep = disbandWhenUnableToPayUpkeep;
    }

    @Override
    public boolean canLeaderWithdrawFunds() {
        return leaderCanWithdrawFunds;
    }

    public void setLeaderCanWithdrawFunds(boolean leaderCanWithdrawFunds) {
        this.leaderCanWithdrawFunds = leaderCanWithdrawFunds;
    }

    @Override
    public boolean shouldLeaderReceiveDisbandFunds() {
        return leaderShouldReceiveDisbandFunds;
    }

    public void setLeaderShouldReceiveDisbandFunds(boolean leaderShouldReceiveDisbandFunds) {
        this.leaderShouldReceiveDisbandFunds = leaderShouldReceiveDisbandFunds;
    }

    @Override
    public int getChargeRentFrequencyInDays() {
        return chargeRentFrequencyInDays;
    }

    public void setChargeRentFrequencyInDays(int chargeRentFrequencyInDays) {
        this.chargeRentFrequencyInDays = chargeRentFrequencyInDays;
    }

    @Override
    public boolean shouldKickNonpayingMembers() {
        return kickNonpayingMembers;
    }

    public void setKickNonpayingMembers(boolean kickNonpayingMembers) {
        this.kickNonpayingMembers = kickNonpayingMembers;
    }

    @Override
    public boolean shouldKickNonpayingAdmins() {
        return kickNonpayingAdmins;
    }

    public void setKickNonpayingAdmins(boolean kickNonpayingAdmins) {
        this.kickNonpayingAdmins = kickNonpayingAdmins;
    }

    @Override
    public String getMaxRentFormula() {
        return maxRentFormula;
    }

    public void setMaxRentFormula(String maxRentFormula) {
        this.maxRentFormula = maxRentFormula;
    }

    @Override
    public String getDisbandFeeFormula() {
        return disbandFeeFormula;
    }

    public void setDisbandFeeFormula(String disbandFeeFormula) {
        this.disbandFeeFormula = disbandFeeFormula;
    }
}
