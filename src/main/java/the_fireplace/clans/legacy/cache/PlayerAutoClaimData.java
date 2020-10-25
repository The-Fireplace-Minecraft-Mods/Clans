package the_fireplace.clans.legacy.cache;

import io.netty.util.internal.ConcurrentSet;
import the_fireplace.clans.clan.Clan;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerAutoClaimData {
    //Maps of (Player Unique ID) -> (Clan)
    private static final Map<UUID, Clan> autoAbandonClaims = new ConcurrentHashMap<>();
    private static final Map<UUID, Clan> autoClaimLands = new ConcurrentHashMap<>();
    private static final Set<UUID> opAutoAbandonClaims = new ConcurrentSet<>();
    private static final Map<UUID, Clan> opAutoClaimLands = new ConcurrentHashMap<>();

    public static void activateAutoClaim(UUID player, Clan selectedClan) {
        autoClaimLands.put(player, selectedClan);
    }

    @Nullable
    public static Clan cancelAutoClaim(UUID player) {
        return autoClaimLands.remove(player);
    }

    public static boolean isAutoClaiming(UUID player) {
        return autoClaimLands.containsKey(player);
    }

    public static Clan getAutoClaimingClan(UUID player) {
        return autoClaimLands.get(player);
    }

    public static void activateAutoAbandon(UUID player, Clan selectedClan) {
        autoAbandonClaims.put(player, selectedClan);
    }

    @Nullable
    public static Clan cancelAutoAbandon(UUID player) {
        return autoAbandonClaims.remove(player);
    }

    public static boolean isAutoAbandoning(UUID player) {
        return autoAbandonClaims.containsKey(player);
    }

    public static Clan getAutoAbandoningClan(UUID player) {
        return autoAbandonClaims.get(player);
    }

    public static void activateOpAutoClaim(UUID player, Clan selectedClan) {
        opAutoClaimLands.put(player, selectedClan);
    }

    @Nullable
    public static Clan cancelOpAutoClaim(UUID player) {
        return opAutoClaimLands.remove(player);
    }

    public static boolean isOpAutoClaiming(UUID player) {
        return opAutoClaimLands.containsKey(player);
    }

    public static Clan getOpAutoClaimingClan(UUID player) {
        return opAutoClaimLands.get(player);
    }

    public static void activateOpAutoAbandon(UUID player) {
        opAutoAbandonClaims.add(player);
    }

    public static boolean cancelOpAutoAbandon(UUID player) {
        return opAutoAbandonClaims.remove(player);
    }

    public static boolean isOpAutoAbandoning(UUID player) {
        return opAutoAbandonClaims.contains(player);
    }

    public static void uncacheClaimingSettings(UUID playerId) {
        opAutoClaimLands.remove(playerId);
        opAutoAbandonClaims.remove(playerId);
        autoAbandonClaims.remove(playerId);
        autoClaimLands.remove(playerId);
    }
}
