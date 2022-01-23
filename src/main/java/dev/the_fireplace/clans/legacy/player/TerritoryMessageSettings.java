package dev.the_fireplace.clans.legacy.player;

import dev.the_fireplace.clans.legacy.model.TerritoryDisplayMode;

import java.util.UUID;

public final class TerritoryMessageSettings
{
    public static TerritoryDisplayMode getTerritoryDisplayMode(UUID player) {
        return PlayerDataStorage.getPlayerData(player).getTerritoryDisplayMode();
    }

    public static boolean isShowingUndergroundMessages(UUID player) {
        return PlayerDataStorage.getPlayerData(player).isShowingUndergroundMessages();
    }

    public static void setTerritoryDisplayMode(UUID player, TerritoryDisplayMode mode) {
        PlayerDataStorage.getPlayerData(player).setTerritoryDisplayMode(mode);
    }

    public static void setShowUndergroundMessages(UUID player, boolean showUndergroundMessages) {
        PlayerDataStorage.getPlayerData(player).setShowUndergroundMessages(showUndergroundMessages);
    }
}
