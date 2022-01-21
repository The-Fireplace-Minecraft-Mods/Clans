package the_fireplace.clans.player;

import java.util.UUID;

public final class PlayerLastSeenData
{
    public static long getLastSeen(UUID player) {
        return PlayerDataStorage.getPlayerData(player).getLastSeen();
    }

    public static void updateLastSeen(UUID player) {
        PlayerDataStorage.getPlayerData(player).updateLastSeen();
    }
}
