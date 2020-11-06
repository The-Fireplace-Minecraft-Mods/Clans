package the_fireplace.clans.clan.admin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import the_fireplace.clans.clan.ClanData;
import the_fireplace.clans.io.JsonReader;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AdminControlledClanSettings extends ClanData {
    private static final Map<UUID, AdminControlledClanSettings> ADMIN_CONTROLLED_SETTINGS_INSTANCES = new ConcurrentHashMap<>();

    public static AdminControlledClanSettings get(UUID clan) {
        ADMIN_CONTROLLED_SETTINGS_INSTANCES.putIfAbsent(clan, new AdminControlledClanSettings(clan));
        return ADMIN_CONTROLLED_SETTINGS_INSTANCES.get(clan);
    }

    public static void delete(UUID clan) {
        AdminControlledClanSettings adminControlledSettings = ADMIN_CONTROLLED_SETTINGS_INSTANCES.remove(clan);
        if(adminControlledSettings != null)
            adminControlledSettings.delete();
    }

    //TODO this desperately needs an overhaul. A new system should probably do a better job at handling the different
    // setting types, since right now it's a mix of integers (which in some cases should really be longs or doubles),
    // tristates, and booleans
    private static final Map<String, Integer> DEFAULT_SETTINGS = new HashMap<>(8, 1);

    private static final String MAX_CLAIMS = "maxclaims";
    private static final String MOB_SPAWNING = "mobspawning";
    private static final String CLAIM_COST = "claimcost";

    private static final String MOB_DAMAGE = "mobdamage";
    private static final String UPKEEP_EXEMPTION = "upkeepexemption";
    private static final String DYNMAP_VISIBLE = "dynmapvisible";
    private static final String SERVER = "server";
    private static final String PVP = "pvp";

    static {
        //Config option overrides
        DEFAULT_SETTINGS.put(MAX_CLAIMS, -1);
        DEFAULT_SETTINGS.put(MOB_SPAWNING, -1);
        DEFAULT_SETTINGS.put(CLAIM_COST, -1);
        //Custom properties
        DEFAULT_SETTINGS.put(MOB_DAMAGE, 1);
        DEFAULT_SETTINGS.put(UPKEEP_EXEMPTION, 0);
        DEFAULT_SETTINGS.put(DYNMAP_VISIBLE, 1);
        DEFAULT_SETTINGS.put(SERVER, 0);
        DEFAULT_SETTINGS.put(PVP, -1);
    }
    public final Map<String, Integer> settings = new ConcurrentHashMap<>(8, 1);

    private AdminControlledClanSettings(UUID clan) {
        super(clan, "adminsettings");
        for(Map.Entry<String, Integer> opt: AdminControlledClanSettings.DEFAULT_SETTINGS.entrySet())
            settings.put(opt.getKey(), opt.getValue());
        loadSavedData();
    }

    public void setOption(String option, int value) {
        settings.put(option, value);
        markChanged();
    }

    public boolean hasCustomMaxClaims() {
        return settings.get(MAX_CLAIMS) >= 0;
    }

    public int getCustomMaxClaims() {
        return settings.get(MAX_CLAIMS);
    }

    public void setCustomMaxClaims(int newCustomMaxClaims) {
        setOption(MAX_CLAIMS, newCustomMaxClaims);
    }

    public boolean hasMobSpawnOverride() {
        return inBooleanRange(settings.get(MOB_SPAWNING));
    }

    public boolean getMobSpawnOverride() {
        return settings.get(MOB_SPAWNING) == 1;
    }

    public void setMobSpawnOverride(Boolean newOverride) {
        setOption(MOB_SPAWNING, newOverride == null ? -1 : (Boolean.FALSE.equals(newOverride) ? 0 : 1));
    }

    public boolean hasCustomClaimCost() {
        return settings.get(CLAIM_COST) >= 0;
    }

    public int getCustomClaimCost() {
        return settings.get(CLAIM_COST);
    }

    public void setCustomClaimCost(int newCustomClaimCost) {
        setOption(CLAIM_COST, newCustomClaimCost);
    }

    public boolean isMobDamageAllowed() {
        return settings.get(MOB_DAMAGE) == 1;
    }

    public void setMobDamageAllowed(boolean mobDamageAllowed) {
        setOption(MOB_DAMAGE, mobDamageAllowed ? 1 : 0);
    }

    public boolean isUpkeepExempt() {
        return settings.get(UPKEEP_EXEMPTION) == 1;
    }

    public void setUpkeepExempt(boolean upkeepExempt) {
        setOption(UPKEEP_EXEMPTION, upkeepExempt ? 1 : 0);
    }

    public boolean isVisibleOnDynmap() {
        return settings.get(DYNMAP_VISIBLE) == 1;
    }

    public void setVisibleOnDynmap(boolean visibleOnDynmap) {
        setOption(DYNMAP_VISIBLE, visibleOnDynmap ? 1 : 0);
    }

    public boolean isServerOwned() {
        return settings.get(SERVER) == 1;
    }

    public void setServerOwned(boolean server) {
        setOption(SERVER, server ? 1 : 0);
    }

    public boolean hasPVPOverride() {
        int pvpSetting = settings.get(PVP);
        return inBooleanRange(pvpSetting) || isServerOwned();
    }

    public boolean getPVPOverride() {
        int pvpSetting = settings.get(PVP);
        return inBooleanRange(pvpSetting)
            ? pvpSetting == 1
            : !isServerOwned();
    }

    public void setPVPOverride(Boolean newOverride) {
        setOption(PVP, newOverride == null ? -1 : (Boolean.FALSE.equals(newOverride) ? 0 : 1));
    }

    public boolean isUnraidable() {
        return isServerOwned() || (hasPVPOverride() && !getPVPOverride());
    }

    private boolean inBooleanRange(int testValue) {
        return testValue == 0 || testValue == 1;
    }

    @Override
    public void readFromJson(JsonReader reader) {
        for(JsonElement e: reader.readArray("options")) {
            JsonObject perm = e.getAsJsonObject();
            if(!DEFAULT_SETTINGS.containsKey(perm.get("name").getAsString()))
                continue;
            settings.put(perm.get("name").getAsString(), perm.get("value").getAsInt());
        }
    }

    @Override
    public JsonObject toJson() {
        JsonObject obj = new JsonObject();

        JsonArray options = new JsonArray();
        for(Map.Entry<String, Integer> entry: this.settings.entrySet()) {
            JsonObject opt = new JsonObject();
            opt.addProperty("name", entry.getKey());
            opt.addProperty("value", entry.getValue());
            options.add(opt);
        }
        obj.add("options", options);

        return obj;
    }
}