package dev.the_fireplace.clans.legacy.player;

import java.util.UUID;

public final class PlayerRaidStats
{
    public static int getRaidWins(UUID player) {
        return PlayerDataStorage.getPlayerData(player).getRaidWins();
    }

    public static int getRaidLosses(UUID player) {
        return PlayerDataStorage.getPlayerData(player).getRaidLosses();
    }

    public static double getRaidWLR(UUID player) {
        return ((double) getRaidWins(player)) / (getRaidLosses(player) > 1 ? (double) getRaidLosses(player) : 1);
    }

    public static void incrementRaidLosses(UUID player) {
        PlayerDataStorage.getPlayerData(player).incrementRaidLosses();
    }

    public static void incrementRaidWins(UUID player) {
        PlayerDataStorage.getPlayerData(player).incrementRaidWins();
    }
}
