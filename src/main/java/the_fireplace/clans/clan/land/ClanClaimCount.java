package the_fireplace.clans.clan.land;

import the_fireplace.clans.clan.admin.AdminControlledClanSettings;
import the_fireplace.clans.legacy.ClansModContainer;
import the_fireplace.clans.legacy.data.ClaimData;
import the_fireplace.clans.legacy.util.FormulaParser;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClanClaimCount {
    private static final Map<UUID, ClanClaimCount> CLAIM_COUNT_INSTANCES = new ConcurrentHashMap<>();

    public static ClanClaimCount get(UUID clan) {
        CLAIM_COUNT_INSTANCES.computeIfAbsent(clan, ClanClaimCount::new);
        return CLAIM_COUNT_INSTANCES.get(clan);
    }

    public static void delete(UUID clan) {
        CLAIM_COUNT_INSTANCES.remove(clan);
    }

    private long cachedClaimCount = -1;
    private final UUID clan;

    public ClanClaimCount(UUID clan) {
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
        long claimCount = (long)FormulaParser.eval(ClansModContainer.getConfig().getMaxClaimCountFormula(), clan, -1);
        return claimCount >= 0 ? claimCount : Long.MAX_VALUE;
    }
}