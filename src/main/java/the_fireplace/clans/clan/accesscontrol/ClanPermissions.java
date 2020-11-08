package the_fireplace.clans.clan.accesscontrol;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import the_fireplace.clans.clan.ClanData;
import the_fireplace.clans.clan.membership.ClanMembers;
import the_fireplace.clans.clan.membership.PlayerClans;
import the_fireplace.clans.io.JsonReader;
import the_fireplace.clans.legacy.commands.ClanSubCommand;
import the_fireplace.clans.legacy.commands.CommandClan;
import the_fireplace.clans.legacy.model.EnumRank;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClanPermissions extends ClanData {
    private static final Map<UUID, ClanPermissions> PERMISSION_DATA_INSTANCES = new ConcurrentHashMap<>();

    public static ClanPermissions get(UUID clan) {
        PERMISSION_DATA_INSTANCES.putIfAbsent(clan, new ClanPermissions(clan));
        return PERMISSION_DATA_INSTANCES.get(clan);
    }

    public static void delete(UUID clan) {
        ClanPermissions permissions = PERMISSION_DATA_INSTANCES.remove(clan);
        if(permissions != null)
            permissions.delete();
    }

    public static final Map<String, EnumRank> DEFAULT_PERMISSIONS = new HashMap<String, EnumRank>(CommandClan.COMMANDS.size() + 9, 1) {{
        for(Map.Entry<String, ClanSubCommand> entry: CommandClan.COMMANDS.entrySet())
            if(entry.getValue().getRequiredClanRank().greaterOrEquals(EnumRank.ADMIN) && !entry.getValue().getRequiredClanRank().equals(EnumRank.ANY))
                put(entry.getKey(), entry.getValue().getRequiredClanRank());
        put("access", EnumRank.MEMBER);
        put("interact", EnumRank.MEMBER);
        put("build", EnumRank.MEMBER);
        put("harmmob", EnumRank.MEMBER);
        put("harmanimal", EnumRank.MEMBER);
        put("lockadmin", EnumRank.LEADER);
        put("lock.private", EnumRank.MEMBER);
        put("lock.clan", EnumRank.MEMBER);
        put("lock.open", EnumRank.MEMBER);
    }};
    public final Map<String, EnumRank> permissions;
    public final Map<String, Map<UUID, Boolean>> permissionOverrides = new ConcurrentHashMap<String, Map<UUID, Boolean>>();

    private ClanPermissions(UUID clan) {
        super(clan, "permissions");
        this.permissions = new ConcurrentHashMap<>(CommandClan.COMMANDS.size() + 9, 1);
        for(Map.Entry<String, EnumRank> perm: DEFAULT_PERMISSIONS.entrySet()) {
            permissions.put(perm.getKey(), perm.getValue());
            permissionOverrides.put(perm.getKey(), new ConcurrentHashMap<>());
        }
        loadSavedData();
    }

    public void setPerm(String permission, EnumRank rank) {
        permissions.put(permission, rank);
        markChanged();
    }

    public void addPermissionOverride(String permission, UUID playerId, boolean value) {
        permissionOverrides.get(permission).put(playerId, value);
        markChanged();
    }

    public boolean hasPerm(String permission, @Nullable UUID playerId) {
        if (playerId == null)
            return false;
        if (permissionOverrides.get(permission).containsKey(playerId))
            return permissionOverrides.get(permission).get(playerId);
        else
            return permissions.get(permission).equals(EnumRank.ANY)
                || (ClanMembers.get(clan).isMember(playerId) && PlayerClans.getPlayerRank(playerId, clan).greaterOrEquals(permissions.get(permission)));
    }

    public Map<String, EnumRank> getPermissions() {
        return Collections.unmodifiableMap(permissions);
    }

    public Map<String, Map<UUID, Boolean>> getPermissionOverrides() {
        return Collections.unmodifiableMap(permissionOverrides);
    }

    @Override
    public void readFromJson(JsonReader reader) {
        for(JsonElement e: reader.readArray("permissions")) {
            JsonObject perm = e.getAsJsonObject();
            if(!DEFAULT_PERMISSIONS.containsKey(perm.get("name").getAsString()))
                continue;
            permissions.put(perm.get("name").getAsString(), EnumRank.valueOf(perm.get("value").getAsString()));
            permissionOverrides.put(perm.get("name").getAsString(), new ConcurrentHashMap<>());
            for(JsonElement o: perm.getAsJsonArray("overrides"))
                permissionOverrides.get(perm.get("name").getAsString()).put(UUID.fromString(o.getAsJsonObject().get("player").getAsString()), o.getAsJsonObject().get("allowed").getAsBoolean());
        }
    }

    @Override
    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        JsonArray permissions = new JsonArray();
        for(Map.Entry<String, EnumRank> entry: this.permissions.entrySet()) {
            JsonObject perm = new JsonObject();
            perm.addProperty("name", entry.getKey());
            perm.addProperty("value", entry.getValue().name());
            JsonArray overrides = new JsonArray();
            if(permissionOverrides.containsKey(entry.getKey()))
                for(Map.Entry<UUID, Boolean> override: permissionOverrides.get(entry.getKey()).entrySet()) {
                    JsonObject or = new JsonObject();
                    or.addProperty("player", override.getKey().toString());
                    or.addProperty("allowed", override.getValue());
                    overrides.add(or);
                }
            perm.add("overrides", overrides);
            permissions.add(perm);
        }
        obj.add("permissions", permissions);
        return obj;
    }

    @Override
    protected boolean isDefaultData() {
        return !hasOverrides() && !hasNonDefaultPermission();
    }

    private boolean hasOverrides() {
        for (Map<UUID, Boolean> overrides: permissionOverrides.values())
            if(!overrides.isEmpty())
                return true;
        return false;
    }

    private boolean hasNonDefaultPermission() {
        for (Map.Entry<String, EnumRank> permission: permissions.entrySet())
            if(!permission.getValue().equals(DEFAULT_PERMISSIONS.get(permission.getKey())))
                return true;
        return false;
    }
}