package the_fireplace.clans.clan.home;

import com.google.gson.JsonObject;
import net.minecraft.util.math.BlockPos;
import the_fireplace.clans.clan.ClanData;
import the_fireplace.clans.clan.ClanIdRegistry;
import the_fireplace.clans.io.JsonReader;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClanHomes extends ClanData
{
    private static final Map<UUID, ClanHomes> HOME_INSTANCES = new ConcurrentHashMap<>();
    private static boolean cacheLoaded = false;

    public static boolean hasHome(UUID clan) {
        loadIfAbsent(clan);
        return HOME_INSTANCES.containsKey(clan);
    }

    public static ClanHomes get(UUID clan) {
        return HOME_INSTANCES.get(clan);
    }

    public static void delete(UUID clan) {
        ClanHomes upkeep = HOME_INSTANCES.remove(clan);
        if (upkeep != null) {
            upkeep.delete();
        }
    }

    public static Map<UUID, ClanHomes> getClanHomes() {
        ensureClanHomeCacheLoaded();
        return Collections.unmodifiableMap(HOME_INSTANCES);
    }

    public static boolean isHomeWithinRadiusExcluding(BlockPos centerPoint, long radius, BlockPos excludePos) {
        return HOME_INSTANCES.values().stream().anyMatch(clanHome -> !clanHome.toBlockPos().equals(excludePos) && clanHome.toBlockPos().getDistance(centerPoint.getX(), centerPoint.getY(), centerPoint.getZ()) < radius);
    }

    public static boolean isHomeWithinRadius(BlockPos centerPoint, long radius) {
        return HOME_INSTANCES.values().stream().anyMatch(clanHome -> clanHome.toBlockPos().getDistance(centerPoint.getX(), centerPoint.getY(), centerPoint.getZ()) < radius);
    }

    public static void set(UUID clan, BlockPos home, int dimension) {
        if (!hasHome(clan)) {
            HOME_INSTANCES.put(clan, new ClanHomes(clan));
        }
        get(clan).setHome(home, dimension);
    }

    private static void ensureClanHomeCacheLoaded() {
        if (!cacheLoaded) {
            for (UUID clan : ClanIdRegistry.getIds()) {
                loadIfAbsent(clan);
            }
        }
        cacheLoaded = true;
    }

    private static void loadIfAbsent(UUID clan) {
        if (!cacheLoaded) {
            ClanHomes loadHome = new ClanHomes(clan);
            if (loadHome.loadSavedData()) {
                HOME_INSTANCES.putIfAbsent(clan, loadHome);
            } else {
                loadHome.delete();
            }
        }
    }

    private float homeX;
    private float homeY;
    private float homeZ;
    private int homeDimension = Integer.MIN_VALUE;

    private ClanHomes(UUID clan) {
        super(clan, "home");
    }

    private void setHome(BlockPos pos, int dimension) {
        this.homeX = pos.getX();
        this.homeY = pos.getY();
        this.homeZ = pos.getZ();
        this.homeDimension = dimension;
        markChanged();
    }

    public BlockPos toBlockPos() {
        return new BlockPos(homeX, homeY, homeZ);
    }

    public int getHomeDim() {
        return homeDimension;
    }

    @Override
    public void readFromJson(JsonReader reader) {
        if (reader.readBool("hasHome", true)) {
            homeX = reader.readFloat("homeX", 0);
            homeY = reader.readFloat("homeY", 0);
            homeZ = reader.readFloat("homeZ", 0);
            homeDimension = reader.readInt("homeDimension", Integer.MIN_VALUE);
        }
    }

    @Override
    public JsonObject toJson() {
        JsonObject obj = new JsonObject();

        obj.addProperty("homeX", homeX);
        obj.addProperty("homeY", homeY);
        obj.addProperty("homeZ", homeZ);
        obj.addProperty("homeDimension", homeDimension);

        return obj;
    }

    @Override
    protected boolean isDefaultData() {
        return homeDimension == Integer.MIN_VALUE;
    }
}
