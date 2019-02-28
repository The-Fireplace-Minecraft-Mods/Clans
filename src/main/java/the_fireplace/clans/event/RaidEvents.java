package the_fireplace.clans.event;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.BlockSlime;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.raid.ChunkRestoreData;
import the_fireplace.clans.raid.RaidRestoreDatabase;
import the_fireplace.clans.raid.RaidingParties;
import the_fireplace.clans.util.ChunkUtils;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Clans.MODID)
public class RaidEvents {
	@SubscribeEvent
	public static void onBlockDrops(BlockEvent.HarvestDropsEvent event) {
		if(!event.getWorld().isRemote()) {
			IChunk c = event.getWorld().getChunkDefault(event.getPos());
			UUID chunkOwner = ChunkUtils.getChunkOwner(c);
			if (chunkOwner != null) {
				Clan chunkClan = ClanCache.getClan(chunkOwner);
				if (chunkClan != null) {
					if (RaidingParties.hasActiveRaid(chunkClan)) {
						//Double check that nothing gets dropped during a raid, to avoid block duping.
						event.getDrops().clear();
						event.setDropChance(0.0f);
					}
				} else {
					//Remove the uuid as the chunk owner since the uuid is not associated with a clan.
					ChunkUtils.clearChunkOwner(c);
				}
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerDeath(LivingDeathEvent event) {
		if(!event.getEntity().getEntityWorld().isRemote) {
			if (event.getEntityLiving() instanceof EntityPlayerMP) {
				EntityPlayerMP player = (EntityPlayerMP) event.getEntityLiving();
				for(Clan clan: ClanCache.getPlayerClans(player.getUniqueID())) {
					if (clan != null && RaidingParties.hasActiveRaid(clan))
						RaidingParties.getActiveRaid(clan).removeDefender(player);
					if (RaidingParties.getRaidingPlayers().contains(player) && RaidingParties.getRaid(player).isActive())
						RaidingParties.getRaid(player).removeMember(player);
				}
			}
		}
	}

	@SubscribeEvent
	public static void onChunkLoaded(ChunkEvent.Load event) {
		if(!event.getWorld().isRemote()) {
			if (!RaidingParties.hasActiveRaid(ClanCache.getClan(ChunkUtils.getChunkOwner(event.getChunk())))) {
				ChunkRestoreData data = RaidRestoreDatabase.popChunkRestoreData(Objects.requireNonNull(event.getChunk().getWorldForge()).getDimension().getType().getId(), event.getChunk());
				if (data != null)
					data.restore(event.getChunk());
			}
		}
	}

	private static HashMap<BlockPos, Boolean> phases = Maps.newHashMap();

	@SubscribeEvent
	public static void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
		if(!event.getWorld().isRemote()) {
			IBlockState state = event.getState();
			if (state.getBlock() instanceof BlockPistonBase) {
				if (event.getState().has(BlockPistonBase.FACING) && event.getState().has(BlockPistonBase.EXTENDED)) {
					Comparable facing = event.getState().get(BlockPistonBase.FACING);
					Comparable extended = event.getState().get(BlockPistonBase.EXTENDED);
					BlockPos oldPos = event.getPos();
					BlockPos newPos = event.getPos();
					if(!phases.containsKey(event.getPos()))
						phases.put(event.getPos(), !(Boolean) extended);

					if (phases.get(event.getPos()) == extended) {
						if ((Boolean) extended) {
							int pushRange = 0;
							BlockPos testPos = event.getPos();
							for(int i=1;i<14;i++)
								if(event.getWorld().getBlockState(testPos.offset((EnumFacing) facing, i)).getBlock() == Blocks.AIR || event.getWorld().getBlockState(testPos.offset((EnumFacing) facing, i)).getPushReaction().equals(EnumPushReaction.DESTROY)) {
									pushRange = i;
									break;
								}
							for(int i=pushRange-1;i>1;i--) {
								newPos = event.getPos().offset((EnumFacing) facing, i);
								oldPos = event.getPos().offset((EnumFacing) facing, i-1);
								IChunk oldChunk = event.getWorld().getChunkDefault(oldPos);
								IChunk newChunk = event.getWorld().getChunkDefault(newPos);
								shiftBlocks(event, oldPos, newPos, oldChunk, newChunk);
								TileEntity piston = event.getWorld().getTileEntity(newPos);
								if(piston instanceof TileEntityPiston && ((TileEntityPiston)piston).getPistonState().getBlock() instanceof BlockSlime) {
									switch((EnumFacing) facing) {
										case UP:
										case DOWN:
											for(EnumFacing shiftDir: Lists.newArrayList(EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.WEST))
												doSlimePush(event, (EnumFacing) facing, newPos, shiftDir);
											break;
										case EAST:
										case WEST:
											for(EnumFacing shiftDir: Lists.newArrayList(EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.UP, EnumFacing.DOWN))
												doSlimePush(event, (EnumFacing) facing, newPos, shiftDir);
											break;
										case NORTH:
										case SOUTH:
											for(EnumFacing shiftDir: Lists.newArrayList(EnumFacing.UP, EnumFacing.DOWN, EnumFacing.EAST, EnumFacing.WEST))
												doSlimePush(event, (EnumFacing) facing, newPos, shiftDir);
											break;
									}
								}
							}
						} else if(state.getBlock().equals(Blocks.STICKY_PISTON)) {
							oldPos = oldPos.offset((EnumFacing) facing, 2);
							newPos = newPos.offset((EnumFacing) facing);
							IChunk oldChunk = event.getWorld().getChunkDefault(oldPos);
							IChunk newChunk = event.getWorld().getChunkDefault(newPos);
							shiftBlocks(event, oldPos, newPos, oldChunk, newChunk);
							if(event.getWorld().getBlockState(newPos) instanceof BlockSlime) {
								switch((EnumFacing) facing) {
									case UP:
									case DOWN:
										for(EnumFacing shiftDir: Lists.newArrayList(EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.WEST))
											doSlimePull(event, (EnumFacing) facing, newPos, shiftDir);
										break;
									case EAST:
									case WEST:
										for(EnumFacing shiftDir: Lists.newArrayList(EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.UP, EnumFacing.DOWN))
											doSlimePull(event, (EnumFacing) facing, newPos, shiftDir);
										break;
									case NORTH:
									case SOUTH:
										for(EnumFacing shiftDir: Lists.newArrayList(EnumFacing.UP, EnumFacing.DOWN, EnumFacing.EAST, EnumFacing.WEST))
											doSlimePull(event, (EnumFacing) facing, newPos, shiftDir);
										break;
								}
							}
						}
						phases.put(event.getPos(), !phases.get(event.getPos()));
					}
				}
			}
		}
	}

	private static void doSlimePush(BlockEvent.NeighborNotifyEvent event, EnumFacing facing, BlockPos newPos, EnumFacing shiftDir) {
		BlockPos testPos = newPos.offset(shiftDir);
		BlockPos oldPos;
		if(event.getWorld().getBlockState(testPos).getPushReaction().equals(EnumPushReaction.NORMAL) || event.getWorld().getBlockState(testPos).getPushReaction().equals(EnumPushReaction.IGNORE))
			return;
		int pushRange2 = 0;
		for(int j=1;j<14;j++)
			if(event.getWorld().getBlockState(testPos.offset(facing, j)).getBlock() == Blocks.AIR || event.getWorld().getBlockState(testPos.offset(facing, j)).getPushReaction().equals(EnumPushReaction.DESTROY)) {
				pushRange2 = j;
				break;
			}
		for(int j=pushRange2;j>0;j--) {
			newPos = testPos.offset(facing, j - 1);
			oldPos = testPos.offset(facing, j - 2);
			IChunk oldChunk2 = event.getWorld().getChunkDefault(oldPos);
			IChunk newChunk2 = event.getWorld().getChunkDefault(newPos);
			shiftBlocks(event, oldPos, newPos, oldChunk2, newChunk2);
		}
	}

	private static void doSlimePull(BlockEvent.NeighborNotifyEvent event, EnumFacing facing, BlockPos newPos, EnumFacing shiftDir) {
		BlockPos testPos = newPos.offset(shiftDir);
		BlockPos oldPos;
		if(event.getWorld().getBlockState(testPos).getPushReaction().equals(EnumPushReaction.BLOCK) || event.getWorld().getBlockState(testPos).getPushReaction().equals(EnumPushReaction.IGNORE) || event.getWorld().getBlockState(testPos).getPushReaction().equals(EnumPushReaction.PUSH_ONLY))
			return;
		newPos = testPos.offset(facing);
		oldPos = testPos.offset(facing, 2);
		IChunk oldChunk = event.getWorld().getChunkDefault(oldPos);
		IChunk newChunk = event.getWorld().getChunkDefault(newPos);
		shiftBlocks(event, oldPos, newPos, oldChunk, newChunk);
	}

	private static void shiftBlocks(BlockEvent.NeighborNotifyEvent event, BlockPos oldPos, BlockPos newPos, IChunk oldChunk, IChunk newChunk) {
		String oldBlock = RaidRestoreDatabase.popRestoreBlock(event.getWorld().getDimension().getType().getId(), oldChunk, oldPos);
		if (oldBlock != null)
			RaidRestoreDatabase.addRestoreBlock(event.getWorld().getDimension().getType().getId(), newChunk, newPos, oldBlock);
		if(RaidRestoreDatabase.delRemoveBlock(event.getWorld().getDimension().getType().getId(), oldChunk, oldPos))
			RaidRestoreDatabase.addRemoveBlock(event.getWorld().getDimension().getType().getId(), newChunk, newPos);
	}
}
