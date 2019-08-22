package the_fireplace.clans.logic;

import com.google.common.collect.Lists;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketEntityEquipment;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.Clans;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.cache.RaidingParties;
import the_fireplace.clans.data.ClaimData;
import the_fireplace.clans.data.RaidCollectionDatabase;
import the_fireplace.clans.data.RaidRestoreDatabase;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.util.BlockSerializeUtil;
import the_fireplace.clans.util.ChunkUtils;
import the_fireplace.clans.util.PermissionManager;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Logic for land protection events goes here. All methods return true if the event should get cancelled, false if it should be allowed to finish executing.
 */
public class LandProtectionEventLogic {
    public static boolean onBlockBroken(World world, BlockPos pos, EntityPlayer breaker) {
        if(!world.isRemote) {
            Chunk c = world.getChunk(pos);
            Clan chunkClan = ChunkUtils.getChunkOwnerClan(c);
            if (chunkClan != null) {
                if (breaker instanceof EntityPlayerMP) {
                    ArrayList<Clan> playerClans = ClanCache.getPlayerClans(breaker.getUniqueID());
                    boolean isRaided = RaidingParties.isRaidedBy(chunkClan, breaker);
                    if (Clans.getConfig().allowBreakProtection() && !ClanCache.isClaimAdmin((EntityPlayerMP) breaker) && (playerClans.isEmpty() || !playerClans.contains(chunkClan)) && !isRaided && !RaidingParties.preparingRaidOnBorderland(breaker, chunkClan, c) && !Clans.getMinecraftHelper().isAllowedNonPlayerEntity(breaker)) {
                        breaker.sendMessage(TranslationUtil.getTranslation(breaker.getUniqueID(), ChunkUtils.isBorderland(c) ? "clans.protection.break.borderland" : "clans.protection.break.claimed").setStyle(TextStyles.RED));
                        return true;
                    } else if (isRaided && !ChunkUtils.isBorderland(c)) {
                        IBlockState targetState = world.getBlockState(pos);
                        if (targetState.getBlock().hasTileEntity(targetState)) {
                            if(ClanCache.isClaimAdmin((EntityPlayerMP) breaker))
                                breaker.sendMessage(TranslationUtil.getTranslation(breaker.getUniqueID(), "clans.protection.break.raid").setStyle(TextStyles.RED));
                            else
                                breaker.sendMessage(TranslationUtil.getTranslation(breaker.getUniqueID(), "clans.protection.break.claimed_raid").setStyle(TextStyles.RED));
                            return true;
                        } else
                            RaidRestoreDatabase.addRestoreBlock(c.getWorld().provider.getDimension(), c, pos, BlockSerializeUtil.blockToString(targetState));
                    }
                }
                return false;
            }
            if (Clans.getConfig().allowBreakProtection() && Clans.getConfig().isProtectWilderness() && (Clans.getConfig().getMinWildernessY() < 0 ? pos.getY() >= world.getSeaLevel() : pos.getY() >= Clans.getConfig().getMinWildernessY()) && !Clans.getMinecraftHelper().isAllowedNonPlayerEntity(breaker) && (!(breaker instanceof EntityPlayerMP) || (!ClanCache.isClaimAdmin((EntityPlayerMP) breaker) && !PermissionManager.hasPermission((EntityPlayerMP)breaker, PermissionManager.PROTECTION_PREFIX+"break.protected_wilderness")))) {
                breaker.sendMessage(TranslationUtil.getTranslation(breaker.getUniqueID(), "clans.protection.break.wilderness").setStyle(TextStyles.RED));
                return true;
            }
        }
        return false;
    }

    public static boolean onCropTrampled(World world, BlockPos pos, @Nullable EntityPlayer breakingPlayer) {
        if(!world.isRemote && Clans.getConfig().allowBreakProtection()) {
            Chunk c = world.getChunk(pos);
            Clan chunkClan = ChunkUtils.getChunkOwnerClan(c);
            if (chunkClan != null) {
                if (breakingPlayer != null) {
                    ArrayList<Clan> playerClans = ClanCache.getPlayerClans(breakingPlayer.getUniqueID());
                    boolean isRaided = RaidingParties.isRaidedBy(chunkClan, breakingPlayer);
                    return (playerClans.isEmpty() || !playerClans.contains(chunkClan)) && !isRaided && !RaidingParties.preparingRaidOnBorderland(breakingPlayer, chunkClan, c);
                }
            }
        }
        return false;
    }

    public static boolean onBlockPlaced(World world, BlockPos pos, EntityPlayer placer, EntityEquipmentSlot hand, Block placedBlock) {
        if(!world.isRemote) {
            Chunk c = world.getChunk(pos);
            if (placer instanceof EntityPlayerMP) {
                Clan chunkClan = ChunkUtils.getChunkOwnerClan(c);
                if (chunkClan != null) {
                    ArrayList<Clan> playerClans = ClanCache.getPlayerClans(placer.getUniqueID());
                    if (Clans.getConfig().allowPlaceProtection() && (!ClanCache.isClaimAdmin((EntityPlayerMP) placer) && (playerClans.isEmpty() || (!playerClans.contains(chunkClan) && !RaidingParties.isRaidedBy(chunkClan, placer) && !RaidingParties.preparingRaidOnBorderland(placer, chunkClan, c)))) && !Clans.getMinecraftHelper().isAllowedNonPlayerEntity(placer)) {
                        if(((EntityPlayerMP) placer).connection != null)
                            ((EntityPlayerMP) placer).connection.sendPacket(new SPacketEntityEquipment(placer.getEntityId(), hand, placer.getItemStackFromSlot(hand)));
                        placer.sendMessage(TranslationUtil.getTranslation(placer.getUniqueID(), ChunkUtils.isBorderland(c) ? "clans.protection.place.borderland" : "clans.protection.place.territory").setStyle(TextStyles.RED));
                        return true;
                    } else if (RaidingParties.hasActiveRaid(chunkClan) && !Clans.getConfig().disableRaidRollback()) {
                        ItemStack out = placer.getHeldItem(hand.getSlotType().equals(EntityEquipmentSlot.Type.HAND) && hand.equals(EntityEquipmentSlot.OFFHAND) ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND).copy();
                        out.setCount(1);
                        if(!Clans.getConfig().isNoReclaimTNT() || !(placedBlock instanceof BlockTNT))
                            RaidCollectionDatabase.getInstance().addCollectItem(placer.getUniqueID(), out);
                        RaidRestoreDatabase.addRemoveBlock(world.provider.getDimension(), c, pos);
                    }
                    return false;
                }
                if (Clans.getConfig().allowPlaceProtection() && !ClanCache.isClaimAdmin((EntityPlayerMP) placer) && (!PermissionManager.permissionManagementExists() || !PermissionManager.hasPermission((EntityPlayerMP)placer, PermissionManager.PROTECTION_PREFIX+"build.protected_wilderness")) && Clans.getConfig().isProtectWilderness() && (Clans.getConfig().getMinWildernessY() < 0 ? pos.getY() >= world.getSeaLevel() : pos.getY() >= Clans.getConfig().getMinWildernessY()) && !Clans.getMinecraftHelper().isAllowedNonPlayerEntity(placer)) {
                    if(((EntityPlayerMP) placer).connection != null)
                        ((EntityPlayerMP) placer).connection.sendPacket(new SPacketEntityEquipment(placer.getEntityId(), hand, placer.getItemStackFromSlot(hand)));
                    placer.inventory.markDirty();
                    placer.sendMessage(TranslationUtil.getTranslation(placer.getUniqueID(), "clans.protection.place.wilderness").setStyle(TextStyles.RED));
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean onFluidPlaceBlock(World world, BlockPos sourceLiquidPos, BlockPos fluidPlacingPos) {
        if(!world.isRemote && Clans.getConfig().allowPlaceProtection()) {
            Chunk c = world.getChunk(fluidPlacingPos);
            UUID chunkOwner = ChunkUtils.getChunkOwner(c);
            if (chunkOwner != null) {
                Chunk sourceChunk = world.getChunk(sourceLiquidPos);
                UUID sourceChunkOwner = ChunkUtils.getChunkOwner(sourceChunk);
                return !chunkOwner.equals(sourceChunkOwner);
            }
        }
        return false;
    }

    public static boolean rightClickBlock(World world, BlockPos pos, EntityPlayer player, ItemStack heldItem) {
        if(!world.isRemote && Clans.getConfig().allowInteractionProtection()) {
            Chunk c = world.getChunk(pos);
            Clan chunkClan = ChunkUtils.getChunkOwnerClan(c);
            if (chunkClan != null) {
                if (player instanceof EntityPlayerMP) {
                    ArrayList<Clan> playerClan = ClanCache.getPlayerClans(player.getUniqueID());
                    IBlockState targetState = world.getBlockState(pos);
                    boolean isRaidedBy = RaidingParties.isRaidedBy(chunkClan, player);
                    if (!ClanCache.isClaimAdmin((EntityPlayerMP) player) && (playerClan.isEmpty() || !playerClan.contains(chunkClan)) && !RaidingParties.preparingRaidOnBorderland(player, chunkClan, c) && (!isRaidedBy || !Clans.getConfig().enableStealing() && targetState.getBlock() instanceof BlockContainer || targetState.getBlock() instanceof BlockDragonEgg)) {
                        if (!(heldItem.getItem() instanceof ItemBlock)) {
                            cancelBlockInteraction(world, pos, player, targetState);
                            return true;
                        } else if ((!isRaidedBy || !Clans.getConfig().enableStealing()) && targetState.getBlock() instanceof BlockContainer || targetState.getBlock() instanceof BlockDragonEgg) {
                            cancelBlockInteraction(world, pos, player, targetState);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private static void cancelBlockInteraction(World world, BlockPos pos, EntityPlayer interactingPlayer, IBlockState targetState) {
        interactingPlayer.sendMessage(TranslationUtil.getTranslation(interactingPlayer.getUniqueID(), "clans.protection.interact.territory").setStyle(TextStyles.RED));
        //Update the client informing it the interaction did not happen. Go in all directions in case surrounding blocks would have been affected.
        world.notifyBlockUpdate(pos, targetState, targetState, 2);
        world.notifyBlockUpdate(pos.up(), targetState, targetState, 2);
        world.notifyBlockUpdate(pos.down(), targetState, targetState, 2);
        world.notifyBlockUpdate(pos.east(), targetState, targetState, 2);
        world.notifyBlockUpdate(pos.west(), targetState, targetState, 2);
        world.notifyBlockUpdate(pos.north(), targetState, targetState, 2);
        world.notifyBlockUpdate(pos.south(), targetState, targetState, 2);
    }

    public static void onDetonate(World world, List<BlockPos> affectedBlocks, List<Entity> affectedEntities) {
        if(!world.isRemote) {
            ArrayList<BlockPos> removeBlocks = Lists.newArrayList();
            for (BlockPos pos : affectedBlocks) {
                Chunk c = world.getChunk(pos);
                UUID chunkOwner = ChunkUtils.getChunkOwner(c);
                Clan chunkClan = ClanCache.getClanById(chunkOwner);
                IBlockState targetState = world.getBlockState(pos);
                if (chunkClan != null) {
                    if (RaidingParties.hasActiveRaid(chunkClan) && !Clans.getConfig().disableRaidRollback() && !ChunkUtils.isBorderland(c) && !targetState.getBlock().hasTileEntity(targetState) && !(targetState.getBlock() instanceof BlockAir) && !(targetState.getBlock() instanceof BlockLiquid))
                        RaidRestoreDatabase.addRestoreBlock(c.getWorld().provider.getDimension(), c, pos, BlockSerializeUtil.blockToString(targetState));
                    else if (!Clans.getConfig().isChainTNT() || !(targetState.getBlock() instanceof BlockTNT))
                        removeBlocks.add(pos);
                } else if (Clans.getConfig().isProtectWilderness() && (Clans.getConfig().getMinWildernessY() < 0 ? pos.getY() >= world.getSeaLevel() : pos.getY() >= Clans.getConfig().getMinWildernessY()) && (!Clans.getConfig().isChainTNT() || !(targetState.getBlock() instanceof BlockTNT)))
                    removeBlocks.add(pos);
            }
            if(Clans.getConfig().allowBreakProtection())
                affectedBlocks.removeAll(removeBlocks);
            if(Clans.getConfig().allowInjuryProtection()) {
                ArrayList<Entity> removeEntities = Lists.newArrayList();
                for (Entity entity : affectedEntities) {
                    if (entity instanceof EntityPlayer || (entity instanceof EntityTameable && ((EntityTameable) entity).getOwnerId() != null)) {
                        Chunk c = world.getChunk(entity.getPosition());
                        UUID chunkOwner = ChunkUtils.getChunkOwner(c);
                        Clan chunkClan = ClanCache.getClanById(chunkOwner);
                        ArrayList<Clan> entityClans = entity instanceof EntityPlayer ? ClanCache.getPlayerClans(entity.getUniqueID()) : ClanCache.getPlayerClans(((EntityTameable) entity).getOwnerId());
                        if (chunkClan != null && !ChunkUtils.isBorderland(c) && !entityClans.isEmpty() && entityClans.contains(chunkClan) && !RaidingParties.hasActiveRaid(chunkClan))
                            removeEntities.add(entity);
                    }
                }
                affectedEntities.removeAll(removeEntities);
            }
        }
    }

    public static boolean onLivingDamage(Entity target, @Nullable Entity attacker) {
        if(!target.getEntityWorld().isRemote && Clans.getConfig().allowInjuryProtection()) {
            Chunk c = target.getEntityWorld().getChunk(target.getPosition());
            Clan chunkClan = ClanCache.getClanById(ChunkUtils.getChunkOwner(c));
            if(attacker instanceof EntityPlayerMP && ClanCache.isClaimAdmin((EntityPlayerMP) attacker) || chunkClan == null || ChunkUtils.isBorderland(c))
                return false;
            if(target instanceof EntityPlayer || (target instanceof EntityTameable && ((EntityTameable) target).getOwnerId() != null)) {
                ArrayList<Clan> entityClans = target instanceof EntityPlayer ? ClanCache.getPlayerClans(target.getUniqueID()) : ClanCache.getPlayerClans(((EntityTameable) target).getOwnerId());
                if (!entityClans.isEmpty() && entityClans.contains(chunkClan) && !RaidingParties.hasActiveRaid(chunkClan) && (attacker instanceof EntityPlayer || (attacker instanceof EntityTameable && ((EntityTameable) attacker).getOwnerId() != null)) && !Clans.getMinecraftHelper().isAllowedNonPlayerEntity(attacker))
                    return true;
                else if((RaidingParties.hasActiveRaid(chunkClan) && (attacker instanceof EntityPlayer || (attacker instanceof EntityTameable && ((EntityTameable) attacker).getOwnerId() != null))) && !Clans.getMinecraftHelper().isAllowedNonPlayerEntity(attacker)) {
                    for (Clan entityClan : entityClans)
                        if (RaidingParties.isRaidedBy(entityClan, attacker instanceof EntityPlayer ? (EntityPlayer) attacker : (((EntityTameable) attacker).getOwner() instanceof EntityPlayer ? (EntityPlayer) ((EntityTameable) attacker).getOwner() : null)))
                            return false;
                    return true;
                }
            } else {//Entity is not a player
                if(attacker instanceof EntityPlayer || (attacker instanceof EntityTameable && ((EntityTameable) attacker).getOwnerId() != null)) {
                    if (RaidingParties.isRaidedBy(chunkClan, attacker instanceof EntityPlayer ? (EntityPlayer) attacker : (((EntityTameable) attacker).getOwner() instanceof EntityPlayer ? (EntityPlayer) ((EntityTameable) attacker).getOwner() : null)))
                        return false;//Raiders can harm things
                    UUID sourceId = attacker instanceof EntityPlayer ? attacker.getUniqueID() : ((EntityTameable) attacker).getOwnerId();
                    ArrayList<Clan> sourceClans = ClanCache.getPlayerClans(sourceId);
                    return !sourceClans.contains(chunkClan)
                            && !RaidingParties.hasActiveRaid(chunkClan)
                            && !Clans.getMinecraftHelper().isAllowedNonPlayerEntity(attacker)
                            && (!chunkClan.isLimitless() || !(target instanceof IMob));//Players can harm things
                }
            }
        }
        return false;
    }

    public static boolean onEntitySpawn(World world, Entity entity) {
        return Clans.getConfig().isPreventMobsOnClaims() && !world.isRemote && entity instanceof IMob && ClaimData.getChunkClan(entity.chunkCoordX, entity.chunkCoordZ, entity.dimension) != null && (Clans.getConfig().isPreventMobsOnBorderlands() || !ClaimData.getChunkPositionData(entity.chunkCoordX, entity.chunkCoordZ, entity.dimension).isBorderland());
    }
}
