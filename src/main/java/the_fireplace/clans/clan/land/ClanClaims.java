package the_fireplace.clans.clan.land;

import com.google.gson.JsonObject;
import the_fireplace.clans.clan.ClanData;
import the_fireplace.clans.clan.admin.AdminControlledClanSettings;
import the_fireplace.clans.clan.membership.ClanMembers;
import the_fireplace.clans.io.JsonReader;
import the_fireplace.clans.legacy.ClansModContainer;
import the_fireplace.clans.legacy.data.ClaimData;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClanClaims extends ClanData {
    private static final Map<UUID, ClanClaims> CLAIMS = new ConcurrentHashMap<>();

    public static ClanClaims get(UUID clan) {
        CLAIMS.putIfAbsent(clan, new ClanClaims(clan));
        return CLAIMS.get(clan);
    }

    public static void delete(UUID clan) {
        ClanClaims claims = CLAIMS.remove(clan);
        if(claims != null)
            claims.delete();
    }

    private long cachedClaimCount = -1;

    public ClanClaims(UUID clan) {
        super(clan, "claims");
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

    void cacheClaimCountIfNeeded() {
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

    @Override
    public void readFromJson(JsonReader reader) {
        //Do nothing
    }

    @Override
    public JsonObject toJson() {
        return new JsonObject();
    }
}