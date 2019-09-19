package the_fireplace.clans.logic;

import com.google.common.collect.Lists;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.IAnimals;
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
import the_fireplace.clans.model.ChunkPositionWithData;
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
 * Logic for land protection events goes here.
 */
public class LandProtectionEventLogic {
    public static boolean shouldCancelBlockBroken(World world, BlockPos pos, EntityPlayer breaker) {
        if(!world.isRemote && Clans.getConfig().allowBuildProtection()) {
            Chunk c = world.getChunk(pos);
            Clan chunkClan = ChunkUtils.getChunkOwnerClan(c);
            if (chunkClan != null) {
                if (breaker instanceof EntityPlayerMP) {
                    boolean isRaided = RaidingParties.isRaidedBy(chunkClan, breaker);
                    IBlockState targetState = world.getBlockState(pos);
                    if(chunkClan.isLocked(pos) && !chunkClan.isLockOwner(pos, breaker.getUniqueID()) && !chunkClan.hasPerm("lockadmin", breaker.getUniqueID())) {
                        breaker.sendMessage(TranslationUtil.getTranslation(breaker.getUniqueID(), "clans.protection.break.locked", chunkClan.getLockOwner(pos)).setStyle(TextStyles.RED));
                        return true;
                    }
                    if (!ClanCache.isClaimAdmin((EntityPlayerMP) breaker)
                            && !chunkClan.hasPerm("build", breaker.getUniqueID())
                            && !isRaided
                            && !RaidingParties.preparingRaidOnBorderland(breaker, chunkClan, c)
                            && !Clans.getMinecraftHelper().isAllowedNonPlayerEntity(breaker, false)) {
                        breaker.sendMessage(TranslationUtil.getTranslation(breaker.getUniqueID(), ChunkUtils.isBorderland(c) ? "clans.protection.break.borderland" : "clans.protection.break.claimed").setStyle(TextStyles.RED));
                        return true;
                    } else if (isRaided && !ChunkUtils.isBorderland(c)) {
                        if (targetState.getBlock().hasTileEntity(targetState)) {
                            if(ClanCache.isClaimAdmin((EntityPlayerMP) breaker))
                                breaker.sendMessage(TranslationUtil.getTranslation(breaker.getUniqueID(), "clans.protection.break.raid").setStyle(TextStyles.RED));
                            else
                                breaker.sendMessage(TranslationUtil.getTranslation(breaker.getUniqueID(), "clans.protection.break.claimed_raid").setStyle(TextStyles.RED));
                            return true;
                        }
                    } else if(chunkClan.isLocked(pos) && !chunkClan.hasLockAccess(pos, breaker.getUniqueID(), targetState.getBlock() instanceof BlockContainer ? "access" : "build")) {
                        breaker.sendMessage(TranslationUtil.getTranslation(breaker.getUniqueID(), "clans.protection.break.locked").setStyle(TextStyles.RED));
                        return true;
                    }
                }
                return false;
            }
            if (Clans.getConfig().isProtectWilderness()
                    && (Clans.getConfig().getMinWildernessY() < 0 ? pos.getY() >= world.getSeaLevel() : pos.getY() >= Clans.getConfig().getMinWildernessY())
                    && !Clans.getMinecraftHelper().isAllowedNonPlayerEntity(breaker, false)
                    && (!(breaker instanceof EntityPlayerMP)
                        || (!ClanCache.isClaimAdmin((EntityPlayerMP) breaker)
                            && !PermissionManager.hasPermission((EntityPlayerMP)breaker, PermissionManager.PROTECTION_PREFIX+"break.protected_wilderness")))) {
                breaker.sendMessage(TranslationUtil.getTranslation(breaker.getUniqueID(), "clans.protection.break.wilderness").setStyle(TextStyles.RED));
                return true;
            }
        }
        return false;
    }

    public static void onBlockBroken(World world, BlockPos pos, EntityPlayer breaker) {
        if (!world.isRemote) {
            Chunk c = world.getChunk(pos);
            if (breaker instanceof EntityPlayerMP) {
                Clan chunkClan = ChunkUtils.getChunkOwnerClan(c);
                if (chunkClan != null) {
                    if (RaidingParties.hasActiveRaid(chunkClan) && !Clans.getConfig().disableRaidRollback()) {
                        IBlockState targetState = world.getBlockState(pos);
                        RaidRestoreDatabase.addRestoreBlock(c.getWorld().provider.getDimension(), c, pos, BlockSerializeUtil.blockToString(targetState));
                    }
                }
            }
        }
    }

    public static boolean shouldCancelCropTrample(World world, BlockPos pos, @Nullable EntityPlayer breakingPlayer) {
        if(!world.isRemote && Clans.getConfig().allowBuildProtection()) {
            Chunk c = world.getChunk(pos);
            Clan chunkClan = ChunkUtils.getChunkOwnerClan(c);
            if (chunkClan != null) {
                if (breakingPlayer != null) {
                    List<Clan> playerClans = ClanCache.getPlayerClans(breakingPlayer.getUniqueID());
                    return (playerClans.isEmpty() || !playerClans.contains(chunkClan))
                            && !RaidingParties.isRaidedBy(chunkClan, breakingPlayer)
                            && !RaidingParties.preparingRaidOnBorderland(breakingPlayer, chunkClan, c);
                }
            }
        }
        return false;
    }

    public static boolean shouldCancelBlockPlacement(World world, BlockPos pos, EntityPlayer placer, EntityEquipmentSlot hand, Block placedBlock) {
        if(!world.isRemote) {
            Chunk c = world.getChunk(pos);
            if (placer instanceof EntityPlayerMP) {
                Clan chunkClan = ChunkUtils.getChunkOwnerClan(c);
                if (chunkClan != null) {
                    if (Clans.getConfig().allowBuildProtection()
                            && !ClanCache.isClaimAdmin((EntityPlayerMP) placer)
                            && !chunkClan.hasPerm("build", placer.getUniqueID())
                            && !RaidingParties.isRaidedBy(chunkClan, placer)
                            && !RaidingParties.preparingRaidOnBorderland(placer, chunkClan, c)
                            && !Clans.getMinecraftHelper().isAllowedNonPlayerEntity(placer, false)) {
                        //Notify the player that the block wasn't placed
                        if(((EntityPlayerMP) placer).connection != null)
                            ((EntityPlayerMP) placer).connection.sendPacket(new SPacketEntityEquipment(placer.getEntityId(), hand, placer.getItemStackFromSlot(hand)));
                        placer.sendMessage(TranslationUtil.getTranslation(placer.getUniqueID(), ChunkUtils.isBorderland(c) ? "clans.protection.place.borderland" : "clans.protection.place.territory").setStyle(TextStyles.RED));
                        return true;
                    }
                    return false;
                }
                if (Clans.getConfig().allowBuildProtection()
                        && !ClanCache.isClaimAdmin((EntityPlayerMP) placer)
                        && (!PermissionManager.permissionManagementExists()
                            || !PermissionManager.hasPermission((EntityPlayerMP)placer, PermissionManager.PROTECTION_PREFIX+"build.protected_wilderness"))
                        && Clans.getConfig().isProtectWilderness()
                        && (Clans.getConfig().getMinWildernessY() < 0 ? pos.getY() >= world.getSeaLevel() : pos.getY() >= Clans.getConfig().getMinWildernessY())
                        && !Clans.getMinecraftHelper().isAllowedNonPlayerEntity(placer, false)) {
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

    public static void onBlockPlaced(World world, BlockPos pos, EntityPlayer placer, EntityEquipmentSlot hand, Block placedBlock) {
        if(!world.isRemote) {
            Chunk c = world.getChunk(pos);
            if (placer instanceof EntityPlayerMP) {
                Clan chunkClan = ChunkUtils.getChunkOwnerClan(c);
                if (chunkClan != null) {
                    if (RaidingParties.hasActiveRaid(chunkClan) && !Clans.getConfig().disableRaidRollback()) {
                        ItemStack out = placer.getHeldItem(hand.getSlotType().equals(EntityEquipmentSlot.Type.HAND) && hand.equals(EntityEquipmentSlot.OFFHAND) ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND).copy();
                        out.setCount(1);
                        if (!Clans.getConfig().isNoReclaimTNT() || !(placedBlock instanceof BlockTNT))
                            RaidCollectionDatabase.getInstance().addCollectItem(placer.getUniqueID(), out);
                        RaidRestoreDatabase.addRemoveBlock(world.provider.getDimension(), c, pos);
                    }
                }
            }
        }
    }

    public static boolean shouldCancelFluidPlaceBlock(World world, BlockPos sourceLiquidPos, BlockPos fluidPlacingPos) {
        if(!world.isRemote && Clans.getConfig().allowBuildProtection()) {
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

    public static boolean shouldCancelRightClickBlock(World world, BlockPos pos, EntityPlayer player, ItemStack heldItem) {
        if(!world.isRemote && Clans.getConfig().allowInteractionProtection()) {
            Chunk c = world.getChunk(pos);
            Clan chunkClan = ChunkUtils.getChunkOwnerClan(c);
            if (chunkClan != null) {
                if (player instanceof EntityPlayerMP) {
                    IBlockState targetState = world.getBlockState(pos);
                    boolean isRaidedBy = RaidingParties.isRaidedBy(chunkClan, player);
                    if(chunkClan.isLocked(pos)) {
                        if(!chunkClan.hasLockAccess(pos, player.getUniqueID(), targetState.getBlock() instanceof BlockContainer ? "access" : "interact")) {
                            player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "clans.protection.interact.locked").setStyle(TextStyles.RED));
                            return true;
                        } else
                            return false;
                    }
                    if (!ClanCache.isClaimAdmin((EntityPlayerMP) player)
                            && !chunkClan.hasPerm("interact", player.getUniqueID())
                            && !(targetState.getBlock() instanceof BlockContainer && chunkClan.hasPerm("access", player.getUniqueID()))
                            && !RaidingParties.preparingRaidOnBorderland(player, chunkClan, c)
                            && (!isRaidedBy
                                || !Clans.getConfig().enableStealing() && targetState.getBlock() instanceof BlockContainer
                                || targetState.getBlock() instanceof BlockDragonEgg)) {
                        if (!(heldItem.getItem() instanceof ItemBlock)) {
                            cancelBlockInteraction(world, pos, player, targetState);
                            return true;
                        } else if (!isRaidedBy
                                || !Clans.getConfig().enableStealing() && targetState.getBlock() instanceof BlockContainer
                                || targetState.getBlock() instanceof BlockDragonEgg) {
                            cancelBlockInteraction(world, pos, player, targetState);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean shouldCancelMinecartInteract(EntityMinecart minecart, EntityPlayer player) {
        if(!minecart.world.isRemote && Clans.getConfig().allowInteractionProtection()) {
            Chunk c = minecart.world.getChunk(minecart.getPosition());
            Clan chunkClan = ChunkUtils.getChunkOwnerClan(c);
            if (chunkClan != null && !ChunkUtils.isBorderland(c)) {
                if (player instanceof EntityPlayerMP) {
                    boolean isRaidedBy = RaidingParties.isRaidedBy(chunkClan, player);
                    if (!ClanCache.isClaimAdmin((EntityPlayerMP) player)
                            && !chunkClan.hasPerm("access", player.getUniqueID())
                            && !RaidingParties.preparingRaidOnBorderland(player, chunkClan, c)) {
                        return !isRaidedBy;
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
            if(Clans.getConfig().allowBuildProtection())
                affectedBlocks.removeAll(removeBlocks);
            if(Clans.getConfig().allowInjuryProtection()) {
                ArrayList<Entity> removeEntities = Lists.newArrayList();
                for (Entity entity : affectedEntities) {
                    if (entity instanceof EntityPlayer || (entity instanceof EntityTameable && ((EntityTameable) entity).getOwnerId() != null)) {
                        Chunk c = world.getChunk(entity.getPosition());
                        UUID chunkOwner = ChunkUtils.getChunkOwner(c);
                        Clan chunkClan = ClanCache.getClanById(chunkOwner);
                        List<Clan> entityClans = entity instanceof EntityPlayer ? ClanCache.getPlayerClans(entity.getUniqueID()) : ClanCache.getPlayerClans(((EntityTameable) entity).getOwnerId());
                        if (chunkClan != null && !ChunkUtils.isBorderland(c) && !entityClans.isEmpty() && entityClans.contains(chunkClan) && !RaidingParties.hasActiveRaid(chunkClan))
                            removeEntities.add(entity);
                    }
                }
                affectedEntities.removeAll(removeEntities);
            }
        }
    }

    public static boolean shouldCancelEntityDamage(Entity target, @Nullable Entity attacker) {
        if(!target.getEntityWorld().isRemote && Clans.getConfig().allowInjuryProtection()) {
            Chunk c = target.getEntityWorld().getChunk(target.getPosition());
            Clan chunkClan = ClanCache.getClanById(ChunkUtils.getChunkOwner(c));
            //Do not cancel if the attacker is in admin mode or the chunk is not a claim
            if(attacker instanceof EntityPlayerMP && ClanCache.isClaimAdmin((EntityPlayerMP) attacker) || chunkClan == null || ChunkUtils.isBorderland(c))
                return false;
            EntityPlayer attackingPlayer = attacker instanceof EntityPlayer ? (EntityPlayer) attacker : attacker instanceof EntityTameable && ((EntityTameable) attacker).getOwner() instanceof EntityPlayer ? (EntityPlayer) ((EntityTameable) attacker).getOwner() : null;
            //Players and their tameables fall into this first category. Including tameables ensures that wolves, Overlord Skeletons, etc are protected
            if(target instanceof EntityPlayer || (target instanceof EntityTameable && ((EntityTameable) target).getOwnerId() != null)) {
                UUID targetPlayerId = target instanceof EntityPlayer ? target.getUniqueID() : ((EntityTameable) target).getOwnerId();
                List<Clan> targetEntityClans = ClanCache.getPlayerClans(targetPlayerId);
                //Cancel if the target player/tameable is in its home territory, not being raided, and not getting hit by their own machines
                if (!targetEntityClans.isEmpty()
                        && targetEntityClans.contains(chunkClan)
                        && !RaidingParties.hasActiveRaid(chunkClan)
                        && targetPlayerId != null
                        && !Clans.getMinecraftHelper().isAllowedNonPlayerEntity(attacker, false))
                    return true;
                //Cancel if the attacker is not a raider or a raider's tameable
                else if(RaidingParties.hasActiveRaid(chunkClan)
                        && (attacker instanceof EntityPlayer || attacker instanceof EntityTameable && ((EntityTameable) attacker).getOwnerId() != null)
                        && !Clans.getMinecraftHelper().isAllowedNonPlayerEntity(attacker, false)) {
                    //Cycle through all the player's clans because we don't want a player to run and hide on a neighboring clan's territory to avoid damage
                    for (Clan targetEntityClan : targetEntityClans)
                        if (RaidingParties.isRaidedBy(targetEntityClan, attackingPlayer))
                            return false;
                    return true;
                }
            } else {//Target is not a player and not owned by a player
                if(attackingPlayer != null) {
                    UUID attackingPlayerId = attackingPlayer.getUniqueID();
                    List<Clan> attackerEntityClans = ClanCache.getPlayerClans(attackingPlayerId);
                    //Players can harm things in their own claims as long as they have permission
                    if (!attackerEntityClans.isEmpty()
                            && attackerEntityClans.contains(chunkClan))
                        return !hasPermissionToHarm(target, chunkClan, attackingPlayerId);
                    //Raiders can harm things when they are attacking
                    if (RaidingParties.isRaidedBy(chunkClan, attackingPlayer))
                        return false;
                    //Attacker is not a raider and not in the clan. Check if the clan has given them permission to harm whatever
                    return !hasPermissionToHarm(target, chunkClan, attackingPlayerId)
                            //Allow anyone to kill mobs on server clan land
                            && (!chunkClan.isServer() || !(target instanceof IMob))
                            && !Clans.getMinecraftHelper().isAllowedNonPlayerEntity(attacker, false);
                }
            }
        }
        return false;
    }

    public static boolean hasPermissionToHarm(Entity targetEntity, Clan permissionClan, UUID attackingPlayerId) {
        return targetEntity instanceof IMob
                ? permissionClan.hasPerm("harmmob", attackingPlayerId)
                : targetEntity instanceof IAnimals
                && permissionClan.hasPerm("harmanimal", attackingPlayerId);
    }

    public static boolean shouldCancelEntitySpawn(World world, Entity entity, BlockPos spawnPos) {
        ChunkPositionWithData spawnChunkPosition = new ChunkPositionWithData(world.getChunk(spawnPos)).retrieveCentralData();
        return Clans.getConfig().isPreventMobsOnClaims()
                && !world.isRemote
                && entity instanceof IMob
                && ClaimData.getChunkClan(spawnChunkPosition) != null
                && (Clans.getConfig().isPreventMobsOnBorderlands() || !spawnChunkPosition.isBorderland());
    }
}
