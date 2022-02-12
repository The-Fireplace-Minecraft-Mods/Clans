package dev.the_fireplace.clans.config.state;

import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.clans.api.config.injectables.GlobalClanConfig;
import dev.the_fireplace.lib.api.io.interfaces.access.StorageReadBuffer;
import dev.the_fireplace.lib.api.io.interfaces.access.StorageWriteBuffer;
import dev.the_fireplace.lib.api.lazyio.injectables.ConfigStateManager;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Implementation
@Singleton
public final class GlobalClanConfigState extends ClansConfigState implements GlobalClanConfig
{
    private final GlobalClanConfig defaultConfig;

    private boolean allowMultipleLeaders;
    private boolean allowMultipleClanMembership;
    private int maximumNameLength;
    private String newPlayerDefaultClan;
    private int minimumDistanceBetweenHomes;
    private double initialClaimSeparationDistanceMultiplier;
    private boolean enforceInitialClaimSeparation;
    private double formationCost;
    private double initialBankAmount;

    @Inject
    public GlobalClanConfigState(ConfigStateManager configStateManager, @Named("default") GlobalClanConfig defaultConfig) {
        this.defaultConfig = defaultConfig;
        configStateManager.initialize(this);
    }

    @Override
    public String getId() {
        return "clan_globals";
    }

    @Override
    public void readFrom(StorageReadBuffer buffer) {
        allowMultipleLeaders = buffer.readBool("allowMultipleLeaders", defaultConfig.allowMultipleLeaders());
        allowMultipleClanMembership = buffer.readBool("allowMultipleClanMembership", defaultConfig.allowMultipleClanMembership());
        maximumNameLength = buffer.readInt("maximumNameLength", defaultConfig.getMaximumNameLength());
        newPlayerDefaultClan = buffer.readString("newPlayerDefaultClan", defaultConfig.getNewPlayerDefaultClan());
        minimumDistanceBetweenHomes = buffer.readInt("minimumDistanceBetweenHomes", defaultConfig.getMinimumDistanceBetweenHomes());
        initialClaimSeparationDistanceMultiplier = buffer.readDouble("initialClaimSeparationDistanceMultiplier", defaultConfig.getInitialClaimSeparationDistanceMultiplier());
        enforceInitialClaimSeparation = buffer.readBool("enforceInitialClaimSeparation", defaultConfig.enforceInitialClaimSeparation());
        formationCost = buffer.readDouble("formationCost", defaultConfig.getFormationCost());
        initialBankAmount = buffer.readDouble("initialBankAmount", defaultConfig.getInitialBankAmount());
    }

    @Override
    public void writeTo(StorageWriteBuffer buffer) {
        buffer.writeBool("allowMultipleLeaders", allowMultipleLeaders);
        buffer.writeBool("allowMultipleClanMembership", allowMultipleClanMembership);
        buffer.writeInt("maximumNameLength", maximumNameLength);
        buffer.writeString("newPlayerDefaultClan", newPlayerDefaultClan);
        buffer.writeInt("minimumDistanceBetweenHomes", minimumDistanceBetweenHomes);
        buffer.writeDouble("initialClaimSeparationDistanceMultiplier", initialClaimSeparationDistanceMultiplier);
        buffer.writeBool("enforceInitialClaimSeparation", enforceInitialClaimSeparation);
        buffer.writeDouble("formationCost", formationCost);
        buffer.writeDouble("initialBankAmount", initialBankAmount);
    }

    @Override
    public boolean allowMultipleLeaders() {
        return allowMultipleLeaders;
    }

    public void setAllowMultipleLeaders(boolean allowMultipleLeaders) {
        this.allowMultipleLeaders = allowMultipleLeaders;
    }

    @Override
    public int getMaximumNameLength() {
        return maximumNameLength;
    }

    public void setMaximumNameLength(int maximumNameLength) {
        this.maximumNameLength = maximumNameLength;
    }

    @Override
    public boolean allowMultipleClanMembership() {
        return allowMultipleClanMembership;
    }

    public void setAllowMultipleClanMembership(boolean allowMultipleClanMembership) {
        this.allowMultipleClanMembership = allowMultipleClanMembership;
    }

    @Override
    public String getNewPlayerDefaultClan() {
        return newPlayerDefaultClan;
    }

    public void setNewPlayerDefaultClan(String newPlayerDefaultClan) {
        this.newPlayerDefaultClan = newPlayerDefaultClan;
    }

    @Override
    public double getFormationCost() {
        return formationCost;
    }

    public void setFormationCost(double formationCost) {
        this.formationCost = formationCost;
    }

    @Override
    public double getInitialBankAmount() {
        return initialBankAmount;
    }

    public void setInitialBankAmount(double initialBankAmount) {
        this.initialBankAmount = initialBankAmount;
    }

    @Override
    public int getMinimumDistanceBetweenHomes() {
        return minimumDistanceBetweenHomes;
    }

    public void setMinimumDistanceBetweenHomes(int minimumDistanceBetweenHomes) {
        this.minimumDistanceBetweenHomes = minimumDistanceBetweenHomes;
    }

    @Override
    public double getInitialClaimSeparationDistanceMultiplier() {
        return initialClaimSeparationDistanceMultiplier;
    }

    public void setInitialClaimSeparationDistanceMultiplier(double initialClaimSeparationDistanceMultiplier) {
        this.initialClaimSeparationDistanceMultiplier = initialClaimSeparationDistanceMultiplier;
    }

    @Override
    public boolean enforceInitialClaimSeparation() {
        return enforceInitialClaimSeparation;
    }

    public void setEnforceInitialClaimSeparation(boolean enforceInitialClaimSeparation) {
        this.enforceInitialClaimSeparation = enforceInitialClaimSeparation;
    }
}
