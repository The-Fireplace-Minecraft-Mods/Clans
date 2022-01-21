package the_fireplace.clans.player.autoland;

import io.netty.util.internal.ConcurrentSet;

import java.util.Set;
import java.util.UUID;

public class OpAutoAbandon
{
    private static final Set<UUID> AUTO_ABANDONING_PLAYERS = new ConcurrentSet<>();

    public static void activateOpAutoAbandon(UUID player) {
        AUTO_ABANDONING_PLAYERS.add(player);
    }

    public static boolean cancelOpAutoAbandon(UUID player) {
        return AUTO_ABANDONING_PLAYERS.remove(player);
    }

    public static boolean isOpAutoAbandoning(UUID player) {
        return AUTO_ABANDONING_PLAYERS.contains(player);
    }
}
