package the_fireplace.clans.logic;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import org.apache.commons.lang3.ArrayUtils;
import the_fireplace.clans.Clans;
import the_fireplace.clans.api.event.PreLandAbandonEvent;
import the_fireplace.clans.api.event.PreLandClaimEvent;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.cache.PlayerCache;
import the_fireplace.clans.data.ClaimData;
import the_fireplace.clans.data.PlayerData;
import the_fireplace.clans.model.ChunkPosition;
import the_fireplace.clans.model.ChunkPositionWithData;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.util.ChunkUtils;
import the_fireplace.clans.util.ClansEventManager;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

public class ClanManagementLogic {
    public static boolean checkCanClaimRadius(EntityPlayerMP claimingPlayer, Clan claimingClan, int radius, String radiusMode) {
        if(radiusMode.equalsIgnoreCase("square")) {
            int chunkCount = radius * radius;
            int initClaimCount = claimingClan.getClaimCount();
            if(!claimingClan.isServer()) {
                if(claimingClan.getMaxClaimCount() > 0 && chunkCount + initClaimCount > claimingClan.getMaxClaimCount()) {
                    claimingPlayer.sendMessage(TranslationUtil.getTranslation(claimingPlayer.getUniqueID(), "commands.clan.claim.maxed_r", chunkCount, claimingClan.getName(), claimingClan.getMaxClaimCount(), initClaimCount));
                    return false;
                }
                int cost;
                int reducedCostCount = Clans.getConfig().getReducedCostClaimCount() - initClaimCount;
                int customCost = claimingClan.hasCustomClaimCost() ? claimingClan.getClaimCost() : -1;

                if(customCost >= 0)
                    cost = customCost;
                else if (reducedCostCount > 0)
                    cost = Clans.getConfig().getReducedChunkClaimCost() * reducedCostCount + Clans.getConfig().getClaimChunkCost() * (chunkCount - reducedCostCount);
                else
                    cost = Clans.getConfig().getClaimChunkCost() * chunkCount;

                if (cost > 0 && Clans.getPaymentHandler().getBalance(claimingClan.getId()) < cost) {
                    claimingPlayer.sendMessage(TranslationUtil.getTranslation(claimingPlayer.getUniqueID(), "commands.clan.claim.insufficient_funds_r", claimingClan.getName(), chunkCount, cost, Clans.getPaymentHandler().getCurrencyName(cost)));
                    return false;
                }
            }
            //Do a connection check if connected claims are enforced, this is not the first claim, and the clan is not a server clan
            boolean doEdgeConnectionCheck = Clans.getConfig().isForceConnectedClaims() && initClaimCount != 0 && !claimingClan.isServer();

            for(int x=claimingPlayer.chunkCoordX-radius;x<=claimingPlayer.chunkCoordX+radius;x++) {
                for(int z=claimingPlayer.chunkCoordZ-radius;z<=claimingPlayer.chunkCoordZ+radius;z++) {
                    Clan chunkClan = ClaimData.getChunkClan(x, z, claimingPlayer.dimension);
                    if(chunkClan != null && !chunkClan.equals(claimingClan)) {
                        claimingPlayer.sendMessage(TranslationUtil.getTranslation(claimingPlayer.getUniqueID(), "commands.clan.claim.taken_other_r", chunkClan.getName()));
                        return false;
                    } else if(doEdgeConnectionCheck && chunkClan != null && chunkClan.equals(claimingClan))//We know the clan has claimed within the borders of the radius, so no need to check the edges for connection
                        doEdgeConnectionCheck = false;
                }
            }

            if(doEdgeConnectionCheck) {
                boolean connected = false;
                for(int x=claimingPlayer.chunkCoordX-radius;x<=claimingPlayer.chunkCoordX+radius && !connected;x++) {
                    Clan chunkClan = ClaimData.getChunkClan(x, claimingPlayer.chunkCoordZ+radius+1, claimingPlayer.dimension);
                    boolean chunkIsBorderland = ClaimData.isBorderland(x, claimingPlayer.chunkCoordZ+radius+1, claimingPlayer.dimension);
                    Clan chunkClan2 = ClaimData.getChunkClan(x, claimingPlayer.chunkCoordZ-radius-1, claimingPlayer.dimension);
                    boolean chunk2IsBorderland = ClaimData.isBorderland(x, claimingPlayer.chunkCoordZ-radius-1, claimingPlayer.dimension);
                    if(claimingClan.equals(chunkClan) && !chunkIsBorderland || claimingClan.equals(chunkClan2) && !chunk2IsBorderland)
                        connected = true;
                }
                for(int z=claimingPlayer.chunkCoordZ-radius;z<=claimingPlayer.chunkCoordZ+radius && !connected;z++) {
                    Clan chunkClan = ClaimData.getChunkClan(claimingPlayer.chunkCoordX+radius+1, z, claimingPlayer.dimension);
                    boolean chunkIsBorderland = ClaimData.isBorderland(claimingPlayer.chunkCoordX+radius+1, z, claimingPlayer.dimension);
                    Clan chunkClan2 = ClaimData.getChunkClan(claimingPlayer.chunkCoordX-radius-1, z, claimingPlayer.dimension);
                    boolean chunk2IsBorderland = ClaimData.isBorderland(claimingPlayer.chunkCoordX-radius-1, z, claimingPlayer.dimension);
                    if(claimingClan.equals(chunkClan) && !chunkIsBorderland || claimingClan.equals(chunkClan2) && !chunk2IsBorderland)
                        connected = true;
                }
                if(!connected) {
                    claimingPlayer.sendMessage(TranslationUtil.getTranslation(claimingPlayer.getUniqueID(), "commands.clan.claim.disconnected_r", claimingClan.getName()));
                    return false;
                }
            }

            if(!claimingClan.isServer()) {
                boolean inClanHomeRange = false;
                for (Map.Entry<Clan, BlockPos> pos : ClanCache.getClanHomes().entrySet()) {
                    if (!pos.getKey().getId().equals(claimingClan.getId()) && pos.getKey().hasHome() && pos.getValue() != null) {
                        //No need to check every single chunk, every position on the outer edge would be ideal but checking what is roughly the four corners, we get a close enough estimation with much less performance cost
                        if(pos.getValue().getDistance(claimingPlayer.getPosition().getX() + radius*16, claimingPlayer.getPosition().getY(), claimingPlayer.getPosition().getZ() + radius*16) < Clans.getConfig().getMinClanHomeDist() * Clans.getConfig().getInitialClaimSeparationMultiplier()
                        || pos.getValue().getDistance(claimingPlayer.getPosition().getX() + radius*16, claimingPlayer.getPosition().getY(), claimingPlayer.getPosition().getZ() - radius*16) < Clans.getConfig().getMinClanHomeDist() * Clans.getConfig().getInitialClaimSeparationMultiplier()
                        || pos.getValue().getDistance(claimingPlayer.getPosition().getX() - radius*16, claimingPlayer.getPosition().getY(), claimingPlayer.getPosition().getZ() + radius*16) < Clans.getConfig().getMinClanHomeDist() * Clans.getConfig().getInitialClaimSeparationMultiplier()
                        || pos.getValue().getDistance(claimingPlayer.getPosition().getX() - radius*16, claimingPlayer.getPosition().getY(), claimingPlayer.getPosition().getZ() - radius*16) < Clans.getConfig().getMinClanHomeDist() * Clans.getConfig().getInitialClaimSeparationMultiplier())
                        inClanHomeRange = true;
                    }
                }
                if (inClanHomeRange) {
                    if (Clans.getConfig().isEnforceInitialClaimSeparation()) {
                        claimingPlayer.sendMessage(TranslationUtil.getTranslation(claimingPlayer.getUniqueID(), "commands.clan.claim.proximity_error_r", Clans.getConfig().getMinClanHomeDist() * Clans.getConfig().getInitialClaimSeparationMultiplier()).setStyle(TextStyles.RED));
                        return false;
                    } else if (!PlayerCache.getClaimWarning(claimingPlayer.getUniqueID())) {
                        claimingPlayer.sendMessage(TranslationUtil.getTranslation(claimingPlayer.getUniqueID(), "commands.clan.claim.proximity_warning_r", Clans.getConfig().getMinClanHomeDist() * Clans.getConfig().getInitialClaimSeparationMultiplier()).setStyle(TextStyles.YELLOW));
                        PlayerCache.setClaimWarning(claimingPlayer.getUniqueID(), true);
                        return false;
                    }
                }
            }
        } else {
            return false;
        }
        return true;
    }

    public static void claimRadius(EntityPlayerMP claimingPlayer, Clan claimingClan, int radius, String radiusMode) {
        final int cX = claimingPlayer.chunkCoordX, cZ = claimingPlayer.chunkCoordZ;
        //This could take a long time with a large radius, do it on a different thread to prevent lag spike and server timeout
        new Thread(() ->{
            claimingPlayer.sendMessage(TranslationUtil.getTranslation(claimingPlayer.getUniqueID(), "commands.clan.claim.start_r").setStyle(TextStyles.GREEN));
            if (radiusMode.equalsIgnoreCase("square"))
                for (int x = cX - radius; x <= cX + radius; x++)
                    for (int z = cZ - radius; z <= cZ + radius; z++)
                        claimChunk(claimingPlayer, new ChunkPositionWithData(x, z, claimingPlayer.dimension), claimingClan, claimingClan.isServer(), false);
            claimingPlayer.sendMessage(TranslationUtil.getTranslation(claimingPlayer.getUniqueID(), "commands.clan.claim.success", claimingClan.getName()).setStyle(TextStyles.GREEN));
        }).start();
    }

    public static boolean checkAndAttemptClaim(EntityPlayerMP claimingPlayer, Clan claimingClan, boolean force) {
        return checkAndAttemptClaim(claimingPlayer, claimingClan, new ChunkPositionWithData(claimingPlayer.chunkCoordX, claimingPlayer.chunkCoordZ, claimingPlayer.getEntityWorld().provider.getDimension()).retrieveCentralData(), force);
    }

    public static boolean checkAndAttemptClaim(EntityPlayerMP claimingPlayer, Clan claimingClan, ChunkPositionWithData claimChunk, boolean force) {
        UUID claimOwner = ClaimData.getChunkClanId(claimChunk);
        Clan claimClan = ClanCache.getClanById(claimOwner);
        if(claimOwner != null && claimClan != null && (!force || claimOwner.equals(claimingClan.getId()))) {
            if(!claimOwner.equals(claimingClan.getId())) {
                claimingPlayer.sendMessage(TranslationUtil.getTranslation(claimingPlayer.getUniqueID(), "commands.clan.claim.taken_other", claimClan.getName()).setStyle(TextStyles.RED));
                return false;
            } else if(!claimChunk.isBorderland()) {
                claimingPlayer.sendMessage(TranslationUtil.getTranslation(claimingPlayer.getUniqueID(), "commands.clan.claim.taken", claimingClan.getName()).setStyle(TextStyles.YELLOW));
                return false;
            }
        }
        if(claimClan != null && !claimChunk.isBorderland() && !claimClan.isServer()) {//In this scenario, we are always forcing the claim and it is over someone else's claim, so we should refund the clan the land is being taken from
            claimClan.refundClaim();
        }
        if(claimingClan.isServer()) {
            return claimChunk(claimingPlayer, claimChunk, claimingClan, true, true);
        } else {
            if (force || !Clans.getConfig().isForceConnectedClaims() || ChunkUtils.hasConnectedClaim(claimChunk, claimingClan.getId()) || claimingClan.getClaimCount() == 0) {
                if (force || claimingClan.getMaxClaimCount() <= 0 || claimingClan.getClaimCount() < claimingClan.getMaxClaimCount()) {
                    if (claimingClan.getClaimCount() > 0)
                        claimChunk(claimingPlayer, claimChunk, claimingClan, force, true);
                    else if (Clans.getConfig().getMinClanHomeDist() > 0 && Clans.getConfig().getInitialClaimSeparationMultiplier() > 0) {
                        boolean inClanHomeRange = false;
                        for (Map.Entry<Clan, BlockPos> pos : ClanCache.getClanHomes().entrySet())
                            if (!pos.getKey().getId().equals(claimingClan.getId()) && pos.getKey().hasHome() && pos.getValue() != null && pos.getValue().getDistance(claimingPlayer.getPosition().getX(), claimingPlayer.getPosition().getY(), claimingPlayer.getPosition().getZ()) < Clans.getConfig().getMinClanHomeDist() * Clans.getConfig().getInitialClaimSeparationMultiplier())
                                inClanHomeRange = true;
                        if (inClanHomeRange) {
                            if (Clans.getConfig().isEnforceInitialClaimSeparation())
                                claimingPlayer.sendMessage(TranslationUtil.getTranslation(claimingPlayer.getUniqueID(), "commands.clan.claim.proximity_error", Clans.getConfig().getMinClanHomeDist() * Clans.getConfig().getInitialClaimSeparationMultiplier()).setStyle(TextStyles.RED));
                            else if (PlayerCache.getClaimWarning(claimingPlayer.getUniqueID()))
                                return claimChunk(claimingPlayer, claimChunk, claimingClan, force, true);
                            else {
                                claimingPlayer.sendMessage(TranslationUtil.getTranslation(claimingPlayer.getUniqueID(), "commands.clan.claim.proximity_warning", Clans.getConfig().getMinClanHomeDist() * Clans.getConfig().getInitialClaimSeparationMultiplier()).setStyle(TextStyles.YELLOW));
                                PlayerCache.setClaimWarning(claimingPlayer.getUniqueID(), true);
                            }
                        } else
                            return claimChunk(claimingPlayer, claimChunk, claimingClan, force, true);
                    } else
                        return claimChunk(claimingPlayer, claimChunk, claimingClan, force, true);
                } else
                    claimingPlayer.sendMessage(TranslationUtil.getTranslation(claimingPlayer.getUniqueID(), "commands.clan.claim.maxed", claimingClan.getName(), claimingClan.getMaxClaimCount()).setStyle(TextStyles.RED));
            } else
                claimingPlayer.sendMessage(TranslationUtil.getTranslation(claimingPlayer.getUniqueID(), "commands.clan.claim.disconnected", claimingClan.getName()).setStyle(TextStyles.RED));
        }
        return false;
    }

    public static boolean claimChunk(EntityPlayerMP claimingPlayer, ChunkPositionWithData claimChunk, Clan claimingClan, boolean noClaimCost, boolean showMessage) {
        if (noClaimCost || claimingClan.payForClaim()) {
            PreLandClaimEvent event = ClansEventManager.fireEvent(new PreLandClaimEvent(claimingPlayer.world, claimChunk, claimingPlayer.getUniqueID(), claimingClan));
            if(!event.isCancelled) {
                ClaimData.swapChunk(claimChunk, null, claimingClan.getId());
                if(showMessage)
                    claimingPlayer.sendMessage(TranslationUtil.getTranslation(claimingPlayer.getUniqueID(), "commands.clan.claim.success", claimingClan.getName()).setStyle(TextStyles.GREEN));
                return true;
            } else {
                claimingPlayer.sendMessage(event.cancelledMessage);
                return false;
            }
        } else
            claimingPlayer.sendMessage(TranslationUtil.getTranslation(claimingPlayer.getUniqueID(), "commands.clan.claim.insufficient_funds", claimingClan.getName(), Clans.getConfig().getClaimChunkCost(), Clans.getPaymentHandler().getCurrencyName(Clans.getConfig().getClaimChunkCost())).setStyle(TextStyles.RED));
        return false;
    }

    public static boolean checkAndAttemptAbandon(EntityPlayerMP abandoningPlayer, @Nullable Clan chunkOwner) {
        return checkAndAttemptAbandon(abandoningPlayer, chunkOwner, new ChunkPositionWithData(abandoningPlayer.chunkCoordX, abandoningPlayer.chunkCoordZ, abandoningPlayer.getEntityWorld().provider.getDimension()).retrieveCentralData());
    }

    public static boolean checkAndAttemptAbandon(EntityPlayerMP abandoningPlayer, @Nullable Clan chunkOwner, ChunkPositionWithData claimChunk) {
        Chunk c = abandoningPlayer.getEntityWorld().getChunk(abandoningPlayer.getPosition());
        UUID claimOwnerClanId = ChunkUtils.getChunkOwner(c);
        if(claimOwnerClanId != null && !claimChunk.isBorderland()) {
            Clan claimOwnerClan = ClanCache.getClanById(claimOwnerClanId);
            if(claimOwnerClan == null) {
                ChunkUtils.clearChunkOwner(c);
                abandoningPlayer.sendMessage(TranslationUtil.getTranslation(abandoningPlayer.getUniqueID(), "commands.clan.abandonclaim.success", "null").setStyle(TextStyles.GREEN));
                return true;
            }
            if(chunkOwner == null || claimOwnerClanId.equals(chunkOwner.getId())) {
                if(chunkOwner == null || claimOwnerClan.isServer() || !Clans.getConfig().isForceConnectedClaims() || ChunkUtils.canBeAbandoned(c, claimOwnerClanId)) {
                    return finishClaimAbandonment(abandoningPlayer, c, claimOwnerClan);
                } else {//We are forcing connected claims and there is a claim connected
                    //Prevent creation of disconnected claims
                    return abandonClaimWithAdjacencyCheck(abandoningPlayer, c, claimOwnerClan);
                }
            } else
                abandoningPlayer.sendMessage(TranslationUtil.getTranslation(abandoningPlayer.getUniqueID(), "commands.clan.abandonclaim.wrongclan", chunkOwner.getName()).setStyle(TextStyles.RED));
        } else
            abandoningPlayer.sendMessage(TranslationUtil.getTranslation(abandoningPlayer.getUniqueID(), "commands.clan.abandonclaim.notclaimed").setStyle(TextStyles.RED));
        return false;
    }

    public static void abandonClaim(EntityPlayerMP abandoningPlayer, Chunk c, Clan chunkOwner) {
        abandonClaim(c.x, c.z, abandoningPlayer.dimension, chunkOwner);
    }

    public static void abandonClaim(int chunkX, int chunkZ, int dim, Clan chunkOwner) {
        ChunkPos pos = new ChunkPos(chunkX, chunkZ);
        //Unset clan home if it is in the chunk
        if (chunkOwner.getHome() != null
                && chunkOwner.hasHome()
                && dim == chunkOwner.getHomeDim()
                && chunkOwner.getHome().getX() >= pos.getXStart()
                && chunkOwner.getHome().getX() <= pos.getXEnd()
                && chunkOwner.getHome().getZ() >= pos.getZStart()
                && chunkOwner.getHome().getZ() <= pos.getZEnd()) {
            chunkOwner.unsetHome();
        }

        ClaimData.delChunk(chunkOwner, new ChunkPositionWithData(chunkX, chunkZ, dim));
        if(!chunkOwner.isServer())
            chunkOwner.refundClaim();
    }

    public static boolean abandonClaimWithAdjacencyCheck(EntityPlayerMP abandoningPlayer, Chunk c, Clan chunkOwner) {
        boolean allowed = true;
        for (Chunk checkChunk : ChunkUtils.getConnectedClaimChunks(c, chunkOwner.getId())) {
            if (ChunkUtils.getConnectedClaimChunks(checkChunk, chunkOwner.getId()).equals(Lists.newArrayList(c))) {
                allowed = false;
                break;
            }
        }
        if (allowed) {
            return finishClaimAbandonment(abandoningPlayer, c, chunkOwner);
        } else
            abandoningPlayer.sendMessage(TranslationUtil.getTranslation(abandoningPlayer.getUniqueID(), "commands.opclan.abandonclaim.disconnected").setStyle(TextStyles.RED));
        return false;
    }

    public static boolean finishClaimAbandonment(EntityPlayerMP abandoningPlayer, Chunk c, Clan chunkOwner) {
        PreLandAbandonEvent event = ClansEventManager.fireEvent(new PreLandAbandonEvent(abandoningPlayer.world, c, new ChunkPosition(c), abandoningPlayer.getUniqueID(), chunkOwner));
        if(!event.isCancelled) {
            abandonClaim(abandoningPlayer, c, chunkOwner);
            abandoningPlayer.sendMessage(TranslationUtil.getTranslation(abandoningPlayer.getUniqueID(), "commands.clan.abandonclaim.success", chunkOwner.getName()).setStyle(TextStyles.GREEN));
            return true;
        } else
            abandoningPlayer.sendMessage(event.cancelledMessage);
        return false;
    }

    public static void promoteClanMember(MinecraftServer server, ICommandSender sender, String playerName, Clan clan) throws CommandException {
        GameProfile target = server.getPlayerProfileCache().getGameProfileForUsername(playerName);

        if(target != null) {
            if (!ClanCache.getPlayerClans(target.getId()).isEmpty()) {
                if (ClanCache.getPlayerClans(target.getId()).contains(clan)) {
                    if (clan.promoteMember(target.getId())) {
                        sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.promote.success", target.getName(), clan.getMembers().get(target.getId()).toString().toLowerCase(), clan.getName()).setStyle(TextStyles.GREEN));
                        for(Map.Entry<EntityPlayerMP, EnumRank> m : clan.getOnlineMembers().entrySet())
                            if(m.getValue().greaterOrEquals(clan.getMembers().get(target.getId())))
                                if(!m.getKey().getUniqueID().equals(target.getId()))
                                    m.getKey().sendMessage(TranslationUtil.getTranslation(m.getKey().getUniqueID(), "commands.clan.promote.notify", target.getName(), clan.getMembers().get(target.getId()).toString().toLowerCase(), clan.getName(), sender.getDisplayName().getFormattedText()).setStyle(TextStyles.GREEN));
                        if(ArrayUtils.contains(server.getPlayerList().getOnlinePlayerProfiles(), target)) {
                            EntityPlayerMP targetPlayer = CommandBase.getPlayer(server, sender, target.getName());
                            targetPlayer.sendMessage(TranslationUtil.getTranslation(targetPlayer.getUniqueID(), "commands.clan.promote.promoted", clan.getName(), clan.getMembers().get(target.getId()).toString().toLowerCase(), sender.getName()).setStyle(TextStyles.GREEN));
                        }
                    } else
                        sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.promote.error", target.getName()).setStyle(TextStyles.RED));
                } else
                    sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.player_not_in_clan", target.getName(), clan.getName()).setStyle(TextStyles.RED));
            } else
                sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.player_not_in_clan", target.getName(), clan.getName()).setStyle(TextStyles.RED));
        } else
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.playernotfound", playerName).setStyle(TextStyles.RED));
    }

    public static void demoteClanMember(MinecraftServer server, ICommandSender sender, String playerName, Clan clan) throws CommandException {
        GameProfile target = server.getPlayerProfileCache().getGameProfileForUsername(playerName);

        if(target != null) {
            if (!ClanCache.getPlayerClans(target.getId()).isEmpty()) {
                if (ClanCache.getPlayerClans(target.getId()).contains(clan)) {
                    if (clan.demoteMember(target.getId())) {
                        sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.demote.success", target.getName(), clan.getMembers().get(target.getId()).toString().toLowerCase(), clan.getName()).setStyle(TextStyles.GREEN));
                        if(ArrayUtils.contains(server.getPlayerList().getOnlinePlayerProfiles(), target)) {
                            EntityPlayerMP targetPlayer = CommandBase.getPlayer(server, sender, target.getName());
                            targetPlayer.sendMessage(TranslationUtil.getTranslation(targetPlayer.getUniqueID(), "commands.clan.demote.demoted", clan.getName(), clan.getMembers().get(target.getId()).toString().toLowerCase(), sender.getName()).setStyle(TextStyles.YELLOW));
                        }
                    } else
                        sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.demote.error", target.getName()).setStyle(TextStyles.RED));
                } else
                    sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.player_not_in_clan", target.getName(), clan.getName()).setStyle(TextStyles.RED));
            } else
                sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.player_not_in_clan", target.getName(), clan.getName()).setStyle(TextStyles.RED));
        } else
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.playernotfound", playerName).setStyle(TextStyles.RED));
    }

    public static void setRank(MinecraftServer server, ICommandSender sender, String playerName, Clan clan, EnumRank rank) throws CommandException {
        GameProfile target = server.getPlayerProfileCache().getGameProfileForUsername(playerName);

        if(target != null) {
            clan.addMember(target.getId(), rank);
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.opclan.setrank.success", target.getName(), rank.toString().toLowerCase(), clan.getName()).setStyle(TextStyles.GREEN));
            if(ArrayUtils.contains(server.getPlayerList().getOnlinePlayerProfiles(), target)) {
                EntityPlayerMP targetPlayer = CommandBase.getPlayer(server, sender, target.getName());
                if(targetPlayer != sender)
                    targetPlayer.sendMessage(TranslationUtil.getTranslation(targetPlayer.getUniqueID(), "commands.opclan.setrank.set", rank.toString().toLowerCase(), clan.getName(), sender.getName()).setStyle(TextStyles.YELLOW));
            }
        } else
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.playernotfound", playerName).setStyle(TextStyles.RED));
    }

    public static void kickMember(MinecraftServer server, ICommandSender sender, Clan selectedClan, GameProfile target) throws CommandException {
        if(selectedClan.removeMember(target.getId())) {
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.kick.success", target.getName(), selectedClan.getName()).setStyle(TextStyles.GREEN));
            selectedClan.messageAllOnline(sender instanceof EntityPlayerMP ? (EntityPlayerMP)sender : null, TextStyles.YELLOW, "commands.clan.kick.kicked_other", target.getName(), selectedClan.getName(), sender.getDisplayName().getFormattedText());
            if(ArrayUtils.contains(server.getPlayerList().getOnlinePlayerProfiles(), target)) {
                EntityPlayerMP targetPlayer = CommandBase.getPlayer(server, sender, target.getName());
                if(sender instanceof EntityPlayerMP && !((EntityPlayerMP) sender).getUniqueID().equals(target.getId()))
                    targetPlayer.sendMessage(TranslationUtil.getTranslation(targetPlayer.getUniqueID(), "commands.clan.kick.kicked", selectedClan.getName(), sender.getName()).setStyle(TextStyles.YELLOW));
                if(selectedClan.getId().equals(PlayerData.getDefaultClan(targetPlayer.getUniqueID())))
                    PlayerData.updateDefaultClan(targetPlayer.getUniqueID(), selectedClan.getId());
            }
        } else
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.kick.fail", target.getName(), selectedClan.getName()).setStyle(TextStyles.RED));
    }
}
