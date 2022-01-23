package dev.the_fireplace.clans.legacy.clan.economics;

import com.google.gson.JsonObject;
import dev.the_fireplace.clans.io.JsonReader;
import dev.the_fireplace.clans.legacy.ClansModContainer;
import dev.the_fireplace.clans.legacy.clan.ClanData;
import dev.the_fireplace.clans.legacy.clan.admin.AdminControlledClanSettings;
import dev.the_fireplace.clans.legacy.util.FormulaParser;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClanRent extends ClanData
{
    private static final Map<UUID, ClanRent> RENT_INSTANCES = new ConcurrentHashMap<>();

    public static ClanRent get(UUID clan) {
        RENT_INSTANCES.computeIfAbsent(clan, ClanRent::new);
        return RENT_INSTANCES.get(clan);
    }

    public static void delete(UUID clan) {
        ClanRent rent = RENT_INSTANCES.remove(clan);
        if (rent != null) {
            rent.delete();
        }
    }

    private double rent = getInitialRent();
    private long rentTimestamp = getNewRentTimestamp();

    private ClanRent(UUID clan) {
        super(clan, "rent");
        loadSavedData();
    }

    public double getRent() {
        return rent;
    }

    public void setRent(double rent) {
        this.rent = rent;
        markChanged();
    }

    public long getNextRentTimestamp() {
        return rentTimestamp;
    }

    public void updateNextRentTimestamp() {
        this.rentTimestamp = getNewRentTimestamp();
        markChanged();
    }

    @Override
    public void readFromJson(JsonReader reader) {
        rent = reader.readDouble("rent", rent);
        rentTimestamp = reader.readLong("rentTimestamp", getNewRentTimestamp());
    }

    @Override
    public JsonObject toJson() {
        JsonObject obj = new JsonObject();

        obj.addProperty("rent", rent);
        obj.addProperty("rentTimestamp", rentTimestamp);

        return obj;
    }

    private long getNewRentTimestamp() {
        return System.currentTimeMillis() + getAdditionalMillisecondsToNextRent();
    }

    private long getAdditionalMillisecondsToNextRent() {
        return ClansModContainer.getConfig().getChargeRentDays() * 1000L * 60L * 60L * 24L;
    }

    private double getInitialRent() {
        double maxRent = FormulaParser.eval(ClansModContainer.getConfig().getMaxRentFormula(), clan, 0);
        int rentDays = ClansModContainer.getConfig().getChargeRentDays();
        double upkeep = FormulaParser.eval(ClansModContainer.getConfig().getClanUpkeepCostFormula(), clan, 0);
        int upkeepDays = ClansModContainer.getConfig().getClanUpkeepDays();

        double maxRentPerDay = maxRent / rentDays;
        double upkeepPerDay = upkeep / upkeepDays;

        return Math.min(maxRentPerDay, upkeepPerDay) * rentDays;
    }

    @Override
    protected boolean isDefaultData() {
        return AdminControlledClanSettings.get(clan).isServerOwned();
    }
}