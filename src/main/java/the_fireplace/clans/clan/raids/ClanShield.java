package the_fireplace.clans.clan.raids;

import com.google.gson.JsonObject;
import the_fireplace.clans.clan.ClanData;
import the_fireplace.clans.io.JsonReader;
import the_fireplace.clans.legacy.ClansModContainer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClanShield extends ClanData {
    private static final Map<UUID, ClanShield> SHIELD_INSTANCES = new ConcurrentHashMap<>();

    public static ClanShield get(UUID clan) {
        SHIELD_INSTANCES.computeIfAbsent(clan, ClanShield::new);
        return SHIELD_INSTANCES.get(clan);
    }

    public static void delete(UUID clan) {
        ClanShield shield = SHIELD_INSTANCES.remove(clan);
        if(shield != null)
            shield.delete();
    }

    private long shield = ClansModContainer.getConfig().getInitialShield() * 60;

    private ClanShield(UUID clan) {
        super(clan, "shield");
    }

    /**
     * Add minutes to the clan's shield
     *
     * @param shield number of minutes of shield
     */
    public void addShield(long shield) {
        this.shield += shield;
        markChanged();
    }

    public void setShield(long shield) {
        this.shield = shield;
        markChanged();
    }

    /**
     * This should be called once a minute
     */
    public void decrementShield() {
        if (shield > 0)
            shield--;
        markChanged();
    }

    public boolean isShielded() {
        return shield > 0;
    }

    /**
     * Gets the amount of shield remaining on the clan, in minutes.
     */
    public long getShield() {
        return shield;
    }

    @Override
    public void readFromJson(JsonReader reader) {
        shield = reader.readLong("shield", 0);
    }

    @Override
    public JsonObject toJson() {
        JsonObject obj = new JsonObject();

        obj.addProperty("shield", shield);

        return obj;
    }

    @Override
    protected boolean isDefaultData() {
        return shield == 0;
    }
}