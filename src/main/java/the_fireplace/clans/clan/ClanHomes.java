package the_fireplace.clans.clan;

import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ClanHomes {
    private static final Map<Clan, BlockPos> clanHomes = new ConcurrentHashMap<>();

    public static Map<Clan, BlockPos> getClanHomes() {
        ensureClanHomeCacheLoaded();
        return Collections.unmodifiableMap(clanHomes);
    }

    public static boolean isHomeWithinRadiusExcluding(BlockPos centerPoint, long radius, @Nullable BlockPos excludePos) {
        return clanHomes.values().stream().anyMatch(blockPos -> !blockPos.equals(excludePos) && blockPos.getDistance(centerPoint.getX(), centerPoint.getY(), centerPoint.getZ()) < radius);
    }

    public static boolean isHomeWithinRadius(BlockPos centerPoint, long radius) {
        return clanHomes.values().stream().anyMatch(blockPos -> blockPos.getDistance(centerPoint.getX(), centerPoint.getY(), centerPoint.getZ()) < radius);
    }

    static void setClanHome(Clan c, BlockPos home) {
        ensureClanHomeCacheLoaded();
        clanHomes.put(c, home);
    }

    private static void ensureClanHomeCacheLoaded() {
        if(clanHomes.isEmpty())
            for(Clan clan: ClanDatabase.getClans())
                if(clan.hasHome())
                    clanHomes.put(clan, clan.getHome());
    }

    static void clearClanHome(Clan c) {
        clanHomes.remove(c);
    }
}
