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
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketEntityEquipment;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.Clans;
import the_fireplace.clans.ClansHelper;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.cache.RaidingParties;
import the_fireplace.clans.data.ClaimData;
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
import java.util.Objects;
import java.util.UUID;

/**
 * Logic for land protection events goes here.
 */
public class LandProtectionEventLogic {
    public static boolean shouldCancelBlockBroken(World world, BlockPos pos, @Nullable EntityPlayer breaker) {
        return shouldCancelBlockBroken(world, pos, breaker, true);
    }

    public static boolean shouldCancelBlockBroken(World world, BlockPos pos, @Nullable EntityPlayer breaker, boolean showMessage) {
        if(!world.isRemote && ClansHelper.getConfig().allowBuildProtection()) {
            Chunk c = world.getChunk(pos);
            Clan chunkClan = ChunkUtils.getChunkOwnerClan(c);
            if (chunkClan != null) {
                if (breaker instanceof EntityPlayerMP) {
                    boolean isRaided = RaidingParties.isRaidedBy(chunkClan, breaker);
                    IBlockState targetState = world.getBlockState(pos);
                    if(chunkClan.isLocked(pos) && !chunkClan.isLockOwner(pos, breaker.getUniqueID()) && !chunkClan.hasPerm("lockadmin", breaker.getUniqueID())) {
                        if(showMessage)
                            //noinspection ConstantConditions
                            breaker.sendMessage(TranslationUtil.getTranslation(breaker.getUniqueID(), "clans.protection.break.locked", world.getMinecraftServer().getPlayerProfileCache().getProfileByUUID(chunkClan.getLockOwner(pos)).getName()).setStyle(TextStyles.RED));
                        return true;
                    }
                    if (!ClanCache.isClaimAdmin((EntityPlayerMP) breaker)
                            && !chunkClan.hasPerm("build", breaker.getUniqueID())
                            && !isRaided
                            && !RaidingParties.preparingRaidOnBorderland(breaker, chunkClan, c)
                            && !Clans.getMinecraftHelper().isAllowedNonPlayerEntity(breaker, false)) {
                        if(showMessage)
                            breaker.sendMessage(TranslationUtil.getTranslation(breaker.getUniqueID(), ChunkUtils.isBorderland(c) ? "clans.protection.break.borderland" : "clans.protection.break.claimed").setStyle(TextStyles.RED));
                        return true;
                    } else if (isRaided && !ChunkUtils.isBorderland(c)) {
                        if (targetState.getBlock().hasTileEntity(targetState)) {
                            if(showMessage) {
                                if (ClanCache.isClaimAdmin((EntityPlayerMP) breaker))
                                    breaker.sendMessage(TranslationUtil.getTranslation(breaker.getUniqueID(), "clans.protection.break.raid").setStyle(TextStyles.RED));
                                else
                                    breaker.sendMessage(TranslationUtil.getTranslation(breaker.getUniqueID(), "clans.protection.break.claimed_raid").setStyle(TextStyles.RED));
                            }
                            return true;
                        }
                    } else if(chunkClan.isLocked(pos) && !chunkClan.hasLockAccess(pos, breaker.getUniqueID(), isContainer(world, pos, targetState, null) ? "access" : "build")) {
                        if(showMessage)
                            //noinspection ConstantConditions
                            breaker.sendMessage(TranslationUtil.getTranslation(breaker.getUniqueID(), "clans.protection.break.locked", world.getMinecraftServer().getPlayerProfileCache().getProfileByUUID(chunkClan.getLockOwner(pos)).getName()).setStyle(TextStyles.RED));
                        return true;
                    }
                }
                return false;
            }
            if (ClansHelper.getConfig().isProtectWilderness()
                    && (ClansHelper.getConfig().getMinWildernessY() < 0 ? pos.getY() >= world.getSeaLevel() : pos.getY() >= ClansHelper.getConfig().getMinWildernessY())
                    && !Clans.getMinecraftHelper().isAllowedNonPlayerEntity(breaker, false)
                    && (!(breaker instanceof EntityPlayerMP)
                        || (!ClanCache.isClaimAdmin((EntityPlayerMP) breaker)
                            && !PermissionManager.hasPermission((EntityPlayerMP)breaker, PermissionManager.PROTECTION_PREFIX+"break.protected_wilderness")))) {
                if(breaker != null && showMessage)
                    breaker.sendMessage(TranslationUtil.getTranslation(breaker.getUniqueID(), "clans.protection.break.wilderness").setStyle(TextStyles.RED));
                return true;
            }
        }
        return false;
    }

    public static boolean shouldCancelCropTrample(World world, BlockPos pos, @Nullable EntityPlayer breakingPlayer) {
        if(!world.isRemote && ClansHelper.getConfig().allowBuildProtection()) {
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

    public static boolean shouldCancelBlockPlacement(World world, BlockPos pos, @Nullable EntityPlayer placer, @Nullable EntityEquipmentSlot hand) {
        return shouldCancelBlockPlacement(world, pos, placer, hand, true);
    }

    public static boolean shouldCancelBlockPlacement(World world, BlockPos pos, @Nullable EntityPlayer placer, @Nullable EntityEquipmentSlot hand, boolean showMessage) {
        if(!world.isRemote && ClansHelper.getConfig().allowBuildProtection()) {
            Chunk c = world.getChunk(pos);
            if (placer instanceof EntityPlayerMP) {
                Clan chunkClan = ChunkUtils.getChunkOwnerClan(c);
                if (chunkClan != null) {
                    if (!ClanCache.isClaimAdmin((EntityPlayerMP) placer)
                            && !chunkClan.hasPerm("build", placer.getUniqueID())
                            && !RaidingParties.isRaidedBy(chunkClan, placer)
                            && !RaidingParties.preparingRaidOnBorderland(placer, chunkClan, c)
                            && !Clans.getMinecraftHelper().isAllowedNonPlayerEntity(placer, false)) {
                        //Notify the player that the block wasn't placed
                        if(((EntityPlayerMP) placer).connection != null && hand != null)
                            ((EntityPlayerMP) placer).connection.sendPacket(new SPacketEntityEquipment(placer.getEntityId(), hand, placer.getItemStackFromSlot(hand)));
                        if(showMessage)
                            placer.sendMessage(TranslationUtil.getTranslation(placer.getUniqueID(), ChunkUtils.isBorderland(c) ? "clans.protection.place.borderland" : "clans.protection.place.territory").setStyle(TextStyles.RED));
                        return true;
                    }
                    return false;
                }
                if (!ClanCache.isClaimAdmin((EntityPlayerMP) placer)
                        && (!PermissionManager.permissionManagementExists()
                            || !PermissionManager.hasPermission((EntityPlayerMP)placer, PermissionManager.PROTECTION_PREFIX+"build.protected_wilderness"))
                        && ClansHelper.getConfig().isProtectWilderness()
                        && (ClansHelper.getConfig().getMinWildernessY() < 0 ? pos.getY() >= world.getSeaLevel() : pos.getY() >= ClansHelper.getConfig().getMinWildernessY())
                        && !Clans.getMinecraftHelper().isAllowedNonPlayerEntity(placer, false)) {
                    if(((EntityPlayerMP) placer).connection != null && hand != null)
                        ((EntityPlayerMP) placer).connection.sendPacket(new SPacketEntityEquipment(placer.getEntityId(), hand, placer.getItemStackFromSlot(hand)));
                    placer.inventory.markDirty();
                    if(showMessage)
                        placer.sendMessage(TranslationUtil.getTranslation(placer.getUniqueID(), "clans.protection.place.wilderness").setStyle(TextStyles.RED));
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean shouldCancelFluidPlaceBlock(World world, BlockPos sourceLiquidPos, BlockPos fluidPlacingPos) {
        if(!world.isRemote && ClansHelper.getConfig().allowBuildProtection()) {
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

    public static boolean shouldCancelRightClickBlock(World world, BlockPos pos, EntityPlayer player, ItemStack heldItem, EnumHand hand) {
        if(!world.isRemote && ClansHelper.getConfig().allowInteractionProtection()) {
            Chunk c = world.getChunk(pos);
            Clan chunkClan = ChunkUtils.getChunkOwnerClan(c);
            if (chunkClan != null) {
                if (player instanceof EntityPlayerMP) {
                    IBlockState targetState = world.getBlockState(pos);
                    TileEntity targetTe = world.getTileEntity(pos);
                    boolean isContainer = isContainer(world, pos, targetState, targetTe);
                    boolean isRaidedBy = RaidingParties.isRaidedBy(chunkClan, player);
                    //Only bypass lock if there is an active raid, stealing is enabled, and the thief is either a raider or a member of the clan (It doesn't make sense to allow raiders to bypass the lock but not the clan members)
                    if(chunkClan.isLocked(pos) && (!RaidingParties.hasActiveRaid(chunkClan) || !ClansHelper.getConfig().isEnableStealing() || !(isRaidedBy || chunkClan.getMembers().containsKey(player.getUniqueID())))) {
                        if(!chunkClan.hasLockAccess(pos, player.getUniqueID(), isContainer ? "access" : "interact")) {
                            //noinspection ConstantConditions
                            player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "clans.protection.interact.locked", world.getMinecraftServer().getPlayerProfileCache().getProfileByUUID(chunkClan.getLockOwner(pos)).getName()).setStyle(TextStyles.RED));
                            notifyClientOfCancelledInteract(world, pos, player, targetState, hand);
                            return true;
                        } else
                            return false;
                    }
                    if (!ClanCache.isClaimAdmin((EntityPlayerMP) player)
                            && !chunkClan.hasPerm("interact", player.getUniqueID())
                            && !(isContainer && chunkClan.hasPerm("access", player.getUniqueID()))
                            && !RaidingParties.preparingRaidOnBorderland(player, chunkClan, c)
                            && (!isRaidedBy
                                || !ClansHelper.getConfig().isEnableStealing() && isContainer
                                || targetState.getBlock() instanceof BlockDragonEgg)) {
                        if (!(heldItem.getItem() instanceof ItemBlock)) {
                            cancelBlockInteraction(world, pos, player, targetState, hand);
                            return true;
                        } else if (!isRaidedBy
                                || !ClansHelper.getConfig().isEnableStealing() && isContainer
                                || targetState.getBlock() instanceof BlockDragonEgg) {
                            cancelBlockInteraction(world, pos, player, targetState, hand);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean isContainer(World world, BlockPos pos, @Nullable IBlockState state, @Nullable TileEntity tileEntity) {
        if(state == null)
            state = world.getBlockState(pos);
        if(tileEntity == null)
            tileEntity = world.getTileEntity(pos);
        return state.getBlock() instanceof BlockContainer || tileEntity instanceof IInventory || Clans.getProtectionCompat().isContainer(world, pos, state, tileEntity);
    }

    public static boolean shouldCancelMinecartInteract(EntityMinecart minecart, EntityPlayer player) {
        if(!minecart.world.isRemote && ClansHelper.getConfig().allowInteractionProtection()) {
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

    private static void cancelBlockInteraction(World world, BlockPos pos, EntityPlayer interactingPlayer, IBlockState targetState, EnumHand hand) {
        interactingPlayer.sendMessage(TranslationUtil.getTranslation(interactingPlayer.getUniqueID(), "clans.protection.interact.territory").setStyle(TextStyles.RED));
        notifyClientOfCancelledInteract(world, pos, interactingPlayer, targetState, hand);
    }

    private static void notifyClientOfCancelledInteract(World world, BlockPos pos, EntityPlayer interactingPlayer, IBlockState targetState, EnumHand hand) {
        //Update the client informing it the interaction did not happen. Go in all directions in case surrounding blocks would have been affected.
        world.notifyBlockUpdate(pos, targetState, targetState, 2);
        world.notifyBlockUpdate(pos.up(), targetState, targetState, 2);
        world.notifyBlockUpdate(pos.down(), targetState, targetState, 2);
        world.notifyBlockUpdate(pos.east(), targetState, targetState, 2);
        world.notifyBlockUpdate(pos.west(), targetState, targetState, 2);
        world.notifyBlockUpdate(pos.north(), targetState, targetState, 2);
        world.notifyBlockUpdate(pos.south(), targetState, targetState, 2);
        //Notify the client that the item hasn't been used.
        if(interactingPlayer instanceof EntityPlayerMP && ((EntityPlayerMP) interactingPlayer).connection != null)
            ((EntityPlayerMP) interactingPlayer).connection.sendPacket(new SPacketEntityEquipment(interactingPlayer.getEntityId(), Objects.equals(hand, EnumHand.OFF_HAND) ? EntityEquipmentSlot.OFFHAND : EntityEquipmentSlot.MAINHAND, interactingPlayer.getHeldItem(hand)));
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
                    if (RaidingParties.hasActiveRaid(chunkClan) && !ClansHelper.getConfig().isDisableRaidRollback() && !ChunkUtils.isBorderland(c) && !targetState.getBlock().hasTileEntity(targetState) && !(targetState.getBlock() instanceof BlockAir) && !(targetState.getBlock() instanceof BlockLiquid))
                        RaidRestoreDatabase.addRestoreBlock(c.getWorld().provider.getDimension(), c, pos, BlockSerializeUtil.blockToString(targetState));
                    else if (!ClansHelper.getConfig().isChainTNT() || !(targetState.getBlock() instanceof BlockTNT))
                        removeBlocks.add(pos);
                } else if (ClansHelper.getConfig().isProtectWilderness() && (ClansHelper.getConfig().getMinWildernessY() < 0 ? pos.getY() >= world.getSeaLevel() : pos.getY() >= ClansHelper.getConfig().getMinWildernessY()) && (!ClansHelper.getConfig().isChainTNT() || !(targetState.getBlock() instanceof BlockTNT)))
                    removeBlocks.add(pos);
            }
            if(ClansHelper.getConfig().allowBuildProtection())
                affectedBlocks.removeAll(removeBlocks);
            if(ClansHelper.getConfig().allowInjuryProtection()) {
                ArrayList<Entity> removeEntities = Lists.newArrayList();
                for (Entity entity : affectedEntities) {
                    if (entity instanceof EntityPlayer || (isOwnable(entity) && getOwnerId(entity) != null)) {
                        Chunk c = world.getChunk(entity.getPosition());
                        UUID chunkOwner = ChunkUtils.getChunkOwner(c);
                        Clan chunkClan = ClanCache.getClanById(chunkOwner);
                        List<Clan> entityClans = entity instanceof EntityPlayer ? ClanCache.getPlayerClans(entity.getUniqueID()) : ClanCache.getPlayerClans(getOwnerId(entity));
                        if (chunkClan != null && !ChunkUtils.isBorderland(c) && !entityClans.isEmpty() && entityClans.contains(chunkClan) && !RaidingParties.hasActiveRaid(chunkClan))
                            removeEntities.add(entity);
                    }
                }
                affectedEntities.removeAll(removeEntities);
            }
        }
    }

    /**
     * @param target
     * The target of the damage
     * @param source
     * The damage source should be provided if possible, try to avoid making this null because it can result in invinvibility
     * @param attacker
     * The attacker, if there is one
     * @return
     * If the damage should be cancelled
     */
    public static boolean shouldCancelEntityDamage(Entity target, @Nullable DamageSource source, @Nullable Entity attacker) {
        if(!target.getEntityWorld().isRemote && ClansHelper.getConfig().allowInjuryProtection()) {
            Chunk c = target.getEntityWorld().getChunk(target.getPosition());
            Clan chunkClan = ClanCache.getClanById(ChunkUtils.getChunkOwner(c));
            if(attacker == null && source != null)
                attacker = source.getTrueSource();
            //Do not cancel if the attacker is in admin mode or the chunk is not a claim
            if(attacker instanceof EntityPlayerMP && ClanCache.isClaimAdmin((EntityPlayerMP) attacker) || chunkClan == null || ChunkUtils.isBorderland(c))
                return false;
            if(!chunkClan.isMobDamageAllowed() && isMob(attacker))
                return true;
            //Do not cancel if it would be able to harm in creative or doesn't come from being attacked
            if(source != null && (source.canHarmInCreative() || (attacker == null && !source.isExplosion())))
                return false;
            EntityPlayer attackingPlayer = attacker instanceof EntityPlayer ? (EntityPlayer) attacker : isOwnable(attacker) && getOwner(attacker) instanceof EntityPlayer ? (EntityPlayer) getOwner(attacker) : null;
            //Players and their tameables fall into this first category. Including tameables ensures that wolves, Overlord Skeletons, etc are protected
            if(target instanceof EntityPlayer || (isOwnable(target) && getOwnerId(target) != null)) {
                Boolean pvpAllowed = chunkClan.pvpAllowed();
                if(pvpAllowed == null)
                    return shouldCancelPVPDefault(target, attacker, chunkClan, attackingPlayer);
                else //Cancel if pvp is not allowed, don't cancel if pvp is allowed
                    return !pvpAllowed;
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
                            && (!chunkClan.isServer() || !(isMob(target)))
                            && !Clans.getMinecraftHelper().isAllowedNonPlayerEntity(attacker, false);
                }
            }
        }
        return false;
    }

    private static boolean shouldCancelPVPDefault(Entity target, @Nullable Entity attacker, Clan chunkClan, @Nullable EntityPlayer attackingPlayer) {
        UUID targetPlayerId = target instanceof EntityPlayer ? target.getUniqueID() : getOwnerId(target);
        List<Clan> targetEntityClans = ClanCache.getPlayerClans(targetPlayerId);
        //Cancel if the target player/tameable is in its home territory, not being raided, and not getting hit by their own machines
        if (!targetEntityClans.isEmpty()
                && targetEntityClans.contains(chunkClan)
                && !RaidingParties.hasActiveRaid(chunkClan)
                && targetPlayerId != null
                && !Clans.getMinecraftHelper().isAllowedNonPlayerEntity(attacker, false))
            return true;
        //The attacker is a player (or is owned by a player) and is in a chunk that is being raided and is not an allowed fake player
        else if(RaidingParties.hasActiveRaid(chunkClan)
                && attackingPlayer != null
                && !Clans.getMinecraftHelper().isAllowedNonPlayerEntity(attacker, false)) {
            //Do not cancel if the attacker is in their home territory.
            if(ClanCache.getPlayerClans(attackingPlayer.getUniqueID()).contains(chunkClan))
                return false;
            //Cancel if the attacker is not a raider or a raider's tameable
            //Cycle through all the player's clans because we don't want a player to run and hide on a neighboring clan's territory to avoid damage
            for (Clan targetEntityClan : targetEntityClans)
                if (RaidingParties.isRaidedBy(targetEntityClan, attackingPlayer))
                    return false;
            return true;
        }
        return false;
    }

    @Nullable
    public static UUID getOwnerId(@Nullable Entity entity) {
        if(entity instanceof EntityTameable)
            return ((EntityTameable) entity).getOwnerId();
        else if(entity != null)
            return Clans.getProtectionCompat().getOwnerId(entity);
        return null;
    }

    @Nullable
    public static Entity getOwner(@Nullable Entity entity) {
        if(entity instanceof EntityTameable)
            return ((EntityTameable) entity).getOwner();
        else if(entity != null)
            try
            {
                UUID uuid = getOwnerId(entity);
                //Typically the entity will be a player but at least make an attempt to find other entities that may be the owner just in case (mods can do some crazy things)
                return uuid == null ? null : (entity.world instanceof WorldServer ? ((WorldServer) entity.world).getEntityFromUuid(uuid) : entity.world.getPlayerEntityByUUID(uuid));
            }
            catch (IllegalArgumentException ignored) {}
        return null;
    }

    public static boolean isOwnable(@Nullable Entity entity) {
        return entity instanceof EntityTameable || (entity != null && Clans.getProtectionCompat().isOwnable(entity));
    }

    public static boolean isMob(@Nullable Entity entity) {
        return entity instanceof IMob || (entity != null && Clans.getProtectionCompat().isMob(entity));
    }

    public static boolean hasPermissionToHarm(Entity targetEntity, Clan permissionClan, UUID attackingPlayerId) {
        return isMob(targetEntity)
                ? permissionClan.hasPerm("harmmob", attackingPlayerId)
                : targetEntity instanceof IAnimals
                && permissionClan.hasPerm("harmanimal", attackingPlayerId);
    }

    public static boolean shouldCancelEntitySpawn(World world, Entity entity, BlockPos spawnPos) {
        ChunkPositionWithData spawnChunkPosition = new ChunkPositionWithData(world.getChunk(spawnPos)).retrieveCentralData();
        Clan c = ClaimData.getChunkClan(spawnChunkPosition);
        return !world.isRemote
                && isMob(entity)
                && c != null
                && (ClansHelper.getConfig().isPreventMobsOnClaims() || Boolean.TRUE.equals(c.getMobSpawnOverride()))
                && (ClansHelper.getConfig().isPreventMobsOnBorderlands() || !spawnChunkPosition.isBorderland() || Boolean.TRUE.equals(c.getMobSpawnOverride()));
    }
}
