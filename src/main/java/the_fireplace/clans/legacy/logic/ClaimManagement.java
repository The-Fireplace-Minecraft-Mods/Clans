package the_fireplace.clans.legacy.logic;

import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.api.event.PreLandAbandonEvent;
import the_fireplace.clans.api.event.PreLandClaimEvent;
import the_fireplace.clans.clan.ClanIdRegistry;
import the_fireplace.clans.clan.accesscontrol.ClanPermissions;
import the_fireplace.clans.clan.admin.AdminControlledClanSettings;
import the_fireplace.clans.clan.economics.ClanClaimCosts;
import the_fireplace.clans.clan.home.ClanHomes;
import the_fireplace.clans.clan.land.ClanClaimCount;
import the_fireplace.clans.clan.metadata.ClanNames;
import the_fireplace.clans.economy.Economy;
import the_fireplace.clans.legacy.ClansModContainer;
import the_fireplace.clans.legacy.cache.PlayerCache;
import the_fireplace.clans.legacy.data.ClaimData;
import the_fireplace.clans.legacy.model.ChunkPosition;
import the_fireplace.clans.legacy.model.ChunkPositionWithData;
import the_fireplace.clans.legacy.util.*;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;
import the_fireplace.clans.multithreading.ConcurrentExecutionManager;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ClaimManagement {
    public static boolean checkCanClaimRadius(EntityPlayerMP claimingPlayer, UUID claimingClan, int radius, String radiusMode) {
        if(radiusMode.equalsIgnoreCase("square")) {
            int requestedClaimCount = radius * radius;
            long currentClaimCount = ClanClaimCount.get(claimingClan).getClaimCount();
            if(!AdminControlledClanSettings.get(claimingClan).isServerOwned()) {
                long maxClaimCount = ClanClaimCount.get(claimingClan).getMaxClaimCount();
                if(requestedClaimCount + currentClaimCount > maxClaimCount) {//TODO this doesn't account for claims already made within the radius
                    claimingPlayer.sendMessage(TranslationUtil.getTranslation(claimingPlayer.getUniqueID(), "commands.clan.claim.maxed_r", requestedClaimCount, ClanNames.get(claimingClan).getName(), maxClaimCount, currentClaimCount));
                    return false;
                }
                if (unableToAffordClaims(claimingPlayer, claimingClan, requestedClaimCount, currentClaimCount))
                    return false;
            }
            //Do a connection check if connected claims are enforced, this is not the first claim, and the clan is not a server clan
            boolean doEdgeConnectionCheck = ClansModContainer.getConfig().isForceConnectedClaims() && currentClaimCount != 0 && !AdminControlledClanSettings.get(claimingClan).isServerOwned();

            for(int x=claimingPlayer.chunkCoordX-radius;x<=claimingPlayer.chunkCoordX+radius;x++) {
                for(int z=claimingPlayer.chunkCoordZ-radius;z<=claimingPlayer.chunkCoordZ+radius;z++) {
                    UUID chunkClan = ClaimData.getChunkClan(x, z, claimingPlayer.dimension);
                    if(chunkClan != null && !chunkClan.equals(claimingClan)) {
                        claimingPlayer.sendMessage(TranslationUtil.getTranslation(claimingPlayer.getUniqueID(), "commands.clan.claim.taken_other_r", ClanNames.get(chunkClan).getName()));
                        return false;
                    } else if(doEdgeConnectionCheck && chunkClan != null && chunkClan.equals(claimingClan))//We know the clan has claimed within the borders of the radius, so no need to check the edges for connection
                        doEdgeConnectionCheck = false;
                }
            }

            if(doEdgeConnectionCheck && createsDisconnectedClaim(claimingPlayer, claimingClan, radius))
                return false;

            if(inRangeOfAnotherClanHome(claimingPlayer, claimingClan, radius))
                return false;
        } else {
            return false;
        }
        return true;
    }

    private static boolean unableToAffordClaims(EntityPlayerMP claimingPlayer, UUID claimingClan, int chunkCount, long previousClaimCount) {
        double cost;
        long reducedCostCount = ClansModContainer.getConfig().getReducedCostClaimCount() - previousClaimCount;
        double customCost = AdminControlledClanSettings.get(claimingClan).hasCustomClaimCost() ? ClanClaimCosts.get(claimingClan).getNextClaimCost(ClanClaimCount.get(claimingClan).getClaimCount()) : -1;

        if(customCost >= 0)
            cost = customCost;
        else if (reducedCostCount > 0)
            cost = ClansModContainer.getConfig().getReducedChunkClaimCost() * reducedCostCount + FormulaParser.eval(ClansModContainer.getConfig().getClaimChunkCostFormula(), claimingClan, 0) * (chunkCount - reducedCostCount);
        else
            cost = FormulaParser.eval(ClansModContainer.getConfig().getClaimChunkCostFormula(), claimingClan, 0) * chunkCount;

        if (cost > 0 && Economy.getBalance(claimingClan) < cost) {
            claimingPlayer.sendMessage(TranslationUtil.getTranslation(claimingPlayer.getUniqueID(), "commands.clan.claim.insufficient_funds_r", ClanNames.get(claimingClan), chunkCount, Economy.getFormattedCurrency(cost)));
            return true;
        }
        return false;
    }

    private static boolean createsDisconnectedClaim(EntityPlayerMP claimingPlayer, UUID claimingClan, int radius) {
        boolean connected = false;
        for(int x = claimingPlayer.chunkCoordX- radius; x<= claimingPlayer.chunkCoordX+ radius && !connected; x++) {
            UUID chunkClan = ClaimData.getChunkClan(x, claimingPlayer.chunkCoordZ+ radius +1, claimingPlayer.dimension);
            boolean chunkIsBorderland = ClaimData.isBorderland(x, claimingPlayer.chunkCoordZ+ radius +1, claimingPlayer.dimension);
            UUID chunkClan2 = ClaimData.getChunkClan(x, claimingPlayer.chunkCoordZ- radius -1, claimingPlayer.dimension);
            boolean chunk2IsBorderland = ClaimData.isBorderland(x, claimingPlayer.chunkCoordZ- radius -1, claimingPlayer.dimension);
            if(claimingClan.equals(chunkClan) && !chunkIsBorderland || claimingClan.equals(chunkClan2) && !chunk2IsBorderland)
                connected = true;
        }
        for(int z = claimingPlayer.chunkCoordZ- radius; z<= claimingPlayer.chunkCoordZ+ radius && !connected; z++) {
            UUID chunkClan = ClaimData.getChunkClan(claimingPlayer.chunkCoordX+ radius +1, z, claimingPlayer.dimension);
            boolean chunkIsBorderland = ClaimData.isBorderland(claimingPlayer.chunkCoordX+ radius +1, z, claimingPlayer.dimension);
            UUID chunkClan2 = ClaimData.getChunkClan(claimingPlayer.chunkCoordX- radius -1, z, claimingPlayer.dimension);
            boolean chunk2IsBorderland = ClaimData.isBorderland(claimingPlayer.chunkCoordX- radius -1, z, claimingPlayer.dimension);
            if(claimingClan.equals(chunkClan) && !chunkIsBorderland || claimingClan.equals(chunkClan2) && !chunk2IsBorderland)
                connected = true;
        }
        if(!connected) {
            claimingPlayer.sendMessage(TranslationUtil.getTranslation(claimingPlayer.getUniqueID(), "commands.clan.claim.disconnected_r", ClanNames.get(claimingClan)));
            return true;
        }
        return false;
    }

    private static boolean inRangeOfAnotherClanHome(EntityPlayerMP claimingPlayer, UUID claimingClan, int radius) {
        if(!AdminControlledClanSettings.get(claimingClan).isServerOwned()) {
            boolean inClanHomeRange = false;
            for (Map.Entry<UUID, ClanHomes> pos : ClanHomes.getClanHomes().entrySet()) {
                if (!pos.getKey().equals(claimingClan)) {
                    //No need to check every single chunk, every position on the outer edge would be ideal but checking what is roughly the four corners, we get a close enough estimation with much less performance cost
                    if(pos.getValue().toBlockPos().getDistance(claimingPlayer.getPosition().getX() + radius *16, claimingPlayer.getPosition().getY(), claimingPlayer.getPosition().getZ() + radius *16) < ClansModContainer.getConfig().getMinClanHomeDist() * ClansModContainer.getConfig().getInitialClaimSeparationMultiplier()
                    || pos.getValue().toBlockPos().getDistance(claimingPlayer.getPosition().getX() + radius *16, claimingPlayer.getPosition().getY(), claimingPlayer.getPosition().getZ() - radius *16) < ClansModContainer.getConfig().getMinClanHomeDist() * ClansModContainer.getConfig().getInitialClaimSeparationMultiplier()
                    || pos.getValue().toBlockPos().getDistance(claimingPlayer.getPosition().getX() - radius *16, claimingPlayer.getPosition().getY(), claimingPlayer.getPosition().getZ() + radius *16) < ClansModContainer.getConfig().getMinClanHomeDist() * ClansModContainer.getConfig().getInitialClaimSeparationMultiplier()
                    || pos.getValue().toBlockPos().getDistance(claimingPlayer.getPosition().getX() - radius *16, claimingPlayer.getPosition().getY(), claimingPlayer.getPosition().getZ() - radius *16) < ClansModContainer.getConfig().getMinClanHomeDist() * ClansModContainer.getConfig().getInitialClaimSeparationMultiplier())
                    inClanHomeRange = true;
                }
            }
            if (inClanHomeRange) {
                if (ClansModContainer.getConfig().isEnforceInitialClaimSeparation()) {
                    claimingPlayer.sendMessage(TranslationUtil.getTranslation(claimingPlayer.getUniqueID(), "commands.clan.claim.proximity_error_r", ClansModContainer.getConfig().getMinClanHomeDist() * ClansModContainer.getConfig().getInitialClaimSeparationMultiplier()).setStyle(TextStyles.RED));
                    return true;
                } else if (!PlayerCache.getClaimWarning(claimingPlayer.getUniqueID())) {
                    claimingPlayer.sendMessage(TranslationUtil.getTranslation(claimingPlayer.getUniqueID(), "commands.clan.claim.proximity_warning_r", ClansModContainer.getConfig().getMinClanHomeDist() * ClansModContainer.getConfig().getInitialClaimSeparationMultiplier()).setStyle(TextStyles.YELLOW));
                    PlayerCache.setClaimWarning(claimingPlayer.getUniqueID(), true);
                    return true;
                }
            }
        }
        return false;
    }

    public static void claimRadius(EntityPlayerMP claimingPlayer, UUID claimingClan, int radius) {
        final int cX = claimingPlayer.chunkCoordX, cZ = claimingPlayer.chunkCoordZ;
        ConcurrentExecutionManager.runKillable(() -> {
            sendStartRadiusClaimMessage(claimingPlayer);
            ExecutorService claimExecutorService = Executors.newFixedThreadPool(128);
            for (int x = cX - radius; x <= cX + radius; x++)
                for (int z = cZ - radius; z <= cZ + radius; z++) {
                    int finalX = x;
                    int finalZ = z;
                    claimExecutorService.execute(() -> claimChunk(claimingPlayer, new ChunkPositionWithData(finalX, finalZ, claimingPlayer.dimension), claimingClan, AdminControlledClanSettings.get(claimingClan).isServerOwned(), false)
                    );
                }
            claimExecutorService.shutdown();
            try {
                claimExecutorService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            } catch (InterruptedException ignored) {}
            sendClaimSuccessMessage(claimingPlayer, claimingClan);
        });
    }

    private static void sendClaimSuccessMessage(EntityPlayerMP claimingPlayer, UUID claimingClan) {
        claimingPlayer.sendMessage(TranslationUtil.getTranslation(claimingPlayer.getUniqueID(), "commands.clan.claim.success", ClanNames.get(claimingClan).getName()).setStyle(TextStyles.GREEN));
    }

    private static void sendStartRadiusClaimMessage(EntityPlayerMP claimingPlayer) {
        claimingPlayer.sendMessage(TranslationUtil.getTranslation(claimingPlayer.getUniqueID(), "commands.clan.claim.start_r").setStyle(TextStyles.GREEN));
    }

    public static boolean checkAndAttemptClaim(EntityPlayerMP claimingPlayer, UUID claimingClan, boolean force) {
        return checkAndAttemptClaim(claimingPlayer, claimingClan, new ChunkPositionWithData(claimingPlayer.chunkCoordX, claimingPlayer.chunkCoordZ, claimingPlayer.getEntityWorld().provider.getDimension()).retrieveCentralData(), force);
    }

    public static boolean checkAndAttemptClaim(EntityPlayerMP claimingPlayer, UUID claimingClan, ChunkPositionWithData claimChunk, boolean force) {
        if (!force && shouldCancelClaimingBecauseOfDimension(claimingPlayer)) {
            claimingPlayer.sendMessage(TranslationUtil.getTranslation(claimingPlayer.getUniqueID(), "commands.clan.claim.dimension").setStyle(TextStyles.RED));
            return false;
        }

        UUID claimOwner = ClaimData.getChunkClan(claimChunk);
        if(claimOwner != null && ClanIdRegistry.isValidClan(claimOwner) && (!force || claimOwner.equals(claimingClan))) {
            if(!claimOwner.equals(claimingClan)) {
                claimingPlayer.sendMessage(TranslationUtil.getTranslation(claimingPlayer.getUniqueID(), "commands.clan.claim.taken_other", ClanNames.get(claimOwner).getName()).setStyle(TextStyles.RED));
                return false;
            } else if(!claimChunk.isBorderland()) {
                claimingPlayer.sendMessage(TranslationUtil.getTranslation(claimingPlayer.getUniqueID(), "commands.clan.claim.taken", ClanNames.get(claimingClan).getName()).setStyle(TextStyles.YELLOW));
                return false;
            }
        }
        if(claimOwner != null && !claimChunk.isBorderland() && !AdminControlledClanSettings.get(claimOwner).isServerOwned()) {//In this scenario, we are always forcing the claim and it is over someone else's claim, so we should refund the clan the land is being taken from
            ClanClaimCosts.get(claimOwner).refundClaim();
        }
        if(AdminControlledClanSettings.get(claimingClan).isServerOwned()) {
            return claimChunk(claimingPlayer, claimChunk, claimingClan, true, true);
        } else {
            ClanClaimCount clanClaimCount = ClanClaimCount.get(claimingClan);
            long claimCount = clanClaimCount.getClaimCount();
            long maxClaimCount = clanClaimCount.getMaxClaimCount();
            if (force || !ClansModContainer.getConfig().isForceConnectedClaims() || ChunkUtils.hasConnectedClaim(claimChunk, claimingClan) || claimCount == 0) {
                if (force || claimCount < maxClaimCount) {
                    if (claimCount > 0)
                        claimChunk(claimingPlayer, claimChunk, claimingClan, force, true);
                    else if (ClansModContainer.getConfig().getMinClanHomeDist() > 0 && ClansModContainer.getConfig().getInitialClaimSeparationMultiplier() > 0) {
                        boolean inClanHomeRange = false;
                        for (Map.Entry<UUID, ClanHomes> pos : ClanHomes.getClanHomes().entrySet())
                            if (!pos.getKey().equals(claimingClan) && pos.getValue() != null && pos.getValue().toBlockPos().getDistance(claimingPlayer.getPosition().getX(), claimingPlayer.getPosition().getY(), claimingPlayer.getPosition().getZ()) < ClansModContainer.getConfig().getMinClanHomeDist() * ClansModContainer.getConfig().getInitialClaimSeparationMultiplier())
                                inClanHomeRange = true;
                        if (inClanHomeRange) {
                            if (ClansModContainer.getConfig().isEnforceInitialClaimSeparation())
                                claimingPlayer.sendMessage(TranslationUtil.getTranslation(claimingPlayer.getUniqueID(), "commands.clan.claim.proximity_error", ClansModContainer.getConfig().getMinClanHomeDist() * ClansModContainer.getConfig().getInitialClaimSeparationMultiplier()).setStyle(TextStyles.RED));
                            else if (PlayerCache.getClaimWarning(claimingPlayer.getUniqueID()))
                                return claimChunk(claimingPlayer, claimChunk, claimingClan, force, true);
                            else {
                                claimingPlayer.sendMessage(TranslationUtil.getTranslation(claimingPlayer.getUniqueID(), "commands.clan.claim.proximity_warning", ClansModContainer.getConfig().getMinClanHomeDist() * ClansModContainer.getConfig().getInitialClaimSeparationMultiplier()).setStyle(TextStyles.YELLOW));
                                PlayerCache.setClaimWarning(claimingPlayer.getUniqueID(), true);
                            }
                        } else
                            return claimChunk(claimingPlayer, claimChunk, claimingClan, force, true);
                    } else
                        return claimChunk(claimingPlayer, claimChunk, claimingClan, force, true);
                } else
                    claimingPlayer.sendMessage(TranslationUtil.getTranslation(claimingPlayer.getUniqueID(), "commands.clan.claim.maxed", ClanNames.get(claimingClan).getName(), maxClaimCount).setStyle(TextStyles.RED));
            } else
                claimingPlayer.sendMessage(TranslationUtil.getTranslation(claimingPlayer.getUniqueID(), "commands.clan.claim.disconnected", ClanNames.get(claimingClan).getName()).setStyle(TextStyles.RED));
        }
        return false;
    }

    private static boolean shouldCancelClaimingBecauseOfDimension(EntityPlayerMP claimingPlayer) {
        if(ClansModContainer.getConfig().getClaimableDimensions().contains("*")) {
            for(String s: ClansModContainer.getConfig().getClaimableDimensions())
                if(s.toLowerCase().equals(claimingPlayer.getServerWorld().provider.getDimensionType().getName())) {
                    return true;
                } else {
                    try {
                        int dimId = Integer.parseInt(s);
                        if(dimId == claimingPlayer.getServerWorld().provider.getDimensionType().getId()) {
                            return true;
                        }
                    } catch(NumberFormatException ignored) {}
                }
        } else {
            boolean found = false;
            for(String s: ClansModContainer.getConfig().getClaimableDimensions())
                if(s.toLowerCase().equals(claimingPlayer.getServerWorld().provider.getDimensionType().getName())) {
                    found = true;
                    break;
                } else {
                    try {
                        int dimId = Integer.parseInt(s);
                        if(dimId == claimingPlayer.getServerWorld().provider.getDimensionType().getId()) {
                            found = true;
                            break;
                        }
                    } catch(NumberFormatException ignored) {}
                }
            return !found;
        }
        return false;
    }

    public static boolean claimChunk(EntityPlayerMP claimingPlayer, ChunkPositionWithData claimChunk, UUID claimingClan, boolean noClaimCost, boolean showMessage) {
        if (noClaimCost || ClanClaimCosts.get(claimingClan).payForClaim()) {
            PreLandClaimEvent event = ClansEventManager.fireEvent(new PreLandClaimEvent(claimingPlayer.world, claimChunk, claimingPlayer.getUniqueID(), claimingClan));
            if(!event.isCancelled) {
                ClaimData.updateChunkOwner(claimChunk, null, claimingClan);
                setClanHomeIfNeeded(claimingPlayer, claimChunk, claimingClan);
                if(showMessage)
                    sendClaimSuccessMessage(claimingPlayer, claimingClan);
                return true;
            } else {
                claimingPlayer.sendMessage(event.cancelledMessage);
                return false;
            }
        } else
            claimingPlayer.sendMessage(TranslationUtil.getTranslation(claimingPlayer.getUniqueID(), "commands.clan.claim.insufficient_funds", ClanNames.get(claimingClan).getName(), Economy.getFormattedCurrency(FormulaParser.eval(ClansModContainer.getConfig().getClaimChunkCostFormula(), claimingClan, 0))).setStyle(TextStyles.RED));
        return false;
    }

    private static void setClanHomeIfNeeded(EntityPlayerMP claimingPlayer, ChunkPositionWithData claimChunk, UUID claimingClan) {
        if(!ClanHomes.hasHome(claimingClan) && entityIsInChunk(claimingPlayer, claimChunk) && hasPermissionToSetHome(claimingPlayer, claimingClan))
            ClanHomes.set(claimingClan, claimingPlayer.getPosition(), claimingPlayer.dimension);
    }

    private static boolean entityIsInChunk(Entity entity, ChunkPosition chunkPosition) {
        return chunkPosition.getPosX() == entity.chunkCoordX
            && chunkPosition.getPosZ() == entity.chunkCoordZ
            && chunkPosition.getDim() == entity.dimension;
    }

    private static boolean hasPermissionToSetHome(EntityPlayerMP claimingPlayer, UUID claimingClan) {
        return ClanPermissions.get(claimingClan).hasPerm("sethome", claimingPlayer.getUniqueID()) && PermissionManager.hasPermission(claimingPlayer, PermissionManager.CLAN_COMMAND_PREFIX + "sethome", true);
    }

    public static boolean checkAndAttemptAbandon(EntityPlayerMP abandoningPlayer, @Nullable UUID chunkOwner) {
        return checkAndAttemptAbandon(abandoningPlayer, chunkOwner, new ChunkPositionWithData(abandoningPlayer.chunkCoordX, abandoningPlayer.chunkCoordZ, abandoningPlayer.getEntityWorld().provider.getDimension()).retrieveCentralData());
    }

    public static boolean checkAndAttemptAbandon(EntityPlayerMP abandoningPlayer, @Nullable UUID chunkOwner, ChunkPositionWithData claimChunk) {
        Chunk c = abandoningPlayer.getEntityWorld().getChunk(abandoningPlayer.getPosition());
        UUID claimOwner = ChunkUtils.getChunkOwner(c);
        if(claimOwner != null && !claimChunk.isBorderland()) {
            if(!ClanIdRegistry.isValidClan(claimOwner)) {
                ChunkUtils.clearChunkOwner(c);
                abandoningPlayer.sendMessage(TranslationUtil.getTranslation(abandoningPlayer.getUniqueID(), "commands.clan.abandonclaim.success", ClanNames.get(claimOwner).getName()).setStyle(TextStyles.GREEN));
                return true;
            }
            if(chunkOwner == null || claimOwner.equals(chunkOwner)) {
                if(chunkOwner == null || AdminControlledClanSettings.get(claimOwner).isServerOwned() || !ClansModContainer.getConfig().isForceConnectedClaims() || ChunkUtils.canBeAbandoned(c, claimOwner)) {
                    return finishClaimAbandonment(abandoningPlayer, c, claimOwner);
                } else {//We are forcing connected claims and there is a claim connected
                    //Prevent creation of disconnected claims
                    return abandonClaimWithAdjacencyCheck(abandoningPlayer, c, claimOwner);
                }
            } else
                abandoningPlayer.sendMessage(TranslationUtil.getTranslation(abandoningPlayer.getUniqueID(), "commands.clan.abandonclaim.wrongclan", ClanNames.get(claimOwner).getName()).setStyle(TextStyles.RED));
        } else
            abandoningPlayer.sendMessage(TranslationUtil.getTranslation(abandoningPlayer.getUniqueID(), "commands.clan.abandonclaim.notclaimed").setStyle(TextStyles.RED));
        return false;
    }

    public static void abandonClaim(EntityPlayerMP abandoningPlayer, Chunk c, UUID chunkOwner) {
        abandonClaim(c.x, c.z, abandoningPlayer.dimension, chunkOwner);
    }

    public static void abandonClaim(int chunkX, int chunkZ, int dim, UUID chunkOwner) {
        ChunkPos pos = new ChunkPos(chunkX, chunkZ);
        //Unset clan home if it is in the chunk
        if (ClanHomes.hasHome(chunkOwner)
                && dim == ClanHomes.get(chunkOwner).getHomeDim()
                && ClanHomes.get(chunkOwner).toBlockPos().getX() >= pos.getXStart()
                && ClanHomes.get(chunkOwner).toBlockPos().getX() <= pos.getXEnd()
                && ClanHomes.get(chunkOwner).toBlockPos().getZ() >= pos.getZStart()
                && ClanHomes.get(chunkOwner).toBlockPos().getZ() <= pos.getZEnd()) {
            ClanHomes.delete(chunkOwner);
        }

        ClaimData.delChunk(chunkOwner, new ChunkPositionWithData(chunkX, chunkZ, dim));
        if(!AdminControlledClanSettings.get(chunkOwner).isServerOwned())
            ClanClaimCosts.get(chunkOwner).refundClaim();
    }

    public static boolean abandonClaimWithAdjacencyCheck(EntityPlayerMP abandoningPlayer, Chunk c, UUID chunkOwner) {
        boolean allowed = true;
        for (Chunk checkChunk : ChunkUtils.getConnectedClaimChunks(c, chunkOwner)) {
            if (ChunkUtils.getConnectedClaimChunks(checkChunk, chunkOwner).equals(Lists.newArrayList(c))) {
                allowed = false;
                break;
            }
        }
        if (allowed) {
            return finishClaimAbandonment(abandoningPlayer, c, chunkOwner);
        } else
            abandoningPlayer.sendMessage(TranslationUtil.getTranslation(abandoningPlayer.getUniqueID(), "commands.clan.abandonclaim.disconnected").setStyle(TextStyles.RED));
        return false;
    }

    public static boolean finishClaimAbandonment(EntityPlayerMP abandoningPlayer, Chunk c, UUID chunkOwner) {
        PreLandAbandonEvent event = ClansEventManager.fireEvent(new PreLandAbandonEvent(abandoningPlayer.world, c, new ChunkPosition(c), abandoningPlayer.getUniqueID(), chunkOwner));
        if(!event.isCancelled) {
            abandonClaim(abandoningPlayer, c, chunkOwner);
            abandoningPlayer.sendMessage(TranslationUtil.getTranslation(abandoningPlayer.getUniqueID(), "commands.clan.abandonclaim.success", ClanNames.get(chunkOwner).getName()).setStyle(TextStyles.GREEN));
            return true;
        } else
            abandoningPlayer.sendMessage(event.cancelledMessage);
        return false;
    }
}
