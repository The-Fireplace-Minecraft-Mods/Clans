package the_fireplace.clans.cache;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import the_fireplace.clans.Clans;
import the_fireplace.clans.model.OrderedPair;
import the_fireplace.clans.util.ChunkUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class PlayerCache {
    //Clan home warmup cache
    public static Map<EntityPlayerMP, OrderedPair<Integer, UUID>> clanHomeWarmups = Maps.newHashMap();
    private static Map<UUID, PlayerCachedData> playerCache = Maps.newHashMap();

    @Nullable
    public static UUID getPreviousChunkOwner(UUID player) {
        return getPlayerCache(player).prevChunkOwner;
    }

    public static boolean getStoredIsInBorderland(UUID player) {
        return getPlayerCache(player).isInBorderland;
    }

    public static boolean getClaimWarning(UUID player) {
        return getPlayerCache(player).claimWarning;
    }

    public static boolean getIsShowingChunkBorders(UUID player) {
        return getPlayerCache(player).isShowingChunkBorders;
    }

    public static float getClanHomeCheckX(UUID player) {
        return getPlayerCache(player).clanHomeCheckX;
    }

    public static float getClanHomeCheckY(UUID player) {
        return getPlayerCache(player).clanHomeCheckY;
    }

    public static int getPreviousY(UUID player) {
        return getPlayerCache(player).prevY;
    }

    public static float getClanHomeCheckZ(UUID player) {
        return getPlayerCache(player).clanHomeCheckZ;
    }

    public static int getPreviousChunkX(UUID player) {
        return getPlayerCache(player).prevChunkX;
    }

    public static int getPreviousChunkZ(UUID player) {
        return getPlayerCache(player).prevChunkZ;
    }

    //region cached data setters
    public static void setPreviousChunkOwner(UUID player, @Nullable UUID prevChunkOwner, boolean isBorderland) {
        PlayerCachedData data = getPlayerCache(player);
        data.prevChunkOwner = prevChunkOwner;
        data.isInBorderland = isBorderland;
    }

    public static void setClaimWarning(UUID player, boolean claimWarning) {
        getPlayerCache(player).claimWarning = claimWarning;
    }

    public static void setIsShowingChunkBorders(UUID player, boolean isShowingChunkBorders) {
        getPlayerCache(player).isShowingChunkBorders = isShowingChunkBorders;
        if(!isShowingChunkBorders)
            updateBorderDisplayCache(Clans.getMinecraftHelper().getServer().getPlayerList().getPlayerByUUID(player), new BlockPos[0]);
    }

    public static void setClanHomeCheckX(UUID player, float prevX) {
        getPlayerCache(player).clanHomeCheckX = prevX;
    }

    public static void setClanHomeCheckY(UUID player, float prevY) {
        getPlayerCache(player).clanHomeCheckY = prevY;
    }

    public static void setPreviousY(UUID player, int prevY) {
        getPlayerCache(player).prevY = prevY;
    }

    public static void setClanHomeCheckZ(UUID player, float prevZ) {
        getPlayerCache(player).clanHomeCheckZ = prevZ;
    }

    public static void setPreviousChunkX(UUID player, int prevChunkX) {
        getPlayerCache(player).prevChunkX = prevChunkX;
    }

    public static void setPreviousChunkZ(UUID player, int prevChunkZ) {
        getPlayerCache(player).prevChunkZ = prevChunkZ;
    }

    public static void updateBorderDisplayCache(EntityPlayerMP player, BlockPos[] newPositions) {
        List<BlockPos> oldPositions = getPlayerCache(player.getUniqueID()).chunkBorderDisplay;
        ChunkUtils.sendUpdatesToPositions(player.connection, player.world, oldPositions);
        getPlayerCache(player.getUniqueID()).chunkBorderDisplay = Lists.newArrayList(newPositions);
    }

    public static boolean isDisplayingGlowstone(UUID player) {
        return !getPlayerCache(player).chunkBorderDisplay.isEmpty();
    }

    public static void setNeedsCleanup(UUID player, boolean isMarkedForCleanup) {
        getPlayerCache(player).isMarkedForCleanup = isMarkedForCleanup;
    }

    public static void cleanup() {
        for(Map.Entry<UUID, PlayerCachedData> entry: Sets.newHashSet(playerCache.entrySet())) {
            if(entry.getValue().isMarkedForCleanup)
                playerCache.remove(entry.getKey());
        }
    }

    //region getPlayerData
    private static PlayerCachedData getPlayerCache(UUID player) {
        if(!playerCache.containsKey(player))
            playerCache.put(player, new PlayerCachedData());
        return playerCache.get(player);
    }
    //endregion

    private static class PlayerCachedData {
        private boolean isMarkedForCleanup = false;

        //region Cache variables
        @Nullable
        private UUID prevChunkOwner;
        private boolean claimWarning, isInBorderland, isShowingChunkBorders;
        private int prevY, prevChunkX, prevChunkZ;
        private float clanHomeCheckX, clanHomeCheckY, clanHomeCheckZ;
        private List<BlockPos> chunkBorderDisplay = Lists.newArrayList();
        //endregion
    }
}
