package the_fireplace.clans.clan.economics;

import com.google.gson.JsonObject;
import the_fireplace.clans.clan.ClanData;
import the_fireplace.clans.clan.admin.AdminControlledClanSettings;
import the_fireplace.clans.io.JsonReader;
import the_fireplace.clans.legacy.ClansModContainer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClanUpkeep extends ClanData {
    private static final Map<UUID, ClanUpkeep> UPKEEP_INSTANCES = new ConcurrentHashMap<>();

    public static ClanUpkeep get(UUID clan) {
        UPKEEP_INSTANCES.putIfAbsent(clan, new ClanUpkeep(clan));
        return UPKEEP_INSTANCES.get(clan);
    }

    public static void delete(UUID clan) {
        ClanUpkeep upkeep = UPKEEP_INSTANCES.remove(clan);
        if(upkeep != null)
            upkeep.delete();
    }

    private long upkeepTimestamp = getNewUpkeepTimestamp();

    private ClanUpkeep(UUID clan) {
        super(clan, "upkeep");
        loadSavedData();
    }

    public long getNextUpkeepTimestamp() {
        return upkeepTimestamp;
    }

    public void updateNextUpkeepTimestamp() {
        this.upkeepTimestamp = getNewUpkeepTimestamp();
        markChanged();
    }

    @Override
    public void readFromJson(JsonReader reader) {
        upkeepTimestamp = reader.readLong("upkeepTimestamp", getNewUpkeepTimestamp());
    }

    @Override
    public JsonObject toJson() {
        JsonObject obj = new JsonObject();

        obj.addProperty("upkeepTimestamp", upkeepTimestamp);

        return obj;
    }

    private long getNewUpkeepTimestamp() {
        return System.currentTimeMillis() + getAdditionalMillisecondsToNextUpkeep();
    }

    private long getAdditionalMillisecondsToNextUpkeep() {
        return ClansModContainer.getConfig().getClanUpkeepDays() * 1000L * 60L * 60L * 24L;
    }

    @Override
    protected boolean isDefaultData() {
        return AdminControlledClanSettings.get(clan).isServerOwned() || AdminControlledClanSettings.get(clan).isUpkeepExempt();
    }
}