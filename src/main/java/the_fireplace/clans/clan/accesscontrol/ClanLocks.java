package the_fireplace.clans.clan.accesscontrol;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.math.BlockPos;
import the_fireplace.clans.clan.ClanData;
import the_fireplace.clans.clan.membership.ClanMembers;
import the_fireplace.clans.io.JsonReader;
import the_fireplace.clans.legacy.model.EnumLockType;
import the_fireplace.clans.legacy.model.OrderedPair;
import the_fireplace.clans.legacy.util.JsonHelper;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClanLocks extends ClanData
{
    private static final Map<UUID, ClanLocks> LOCK_DATA_INSTANCES = new ConcurrentHashMap<>();

    public static ClanLocks get(UUID clan) {
        LOCK_DATA_INSTANCES.computeIfAbsent(clan, ClanLocks::new);
        return LOCK_DATA_INSTANCES.get(clan);
    }

    public static void delete(UUID clan) {
        ClanLocks locks = LOCK_DATA_INSTANCES.remove(clan);
        if (locks != null) {
            locks.delete();
        }
    }

    private final Map<BlockPos, OrderedPair<EnumLockType, UUID>> locks = new ConcurrentHashMap<>();
    private final Map<BlockPos, Map<UUID, Boolean>> lockOverrides = new ConcurrentHashMap<>();

    private ClanLocks(UUID clan) {
        super(clan, "locks");
        loadSavedData();
    }

    public void addLock(BlockPos pos, EnumLockType type, UUID owner) {
        locks.put(pos, new OrderedPair<>(type, owner));
        markChanged();
    }

    public void delLock(BlockPos pos) {
        if (locks.remove(pos) != null) {
            markChanged();
        }
    }

    public void addLockOverride(BlockPos pos, UUID playerId, boolean value) {
        if (!lockOverrides.containsKey(pos)) {
            lockOverrides.put(pos, new ConcurrentHashMap<>());
        }
        lockOverrides.get(pos).put(playerId, value);
        markChanged();
    }

    public boolean hasLockAccess(BlockPos pos, UUID playerId, String defaultToPermission) {
        if (playerId == null) {
            return false;
        }
        if (ClanPermissions.get(clan).hasPerm("lockadmin", playerId)) {
            return true;
        }
        if (hasLockOverride(pos, playerId)) {
            return lockOverrides.get(pos).get(playerId);
        } else if (locks.containsKey(pos)) {
            OrderedPair<EnumLockType, UUID> lockData = locks.get(pos);
            return getAccessByLockType(lockData.getValue1(), lockData.getValue2(), playerId);
        } else {
            return ClanPermissions.get(clan).hasPerm(defaultToPermission, playerId);
        }
    }

    private boolean hasLockOverride(BlockPos pos, UUID testPlayerId) {
        return lockOverrides.containsKey(pos) && lockOverrides.get(pos).containsKey(testPlayerId);
    }

    private boolean getAccessByLockType(EnumLockType type, UUID lockOwner, UUID testPlayerId) {
        switch (type) {
            case OPEN:
                return true;
            case CLAN:
            default:
                return ClanMembers.get(clan).isMember(testPlayerId);
            case PRIVATE:
                return lockOwner.equals(testPlayerId);
        }
    }

    public boolean isLocked(BlockPos pos) {
        return locks.containsKey(pos);
    }

    public boolean isLockOwner(BlockPos pos, UUID checkOwner) {
        return locks.getOrDefault(pos, new OrderedPair<>(null, UUID.fromString("00000000-0000-0000-0000-000000000000"))).getValue2().equals(checkOwner);
    }

    @Nullable
    public UUID getLockOwner(BlockPos pos) {
        return locks.getOrDefault(pos, new OrderedPair<>(null, null)).getValue2();
    }

    @Nullable
    public EnumLockType getLockType(BlockPos pos) {
        return locks.getOrDefault(pos, new OrderedPair<>(null, null)).getValue1();
    }

    public Map<UUID, Boolean> getLockOverrides(BlockPos pos) {
        return Collections.unmodifiableMap(lockOverrides.getOrDefault(pos, Collections.emptyMap()));
    }

    public void removeLockData(UUID player) {
        for (Map.Entry<BlockPos, OrderedPair<EnumLockType, UUID>> entry : locks.entrySet()) {
            if (entry.getValue().getValue2().equals(player)) {
                locks.remove(entry.getKey());
            }
        }
        for (Map.Entry<BlockPos, Map<UUID, Boolean>> entry : lockOverrides.entrySet()) {
            if (entry.getValue().containsKey(player)) {
                lockOverrides.get(entry.getKey()).remove(player);
            }
        }
    }

    @Override
    public JsonObject toJson() {
        JsonObject obj = new JsonObject();

        JsonArray locks = new JsonArray();
        for (Map.Entry<BlockPos, OrderedPair<EnumLockType, UUID>> entry : this.locks.entrySet()) {
            JsonObject lock = new JsonObject();
            lock.add("position", JsonHelper.toJsonObject(entry.getKey()));
            lock.addProperty("type", entry.getValue().getValue1().ordinal());
            lock.addProperty("owner", entry.getValue().getValue2().toString());
            JsonArray overrides = new JsonArray();
            if (this.lockOverrides.containsKey(entry.getKey())) {
                for (Map.Entry<UUID, Boolean> override : this.lockOverrides.get(entry.getKey()).entrySet()) {
                    JsonObject or = new JsonObject();
                    or.addProperty("player", override.getKey().toString());
                    or.addProperty("allowed", override.getValue());
                    overrides.add(or);
                }
            }
            lock.add("overrides", overrides);
            locks.add(lock);
        }
        obj.add("locks", locks);

        return obj;
    }

    @Override
    public void readFromJson(JsonReader reader) {
        for (JsonElement e : reader.readArray("locks")) {
            JsonObject lock = e.getAsJsonObject();
            BlockPos pos = JsonHelper.fromJsonObject(lock.get("position").getAsJsonObject());
            locks.put(pos, new OrderedPair<>(EnumLockType.values()[lock.get("type").getAsInt()], UUID.fromString(lock.get("owner").getAsString())));
            lockOverrides.put(pos, new ConcurrentHashMap<>());
            for (JsonElement o : lock.getAsJsonArray("overrides")) {
                lockOverrides.get(pos).put(UUID.fromString(o.getAsJsonObject().get("player").getAsString()), o.getAsJsonObject().get("allowed").getAsBoolean());
            }
        }
    }

    @Override
    protected boolean isDefaultData() {
        return locks.isEmpty() && lockOverrides.isEmpty();
    }
}