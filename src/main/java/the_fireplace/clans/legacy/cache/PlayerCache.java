package the_fireplace.clans.legacy.cache;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import the_fireplace.clans.clan.home.ClanHomes;
import the_fireplace.clans.clan.membership.ClanMembers;
import the_fireplace.clans.legacy.ClansModContainer;
import the_fireplace.clans.legacy.model.OrderedPair;
import the_fireplace.clans.legacy.util.ChunkUtils;
import the_fireplace.clans.legacy.util.EntityUtil;
import the_fireplace.clans.legacy.util.TextStyles;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerCache {
    private static final Map<UUID, UUID> clanChattingPlayers = new ConcurrentHashMap<>();
    private static final Map<EntityPlayerMP, OrderedPair<Integer, UUID>> clanHomeWarmups = new ConcurrentHashMap<>();
    private static final Map<UUID, PlayerCachedData> playerCache = new ConcurrentHashMap<>();

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
            updateBorderDisplayCache(ClansModContainer.getMinecraftHelper().getServer().getPlayerList().getPlayerByUUID(player), new BlockPos[0]);
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
        if(isMarkedForCleanup)
            clanChattingPlayers.remove(player);
    }

    public static void cleanup() {
        for(Map.Entry<UUID, PlayerCachedData> entry: Sets.newHashSet(playerCache.entrySet())) {
            if(entry.getValue().isMarkedForCleanup)
                playerCache.remove(entry.getKey());
        }
    }

    private static PlayerCachedData getPlayerCache(UUID player) {
        playerCache.computeIfAbsent(player, (unused) -> new PlayerCachedData());
        return playerCache.get(player);
    }

    public static boolean isClanChatting(UUID player) {
        return clanChattingPlayers.containsKey(player);
    }

    public static UUID getChattingWithClan(EntityPlayer player) {
        return clanChattingPlayers.get(player.getUniqueID());
    }

    public static void toggleClanChat(UUID uuid, UUID clan) {
        if(clanChattingPlayers.containsKey(uuid) && clanChattingPlayers.get(uuid).equals(clan))
            clanChattingPlayers.remove(uuid);
        else
            clanChattingPlayers.put(uuid, clan);
    }

    public static boolean hasPlayerMovedSinceTeleportStarted(UUID player, Vec3d position) {
        return !(Math.abs(position.x - getClanHomeCheckX(player)) < 0.1f && Math.abs(position.z - getClanHomeCheckZ(player)) < 0.1f && Math.abs(position.y - getClanHomeCheckY(player)) < 0.1f);
    }

    public static void startHomeTeleportWarmup(EntityPlayerMP sender, UUID clan) {
        clanHomeWarmups.put(sender, new OrderedPair<>(ClansModContainer.getConfig().getClanHomeWarmupTime(), clan));
    }

    public static void decrementHomeWarmupTimers() {
        for(Map.Entry<EntityPlayerMP, OrderedPair<Integer, UUID>> entry : clanHomeWarmups.entrySet()) {
            if (entry.getValue().getValue1() > 0) {
                if(!hasPlayerMovedSinceTeleportStarted(entry.getKey().getUniqueID(), entry.getKey().getPositionVector()))
                    clanHomeWarmups.put(entry.getKey(), new OrderedPair<>(entry.getValue().getValue1() - 1, entry.getValue().getValue2()));
                else {
                    cancelClanHomeWarmup(entry.getKey());
                    entry.getKey().sendMessage(TranslationUtil.getTranslation(entry.getKey().getUniqueID(), "commands.clan.home.cancelled").setStyle(TextStyles.RED));
                    continue;
                }
            } else
                clanHomeWarmups.remove(entry.getKey());

            if (entry.getValue().getValue1() == 0 && entry.getKey() != null && entry.getKey().isEntityAlive()) {
                UUID clan = entry.getValue().getValue2();
                //Ensure that the clan still has a home and that the player is still in the clan before teleporting.
                if(clan != null && ClanHomes.hasHome(clan) && ClanMembers.get(clan).getMembers().contains(entry.getKey().getUniqueID()))
                    EntityUtil.teleportHome(entry.getKey(), ClanHomes.get(clan).toBlockPos(), ClanHomes.get(clan).getHomeDim(), entry.getKey().dimension, false);
                else
                    entry.getKey().sendMessage(TranslationUtil.getTranslation(entry.getKey().getUniqueID(), "commands.clan.home.cancelled").setStyle(TextStyles.RED));
            }
        }
    }

    public static boolean cancelClanHomeWarmup(EntityPlayerMP player) {
        return clanHomeWarmups.remove(player) != null;
    }

    private static class PlayerCachedData {
        private boolean isMarkedForCleanup = false;

        @Nullable
        private UUID prevChunkOwner;
        private boolean claimWarning, isInBorderland, isShowingChunkBorders;
        private int prevY, prevChunkX, prevChunkZ;
        private float clanHomeCheckX, clanHomeCheckY, clanHomeCheckZ;
        private List<BlockPos> chunkBorderDisplay = Lists.newArrayList();
    }
}
