package the_fireplace.clans.clan;

import io.netty.util.internal.ConcurrentSet;
import the_fireplace.clans.legacy.model.EnumRank;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class ClanMemberCache {
    private static final Map<UUID, Set<Clan>> PLAYER_CLAN_CACHE = new ConcurrentHashMap<>();

    public static Collection<Clan> getClansPlayerIsIn(@Nullable UUID player) {
        if(player == null)
            return Collections.emptySet();
        ensurePlayerClansCached(player);
        return Collections.unmodifiableSet(PLAYER_CLAN_CACHE.get(player));
    }

    public static int countClansPlayerIsIn(@Nullable UUID player) {
        if(player == null)
            return 0;
        ensurePlayerClansCached(player);
        return PLAYER_CLAN_CACHE.get(player).size();
    }

    static void cachePlayerClan(UUID player, Clan clan) {
        ensurePlayerClansCached(player);
        PLAYER_CLAN_CACHE.get(player).add(clan);
    }

    static void uncachePlayerClan(UUID player, Clan clan) {
        ensurePlayerClansCached(player);
        PLAYER_CLAN_CACHE.get(player).remove(clan);
    }

    private static void ensurePlayerClansCached(UUID player) {
        if(!PLAYER_CLAN_CACHE.containsKey(player)) {
            Set<Clan> clansFromDb = new ConcurrentSet<>();
            clansFromDb.addAll(ClanDatabase.lookupPlayerClans(player));
            PLAYER_CLAN_CACHE.put(player, clansFromDb);
        }
    }

    public static EnumRank getPlayerRank(UUID player, Clan clan) {
        return clan.getMembers().get(player);
    }

    static void uncacheClan(Clan c) {
        for(UUID player: PLAYER_CLAN_CACHE.keySet())
            uncachePlayerClan(player, c);
    }
}
