package the_fireplace.clans.util;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.Clans;
import the_fireplace.clans.api.event.PreLandAbandonEvent;
import the_fireplace.clans.api.event.PreLandClaimEvent;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.data.ClaimDataManager;
import the_fireplace.clans.data.PlayerDataManager;
import the_fireplace.clans.model.ChunkPosition;
import the_fireplace.clans.model.ChunkPositionWithData;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.util.translation.TranslationUtil;

import java.util.Map;
import java.util.UUID;

public class ClanManagementUtil {
    public static boolean checkAndAttemptClaim(EntityPlayerMP sender, Clan selectedClan, boolean isOpclanCommand, boolean force) {
        return checkAndAttemptClaim(sender, selectedClan, new ChunkPositionWithData(sender.chunkCoordX, sender.chunkCoordZ, sender.getEntityWorld().provider.getDimension()), isOpclanCommand, force);
    }

    public static boolean checkAndAttemptClaim(EntityPlayerMP sender, Clan selectedClan, ChunkPositionWithData claimChunk, boolean isOpclanCommand, boolean force) {
        UUID claimOwner = ClaimDataManager.getChunkClanId(claimChunk);
        Clan claimClan = ClanCache.getClanById(claimOwner);
        String clanCommandString = isOpclanCommand ? "opclan" : "clan";
        if(claimOwner != null && claimClan != null && (!force || claimOwner.equals(selectedClan.getClanId()))) {
            if(claimOwner.equals(selectedClan.getClanId()))
                sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.claim.taken", selectedClan.getClanName()).setStyle(TextStyles.YELLOW));
            else
                sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands."+clanCommandString+".claim.taken_other", claimClan.getClanName()).setStyle(TextStyles.RED));
        } else {
            if(claimClan != null) {//In this scenario, we are always forcing the claim, so we should refund the clan the land is being taken from
                Clans.getPaymentHandler().addAmount(Clans.getConfig().getClaimChunkCost(), claimClan.getClanId());
            }
            if(selectedClan.isOpclan()) {
                ClaimDataManager.swapChunk(claimChunk, claimOwner, selectedClan.getClanId());
                sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.claim.success", selectedClan.getClanName()).setStyle(TextStyles.GREEN));
                return true;
            } else {
                if (force || !Clans.getConfig().isForceConnectedClaims() || ChunkUtils.hasConnectedClaim(claimChunk, selectedClan.getClanId()) || selectedClan.getClaimCount() == 0) {
                    if (force || Clans.getConfig().getMaxClanPlayerClaims() <= 0 || selectedClan.getClaimCount() < selectedClan.getMaxClaimCount()) {
                        if (selectedClan.getClaimCount() > 0)
                            claimChunk(sender, claimChunk, selectedClan, force);
                        else if (Clans.getConfig().getMinClanHomeDist() > 0 && Clans.getConfig().getInitialClaimSeparationMultiplier() > 0) {
                            boolean inClanHomeRange = false;
                            for (Map.Entry<Clan, BlockPos> pos : ClanCache.getClanHomes().entrySet())
                                if (!pos.getKey().getClanId().equals(selectedClan.getClanId()) && pos.getKey().hasHome() && pos.getValue() != null && pos.getValue().getDistance(sender.getPosition().getX(), sender.getPosition().getY(), sender.getPosition().getZ()) < Clans.getConfig().getMinClanHomeDist() * Clans.getConfig().getInitialClaimSeparationMultiplier())
                                    inClanHomeRange = true;
                            if (inClanHomeRange) {
                                if (Clans.getConfig().isEnforceInitialClaimSeparation())
                                    sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.claim.proximity_error", Clans.getConfig().getMinClanHomeDist() * Clans.getConfig().getInitialClaimSeparationMultiplier()).setStyle(TextStyles.RED));
                                else if (PlayerDataManager.getClaimWarning(sender.getUniqueID()))
                                    return claimChunk(sender, claimChunk, selectedClan, force);
                                else {
                                    sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.claim.proximity_warning", Clans.getConfig().getMinClanHomeDist() * Clans.getConfig().getInitialClaimSeparationMultiplier()).setStyle(TextStyles.YELLOW));
                                    PlayerDataManager.setClaimWarning(sender.getUniqueID(), true);
                                }
                            } else
                                return claimChunk(sender, claimChunk, selectedClan, force);
                        } else
                            return claimChunk(sender, claimChunk, selectedClan, force);
                    } else
                        sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.claim.maxed", selectedClan.getClanName(), selectedClan.getMaxClaimCount()).setStyle(TextStyles.RED));
                } else
                    sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.claim.disconnected", selectedClan.getClanName()).setStyle(TextStyles.RED));
            }
        }
        return false;
    }

    public static boolean claimChunk(EntityPlayerMP sender, ChunkPositionWithData claimChunk, Clan selectedClan, boolean force) {
        if (force || Clans.getPaymentHandler().deductAmount(Clans.getConfig().getClaimChunkCost(), selectedClan.getClanId())) {
            PreLandClaimEvent event = ClansEventManager.fireEvent(new PreLandClaimEvent(sender.world, sender.world.getChunk(claimChunk.getPosX(), claimChunk.getPosZ()), claimChunk, sender.getUniqueID(), selectedClan));
            if(!event.isCancelled) {
                ClaimDataManager.swapChunk(claimChunk, null, selectedClan.getClanId());
                sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.claim.success", selectedClan.getClanName()).setStyle(TextStyles.GREEN));
                return true;
            } else {
                sender.sendMessage(event.cancelledMessage);
                return false;
            }
        } else
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.claim.insufficient_funds", selectedClan.getClanName(), Clans.getConfig().getClaimChunkCost(), Clans.getPaymentHandler().getCurrencyName(Clans.getConfig().getClaimChunkCost())).setStyle(TextStyles.RED));
        return false;
    }

    public static boolean checkAndAttemptAbandon(EntityPlayerMP sender, Clan selectedClan, boolean isOpclanCommand, boolean force) {
        return checkAndAttemptAbandon(sender, selectedClan, new ChunkPositionWithData(sender.chunkCoordX, sender.chunkCoordZ, sender.getEntityWorld().provider.getDimension()), isOpclanCommand, force);
    }

    public static boolean checkAndAttemptAbandon(EntityPlayerMP sender, Clan selectedClan, ChunkPositionWithData claimChunk, boolean isOpclanCommand, boolean force) {
        Chunk c = sender.getEntityWorld().getChunk(sender.getPosition());
        UUID claimOwnerClanId = ChunkUtils.getChunkOwner(c);
        if(claimOwnerClanId != null) {
            Clan claimOwnerClan = ClanCache.getClanById(claimOwnerClanId);
            if(claimOwnerClan == null) {
                ChunkUtils.clearChunkOwner(c);
                sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.abandonclaim.success", "null").setStyle(TextStyles.GREEN));
                return true;
            }
            if(force || claimOwnerClanId.equals(selectedClan.getClanId())) {
                if(force || claimOwnerClan.isOpclan() || !Clans.getConfig().isForceConnectedClaims() || ChunkUtils.canBeAbandoned(c, claimOwnerClanId)) {
                    PreLandAbandonEvent event = ClansEventManager.fireEvent(new PreLandAbandonEvent(sender.world, c, new ChunkPosition(c), sender.getUniqueID(), claimOwnerClan));
                    if(!event.isCancelled) {
                        abandonClaim(sender, c, claimOwnerClan);
                        ChunkUtils.clearChunkOwner(c);
                        sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.abandonclaim.success", claimOwnerClan.getClanName()).setStyle(TextStyles.GREEN));
                        return true;
                    } else
                        sender.sendMessage(event.cancelledMessage);
                } else {//We are forcing connected claims and there is a claim connected
                    //Prevent creation of disconnected claims
                    return abandonClaimWithAdjacencyCheck(sender, c, claimOwnerClan);
                }
            } else
                sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.abandonclaim.wrongclan", selectedClan.getClanName()).setStyle(TextStyles.RED));
        } else
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.abandonclaim.notclaimed").setStyle(TextStyles.RED));
        return false;
    }

    public static void abandonClaim(EntityPlayerMP sender, Chunk c, Clan targetClan) {
        //Unset clan home if it is in the chunk
        if (targetClan.getHome() != null
                && targetClan.hasHome()
                && sender.dimension == targetClan.getHomeDim()
                && targetClan.getHome().getX() >= c.getPos().getXStart()
                && targetClan.getHome().getX() <= c.getPos().getXEnd()
                && targetClan.getHome().getZ() >= c.getPos().getZStart()
                && targetClan.getHome().getZ() <= c.getPos().getZEnd()) {
            targetClan.unsetHome();
        }

        ClaimDataManager.delChunk(targetClan, c.x, c.z, c.getWorld().provider.getDimension());
        if(!targetClan.isOpclan())
            Clans.getPaymentHandler().addAmount(Clans.getConfig().getClaimChunkCost(), targetClan.getClanId());
    }

    public static boolean abandonClaimWithAdjacencyCheck(EntityPlayerMP sender, Chunk c, Clan targetClan) {
        boolean allowed = true;
        for (Chunk checkChunk : ChunkUtils.getConnectedClaims(c, targetClan.getClanId())) {
            if (ChunkUtils.getConnectedClaims(checkChunk, targetClan.getClanId()).equals(Lists.newArrayList(c))) {
                allowed = false;
                break;
            }
        }
        if (allowed) {
            PreLandAbandonEvent event = ClansEventManager.fireEvent(new PreLandAbandonEvent(sender.world, c, new ChunkPosition(c), sender.getUniqueID(), targetClan));
            if(!event.isCancelled) {
                //Unset clan home if it is in the chunk
                abandonClaim(sender, c, targetClan);
                ChunkUtils.clearChunkOwner(c);
                sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.abandonclaim.success", targetClan.getClanName()).setStyle(TextStyles.GREEN));
                return true;
            } else
                sender.sendMessage(event.cancelledMessage);
        } else
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.opclan.abandonclaim.disconnected").setStyle(TextStyles.RED));
        return false;
    }
}
