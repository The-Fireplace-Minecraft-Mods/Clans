package dev.the_fireplace.clans.legacy.clan.raids;

import com.google.gson.JsonObject;
import dev.the_fireplace.clans.io.JsonReader;
import dev.the_fireplace.clans.legacy.ClansModContainer;
import dev.the_fireplace.clans.legacy.clan.ClanData;
import dev.the_fireplace.clans.legacy.model.Raid;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClanRaidStats extends ClanData
{
    private static final Map<UUID, ClanRaidStats> RAID_STATS_INSTANCES = new ConcurrentHashMap<>();

    public static ClanRaidStats get(UUID clan) {
        RAID_STATS_INSTANCES.computeIfAbsent(clan, ClanRaidStats::new);
        return RAID_STATS_INSTANCES.get(clan);
    }

    public static void delete(UUID clan) {
        ClanRaidStats stats = RAID_STATS_INSTANCES.remove(clan);
        if (stats != null) {
            stats.delete();
        }
    }

    private int wins = 0;
    private int losses = 0;

    private ClanRaidStats(UUID clan) {
        super(clan, "raidstats");
        loadSavedData();
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    public void addWin(Raid raid) {
        wins++;
        //TODO Abstract this out
        if (ClansModContainer.getConfig().isIncreasingRewards()) {
            if (raid.getPartyWlr() >= ClansModContainer.getConfig().getWLRThreshold()) {
                ClanWeaknessFactor.get(clan).decreaseWeaknessFactor();
            }
        }
        markChanged();
    }

    public void addLoss() {
        losses++;
        //TODO Abstract this out
        if (ClansModContainer.getConfig().isIncreasingRewards()) {
            ClanWeaknessFactor.get(clan).increaseWeaknessFactor();
        }
        markChanged();
    }

    @Override
    public void readFromJson(JsonReader reader) {
        wins = reader.readInt("wins", 0);
        losses = reader.readInt("losses", 0);
    }

    @Override
    public JsonObject toJson() {
        JsonObject obj = new JsonObject();

        obj.addProperty("wins", wins);
        obj.addProperty("losses", losses);

        return obj;
    }

    @Override
    protected boolean isDefaultData() {
        return wins == 0 && losses == 0;
    }
}