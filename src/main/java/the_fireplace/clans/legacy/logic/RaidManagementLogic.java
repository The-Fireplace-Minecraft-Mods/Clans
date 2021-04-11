package the_fireplace.clans.legacy.logic;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.BlockSlime;
import net.minecraft.block.BlockTNT;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.clan.ClanIdRegistry;
import the_fireplace.clans.clan.membership.PlayerClans;
import the_fireplace.clans.legacy.ClansModContainer;
import the_fireplace.clans.legacy.api.ClaimAccessor;
import the_fireplace.clans.legacy.cache.RaidingParties;
import the_fireplace.clans.legacy.data.ChunkRestoreData;
import the_fireplace.clans.legacy.data.RaidCollectionDatabase;
import the_fireplace.clans.legacy.data.RaidRestoreDatabase;
import the_fireplace.clans.legacy.model.Raid;
import the_fireplace.clans.legacy.util.BlockSerializeUtil;
import the_fireplace.clans.legacy.util.ChunkUtils;
import the_fireplace.clans.legacy.util.MultiblockUtil;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;
import the_fireplace.clans.raid.PistonPhaseTracker;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class RaidManagementLogic {
    public static boolean shouldCancelBlockDrops(World world, BlockPos pos) {
        if(!world.isRemote && !ClansModContainer.getConfig().isDisableRaidRollback()) {
            Chunk c = world.getChunk(pos);
            UUID chunkOwner = ChunkUtils.getChunkOwner(c);
            if (chunkOwner != null) {
                if(ChunkUtils.isBorderland(c))
                    return false;
                if (ClanIdRegistry.isValidClan(chunkOwner)) {
                    return RaidingParties.hasActiveRaid(chunkOwner);
                } else {
                    //Remove the uuid as the chunk owner since the uuid is not associated with a clan.
                    ChunkUtils.clearChunkOwner(c);
                }
            }
        }
        return false;
    }

    public static void onPlayerDeath(EntityPlayerMP player, DamageSource source) {
        if(!player.getEntityWorld().isRemote) {
            for(UUID clan: PlayerClans.getClansPlayerIsIn(player.getUniqueID())) {
                if (clan != null && RaidingParties.hasActiveRaid(clan))
                    RaidingParties.getActiveRaid(clan).removeDefender(player.getUniqueID());
                if (RaidingParties.getRaidingPlayers().contains(player.getUniqueID()) && RaidingParties.getRaid(player).isActive())
                    RaidingParties.getRaid(player).removeAttacker(player.getUniqueID());
            }
        }
    }

    public static void checkAndRestoreChunk(Chunk chunk) {
        if(!chunk.getWorld().isRemote) {
            UUID chunkOwner = ChunkUtils.getChunkOwner(chunk);
            if (chunkOwner == null || !RaidingParties.hasActiveRaid(chunkOwner)) {
                ChunkRestoreData data = RaidRestoreDatabase.popChunkRestoreData(chunk.getWorld().provider.getDimension(), chunk);
                if (data != null)
                    data.restore(chunk);
            }
        }
    }

    public static void onNeighborBlockNotified(World world, IBlockState state, BlockPos pos) {
        if(!world.isRemote && !ClansModContainer.getConfig().isDisableRaidRollback()) {
            if (state.getBlock() instanceof BlockPistonBase) {
                if (state.getProperties().containsKey(BlockPistonBase.FACING) && state.getProperties().containsKey(BlockPistonBase.EXTENDED)) {
                    Comparable<?> facing = state.getProperties().get(BlockPistonBase.FACING);
                    Comparable<?> extended = state.getProperties().get(BlockPistonBase.EXTENDED);
                    BlockPos oldPos = pos;
                    BlockPos newPos = pos;
                    if(!PistonPhaseTracker.isTrackingPistonPhaseAt(pos))
                        PistonPhaseTracker.setPistonPhase(pos, !(Boolean) extended);

                    if (facing instanceof EnumFacing && extended instanceof Boolean && PistonPhaseTracker.getPistonPhase(pos) == extended) {
                        if ((Boolean) extended) {
                            int pushRange = 0;
                            for(int i=1;i<14;i++)
                                if(world.getBlockState(pos.offset((EnumFacing) facing, i)).getBlock() == Blocks.AIR || world.getBlockState(pos.offset((EnumFacing) facing, i)).getPushReaction().equals(EnumPushReaction.DESTROY)) {
                                    pushRange = i;
                                    break;
                                }
                            for(int i=pushRange-1;i>1;i--) {
                                newPos = pos.offset((EnumFacing) facing, i);
                                oldPos = pos.offset((EnumFacing) facing, i-1);
                                Chunk oldChunk = world.getChunk(oldPos);
                                Chunk newChunk = world.getChunk(newPos);
                                shiftBlocks(world, oldPos, newPos, oldChunk, newChunk);
                                TileEntity piston = world.getTileEntity(newPos);
                                if(piston instanceof TileEntityPiston && ((TileEntityPiston)piston).getPistonState().getBlock() instanceof BlockSlime) {
                                    switch((EnumFacing) facing) {
                                        case UP:
                                        case DOWN:
                                            for(EnumFacing shiftDir: Lists.newArrayList(EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.WEST))
                                                doSlimePush(world, (EnumFacing) facing, newPos, shiftDir);
                                            break;
                                        case EAST:
                                        case WEST:
                                            for(EnumFacing shiftDir: Lists.newArrayList(EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.UP, EnumFacing.DOWN))
                                                doSlimePush(world, (EnumFacing) facing, newPos, shiftDir);
                                            break;
                                        case NORTH:
                                        case SOUTH:
                                            for(EnumFacing shiftDir: Lists.newArrayList(EnumFacing.UP, EnumFacing.DOWN, EnumFacing.EAST, EnumFacing.WEST))
                                                doSlimePush(world, (EnumFacing) facing, newPos, shiftDir);
                                            break;
                                    }
                                }
                            }
                        } else if(state.getBlock().equals(Blocks.STICKY_PISTON)) {
                            oldPos = oldPos.offset((EnumFacing) facing, 2);
                            newPos = newPos.offset((EnumFacing) facing);
                            Chunk oldChunk = world.getChunk(oldPos);
                            Chunk newChunk = world.getChunk(newPos);
                            shiftBlocks(world, oldPos, newPos, oldChunk, newChunk);
                            if(world.getBlockState(newPos) instanceof BlockSlime) {
                                switch((EnumFacing) facing) {
                                    case UP:
                                    case DOWN:
                                        for(EnumFacing shiftDir: Lists.newArrayList(EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.WEST))
                                            doSlimePull(world, (EnumFacing) facing, newPos, shiftDir);
                                        break;
                                    case EAST:
                                    case WEST:
                                        for(EnumFacing shiftDir: Lists.newArrayList(EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.UP, EnumFacing.DOWN))
                                            doSlimePull(world, (EnumFacing) facing, newPos, shiftDir);
                                        break;
                                    case NORTH:
                                    case SOUTH:
                                        for(EnumFacing shiftDir: Lists.newArrayList(EnumFacing.UP, EnumFacing.DOWN, EnumFacing.EAST, EnumFacing.WEST))
                                            doSlimePull(world, (EnumFacing) facing, newPos, shiftDir);
                                        break;
                                }
                            }
                        }
                        PistonPhaseTracker.invertPistonPhase(pos);
                    }
                }
            }
        }
    }

    public static boolean shouldCancelFallingBlockCreation(EntityFallingBlock entity) {
        if(entity.world.isRemote)
            return false;
        UUID owningClan = ClaimAccessor.getInstance().getChunkClan(entity.chunkCoordX, entity.chunkCoordZ, entity.dimension);
        return owningClan != null && RaidingParties.hasActiveRaid(owningClan) && !ClaimAccessor.getInstance().getChunkPositionData(entity.chunkCoordX, entity.chunkCoordZ, entity.dimension).isBorderland() && !ClansModContainer.getConfig().isDisableRaidRollback();//TODO monitor where it goes rather than just preventing it from falling
    }

    private static void doSlimePush(World world, EnumFacing facing, BlockPos newPos, EnumFacing shiftDir) {
        BlockPos testPos = newPos.offset(shiftDir);
        BlockPos oldPos;
        if(world.getBlockState(testPos).getPushReaction().equals(EnumPushReaction.NORMAL) || world.getBlockState(testPos).getPushReaction().equals(EnumPushReaction.IGNORE))
            return;
        int pushRange2 = 0;
        for(int j=1;j<14;j++)
            if(world.getBlockState(testPos.offset(facing, j)).getBlock() == Blocks.AIR || world.getBlockState(testPos.offset(facing, j)).getPushReaction().equals(EnumPushReaction.DESTROY)) {
                pushRange2 = j;
                break;
            }
        for(int j=pushRange2;j>0;j--) {
            newPos = testPos.offset(facing, j - 1);
            oldPos = testPos.offset(facing, j - 2);
            Chunk oldChunk2 = world.getChunk(oldPos);
            Chunk newChunk2 = world.getChunk(newPos);
            shiftBlocks(world, oldPos, newPos, oldChunk2, newChunk2);
        }
    }

    private static void doSlimePull(World world, EnumFacing facing, BlockPos newPos, EnumFacing shiftDir) {
        BlockPos testPos = newPos.offset(shiftDir);
        BlockPos oldPos;
        if(world.getBlockState(testPos).getPushReaction().equals(EnumPushReaction.BLOCK) || world.getBlockState(testPos).getPushReaction().equals(EnumPushReaction.IGNORE) || world.getBlockState(testPos).getPushReaction().equals(EnumPushReaction.PUSH_ONLY))
            return;
        newPos = testPos.offset(facing);
        oldPos = testPos.offset(facing, 2);
        Chunk oldChunk2 = world.getChunk(oldPos);
        Chunk newChunk2 = world.getChunk(newPos);
        shiftBlocks(world, oldPos, newPos, oldChunk2, newChunk2);
    }

    private static void shiftBlocks(World world, BlockPos oldPos, BlockPos newPos, Chunk oldChunk, Chunk newChunk) {
        String oldBlock = RaidRestoreDatabase.popRestoreBlock(world.provider.getDimension(), oldChunk, oldPos);
        if (oldBlock != null)
            RaidRestoreDatabase.addRestoreBlock(world.provider.getDimension(), newChunk, newPos, oldBlock);
        if(RaidRestoreDatabase.delRemoveBlock(world.provider.getDimension(), oldChunk, oldPos))
            RaidRestoreDatabase.addRemoveBlock(world.provider.getDimension(), newChunk, newPos);
    }

    public static void checkAndRemoveForbiddenItems(MinecraftServer server, Raid raid) {
        Collection<String> itemList = ClansModContainer.getConfig().getRaidItemList();
        boolean isBlacklist = itemList.contains("*");
        //If everything is allowed, no need to go through this process
        if(itemList.size() == 1 && isBlacklist)
            return;
        List<UUID> raidingPlayers = Lists.newArrayList(raid.getAttackers());
        raidingPlayers.addAll(raid.getDefenders());
        for(UUID playerId: raidingPlayers) {
            EntityPlayer player = server.getPlayerList().getPlayerByUUID(playerId);
            //noinspection ConstantConditions
            if(player == null || player.inventory == null)
                continue;
            List<String> confiscated = Lists.newArrayList();
            for(int i=0;i<player.inventory.getSizeInventory();i++) {
                ItemStack stack = player.inventory.getStackInSlot(i);
                if(!stack.isEmpty() && isBlacklist == itemList.contains(Objects.requireNonNull(stack.getItem().getRegistryName()).toString())) {
                    ItemStack rm = player.inventory.removeStackFromSlot(i);
                    confiscated.add(rm.getDisplayName());
                    RaidCollectionDatabase.getInstance().addCollectItem(player.getUniqueID(), rm);
                }
            }
            if(!confiscated.isEmpty())
                for(String item: confiscated)
                    player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "clans.raid.confiscated", item));
        }
    }

    public static void onBlockBroken(World world, BlockPos pos, @Nullable IBlockState state) {
        if (!world.isRemote) {
            Chunk c = world.getChunk(pos);
            UUID chunkClan = ChunkUtils.getChunkOwner(c);
            if (chunkClan != null) {
                if (RaidingParties.hasActiveRaid(chunkClan) && !ClansModContainer.getConfig().isDisableRaidRollback()) {
                    IBlockState targetState = state != null ? state : world.getBlockState(pos);
                    RaidRestoreDatabase.addRestoreBlock(world.provider.getDimension(), c, pos, BlockSerializeUtil.blockToString(targetState));
                    for(BlockPos connected: MultiblockUtil.getDependentPositions(world, pos, targetState))
                        RaidRestoreDatabase.addRestoreBlock(world.provider.getDimension(), world.getChunk(connected), connected, BlockSerializeUtil.blockToString(world.getBlockState(connected)));
                }
            }
        }
    }

    public static void onBlockPlaced(World world, BlockPos pos, EntityPlayer placer, EntityEquipmentSlot hand, Block placedBlock) {
        if(!world.isRemote) {
            Chunk c = world.getChunk(pos);
            if (placer instanceof EntityPlayerMP) {
                UUID chunkClan = ChunkUtils.getChunkOwner(c);
                if (chunkClan != null) {
                    if (RaidingParties.hasActiveRaid(chunkClan) && !ClansModContainer.getConfig().isDisableRaidRollback()) {
                        ItemStack out = placer.getHeldItem(hand.getSlotType().equals(EntityEquipmentSlot.Type.HAND) && hand.equals(EntityEquipmentSlot.OFFHAND) ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND).copy();
                        out.setCount(1);
                        if (!ClansModContainer.getConfig().isNoReclaimTNT() || !(placedBlock instanceof BlockTNT))
                            RaidCollectionDatabase.getInstance().addCollectItem(placer.getUniqueID(), out);
                        RaidRestoreDatabase.addRemoveBlock(world.provider.getDimension(), c, pos);
                        //TODO After the block has been placed, something needs to check if a multiblock was created at that position and mark the dependent positions for removal as well.
                    }
                }
            }
        }
    }
}
