package the_fireplace.clans.player.autoland;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class OpAutoClaim
{
    private static final Map<UUID, UUID> AUTO_CLAIMING_PLAYERS = new ConcurrentHashMap<>();

    public static void activateAutoClaim(UUID player, UUID selectedClan) {
        AUTO_CLAIMING_PLAYERS.put(player, selectedClan);
    }

    @Nullable
    public static UUID cancelAutoClaim(UUID player) {
        return AUTO_CLAIMING_PLAYERS.remove(player);
    }

    public static boolean isAutoClaiming(UUID player) {
        return AUTO_CLAIMING_PLAYERS.containsKey(player);
    }

    public static UUID getAutoClaimingClan(UUID player) {
        return AUTO_CLAIMING_PLAYERS.get(player);
    }
}
