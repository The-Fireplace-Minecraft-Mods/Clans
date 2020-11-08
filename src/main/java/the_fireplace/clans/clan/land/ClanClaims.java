package the_fireplace.clans.clan.land;

import the_fireplace.clans.clan.admin.AdminControlledClanSettings;
import the_fireplace.clans.clan.membership.ClanMembers;
import the_fireplace.clans.legacy.ClansModContainer;
import the_fireplace.clans.legacy.data.ClaimData;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClanClaims {
    private static final Map<UUID, ClanClaims> CLAIMS = new ConcurrentHashMap<>();

    public static ClanClaims get(UUID clan) {
        CLAIMS.computeIfAbsent(clan, ClanClaims::new);
        return CLAIMS.get(clan);
    }

    public static void delete(UUID clan) {
        CLAIMS.remove(clan);
    }

    private long cachedClaimCount = -1;
    private final UUID clan;

    public ClanClaims(UUID clan) {
        this.clan = clan;
    }

    public long getClaimCount() {
        cacheClaimCountIfNeeded();
        return cachedClaimCount;
    }

    public void incrementCachedClaimCount() {
        cacheClaimCountIfNeeded();
        cachedClaimCount++;
    }

    public void decrementCachedClaimCount() {
        cacheClaimCountIfNeeded();
        cachedClaimCount--;
    }

    private void cacheClaimCountIfNeeded() {
        if (cachedClaimCount < 0)
            cacheClaimCount();
    }

    /**
     * WARNING: Potentially costly
     */
    public void cacheClaimCount() {
        cachedClaimCount = ClaimData.getClaimedChunkCount(clan);
    }

    public long getMaxClaimCount() {
        AdminControlledClanSettings clanSettings = AdminControlledClanSettings.get(clan);
        if (clanSettings.hasCustomMaxClaims())
            return clanSettings.getCustomMaxClaims();
        //TODO Formula based max claim count
        return ClansModContainer.getConfig().isMultiplyMaxClaimsByPlayers()
            ? ClanMembers.get(clan).getMemberCount() * ClansModContainer.getConfig().getMaxClaims()
            : ClansModContainer.getConfig().getMaxClaims();
    }
}