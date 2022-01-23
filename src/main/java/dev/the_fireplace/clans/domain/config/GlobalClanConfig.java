package dev.the_fireplace.clans.domain.config;

public interface GlobalClanConfig
{
    boolean allowMultipleLeaders();

    int getMaximumNameLength();

    boolean allowMultipleClanMembership();

    String getNewPlayerDefaultClan();

    double getFormationCost();

    double getInitialBankAmount();

    int getMinimumDistanceBetweenHomes();

    double getInitialClaimSeparationDistanceMultiplier();

    boolean enforceInitialClaimSeparation();
}
