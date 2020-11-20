package the_fireplace.clans.client.clan.land;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import the_fireplace.clans.legacy.model.ChunkPosition;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static the_fireplace.clans.legacy.data.ClaimData.CACHE_SECTION_SIZE;

@SideOnly(Side.CLIENT)
public final class ClientChunkOwnerCache {
    //cacheX, cacheZ, position, owner
    private static final ConcurrentMap<Integer, ConcurrentMap<Integer, ConcurrentMap<ChunkPosition, String>>> chunkOwners = new ConcurrentHashMap<>();

    private static ConcurrentMap<ChunkPosition, String> getCacheSection(ChunkPosition pos) {
        int sectionX = getCacheSectionX(pos);
        int sectionZ = getCacheSectionZ(pos);
        chunkOwners.computeIfAbsent(sectionX, (unused) -> new ConcurrentHashMap<>());
        chunkOwners.get(sectionX).computeIfAbsent(sectionZ, (unused) -> new ConcurrentHashMap<>());
        return chunkOwners.get(sectionX).get(sectionZ);
    }

    private static int getCacheSectionX(ChunkPosition pos) {
        return pos.getPosX() / CACHE_SECTION_SIZE;
    }

    private static int getCacheSectionZ(ChunkPosition pos) {
        return pos.getPosZ() / CACHE_SECTION_SIZE;
    }

    public static void setChunkOwner(ChunkPosition chunkPosition, String clanName) {
        getCacheSection(chunkPosition).put(chunkPosition, clanName);
    }

    public static void clearChunkOwner(ChunkPosition chunkPosition) {
        getCacheSection(chunkPosition).remove(chunkPosition);
    }

    public static String getChunkOwner(ChunkPosition chunkPosition) {
        return getCacheSection(chunkPosition).get(chunkPosition);
    }

    public static boolean hasChunkOwner(ChunkPosition chunkPosition) {
        return getCacheSection(chunkPosition).containsKey(chunkPosition);
    }
}
