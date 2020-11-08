package the_fireplace.clans.clan.membership;

import io.netty.util.internal.ConcurrentSet;
import the_fireplace.clans.legacy.model.EnumRank;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerClans {
    private static final Map<UUID, Set<UUID>> PLAYER_CLAN_CACHE = new ConcurrentHashMap<>();

    public static Collection<UUID> getClansPlayerIsIn(UUID player) {
        if(player == null)
            return Collections.emptySet();
        ensurePlayerClansCached(player);
        return Collections.unmodifiableSet(PLAYER_CLAN_CACHE.get(player));
    }

    public static int countClansPlayerIsIn(UUID player) {
        if(player == null)
            return 0;
        ensurePlayerClansCached(player);
        return PLAYER_CLAN_CACHE.get(player).size();
    }

    static void cachePlayerClan(UUID player, UUID clan) {
        ensurePlayerClansCached(player);
        PLAYER_CLAN_CACHE.get(player).add(clan);
    }

    static void uncachePlayerClan(UUID player, UUID clan) {
        ensurePlayerClansCached(player);
        PLAYER_CLAN_CACHE.get(player).remove(clan);
    }

    private static void ensurePlayerClansCached(UUID player) {
        if (!PLAYER_CLAN_CACHE.containsKey(player)) {
            Set<UUID> clansFromDb = new ConcurrentSet<>();
            clansFromDb.addAll(ClanMembers.lookupPlayerClans(player));
            PLAYER_CLAN_CACHE.put(player, clansFromDb);
        }
    }

    public static EnumRank getPlayerRank(UUID player, UUID clan) {
        return ClanMembers.get(clan).getMemberRanks().get(player);
    }

    static void uncacheClan(UUID c) {
        for(UUID player: PLAYER_CLAN_CACHE.keySet())
            uncachePlayerClan(player, c);
    }
}
