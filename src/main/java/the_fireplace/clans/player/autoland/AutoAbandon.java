package the_fireplace.clans.player.autoland;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AutoAbandon {
    private static final Map<UUID, UUID> AUTO_ABANDONING_PLAYERS = new ConcurrentHashMap<>();

    public static void activateAutoAbandon(UUID player, UUID selectedClan) {
        AUTO_ABANDONING_PLAYERS.put(player, selectedClan);
    }

    @Nullable
    public static UUID cancelAutoAbandon(UUID player) {
        return AUTO_ABANDONING_PLAYERS.remove(player);
    }

    public static boolean isAutoAbandoning(UUID player) {
        return AUTO_ABANDONING_PLAYERS.containsKey(player);
    }

    public static UUID getAutoAbandoningClan(UUID player) {
        return AUTO_ABANDONING_PLAYERS.get(player);
    }
}
