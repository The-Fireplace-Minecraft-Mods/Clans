package the_fireplace.clans.player;

import io.netty.util.internal.ConcurrentSet;
import the_fireplace.clans.multithreading.ConcurrentExecutionManager;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class InvitedPlayers
{
    //Map of Clan ID -> Set of invited players
    private static final Map<UUID, Set<UUID>> INVITED_PLAYERS_CACHE = new ConcurrentHashMap<>();

    public static Collection<UUID> getInvitedPlayers(UUID clanId) {
        INVITED_PLAYERS_CACHE.computeIfAbsent(clanId, (unused) -> new ConcurrentSet<>());
        return INVITED_PLAYERS_CACHE.get(clanId);
    }

    public static Collection<UUID> getReceivedInvites(UUID player) {
        return Collections.unmodifiableCollection(PlayerDataStorage.getPlayerData(player).getInvites());
    }

    /**
     * Adds an invite from a clan to a player's data.
     *
     * @return true if the player did not already have an invite pending from that clan, false otherwise.
     */
    public static boolean addInvite(UUID player, UUID clan) {
        cacheInvite(clan, player);
        return PlayerDataStorage.getPlayerData(player).addInvite(clan);
    }

    private static void cacheInvite(UUID clanId, UUID playerId) {
        INVITED_PLAYERS_CACHE.computeIfAbsent(clanId, (unused) -> new ConcurrentSet<>());
        INVITED_PLAYERS_CACHE.get(clanId).add(playerId);
    }

    /**
     * Removes an invite from a clan from a player's data.
     *
     * @return true if the invite was removed, or false if they didn't have a pending invite from the specified clan.
     */
    public static boolean removeInvite(UUID player, UUID clan) {
        uncacheInvite(clan, player);
        return PlayerDataStorage.getPlayerData(player).removeInvite(clan);
    }

    private static void uncacheInvite(UUID clanId, UUID playerId) {
        INVITED_PLAYERS_CACHE.computeIfAbsent(clanId, (unused) -> new ConcurrentSet<>());
        INVITED_PLAYERS_CACHE.get(clanId).remove(playerId);
    }

    public static void clearReceivedInvites(UUID player) {
        for (UUID clan : INVITED_PLAYERS_CACHE.keySet()) {
            removeInvite(player, clan);
        }
    }

    /**
     * Adds an invite block for a clan to a player's data. This also deletes any existing invites from the clan being blocked.
     *
     * @return true if the player did not already block that clan, false otherwise.
     */
    public static boolean addInviteBlock(UUID player, UUID clan) {
        boolean blockAddedSuccessfully = PlayerDataStorage.getPlayerData(player).addInviteBlock(clan);
        if (blockAddedSuccessfully) {
            removeInvite(player, clan);
        }
        return blockAddedSuccessfully;
    }

    /**
     * Removes an invite block for a clan from a player's data.
     *
     * @return true if the clan was unblocked, or false if the specified clan wasn't blocked.
     */
    public static boolean removeInviteBlock(UUID player, UUID clan) {
        return PlayerDataStorage.getPlayerData(player).removeInviteBlock(clan);
    }

    public static void setGlobalInviteBlock(UUID player, boolean block) {
        PlayerDataStorage.getPlayerData(player).setGlobalInviteBlock(block);
    }

    public static Collection<UUID> getBlockedClans(UUID player) {
        return Collections.unmodifiableCollection(PlayerDataStorage.getPlayerData(player).getBlockedClans());
    }

    public static boolean isBlockingAllInvites(UUID player) {
        return PlayerDataStorage.getPlayerData(player).getGlobalInviteBlock();
    }

    /**
     * Load all player data on another thread. Only do this when the server is starting.
     */
    public static void loadInvitedPlayers() {
        ConcurrentExecutionManager.runKillable(() -> {
            File[] files = PlayerDataStorage.PLAYER_DATA_LOCATION.listFiles();
            if (files != null) {
                for (File f : files) {
                    try {
                        UUID playerId = UUID.fromString(f.getName().replace(".json", ""));
                        for (UUID clanId : getReceivedInvites(playerId)) {
                            cacheInvite(clanId, playerId);
                        }
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
        });
    }
}
