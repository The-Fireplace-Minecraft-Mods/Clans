package the_fireplace.clans.event;

import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import the_fireplace.clans.raid.ChunkRestoreData;
import the_fireplace.clans.raid.RaidRestoreDatabase;
import the_fireplace.clans.util.ChunkUtils;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.raid.RaidingParties;

import java.util.Objects;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Clans.MODID)
public class RaidEvents {
	@SubscribeEvent
	public static void onBlockDrops(BlockEvent.HarvestDropsEvent event) {
		if(!event.getWorld().isRemote) {
			Chunk c = event.getWorld().getChunk(event.getPos());
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
				Clan clan = ClanCache.getPlayerClan(player.getUniqueID());
				if (clan != null && RaidingParties.hasActiveRaid(clan))
					RaidingParties.getActiveRaid(clan).removeDefender(player);
				if (RaidingParties.getRaidingPlayers().contains(player) && RaidingParties.getRaid(player).isActive())
					RaidingParties.getRaid(player).removeMember(player);
			}
		}
	}

	@SubscribeEvent
	public static void onChunkLoaded(ChunkEvent.Load event) {
		if(!event.getWorld().isRemote) {
			if (!RaidingParties.hasActiveRaid(ClanCache.getClan(Objects.requireNonNull(event.getChunk().getCapability(Clans.CLAIMED_LAND, null)).getClan()))) {
				ChunkRestoreData data = RaidRestoreDatabase.popChunkRestoreData(event.getChunk().getWorld().provider.getDimension(), event.getChunk());
				if (data != null)
					data.restore(event.getChunk());
			}
		}
	}

	@SubscribeEvent
	public static void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
		if(!event.getWorld().isRemote) {
			IBlockState state = event.getState();
			if (state.getBlock() instanceof BlockPistonBase) {
				if (event.getState().getProperties().containsKey(BlockPistonBase.FACING)) {
					Comparable facing = event.getState().getProperties().get(BlockPistonBase.FACING);
					Comparable extended = event.getState().getProperties().get(BlockPistonBase.EXTENDED);
					BlockPos oldPos = event.getPos();
					BlockPos newPos = event.getPos();

					if (facing instanceof EnumFacing && extended instanceof Boolean) {
						int pushRange = 0;
						BlockPos testPos = event.getPos();
						for(int i=1;i<14;i++)
							if(event.getWorld().getBlockState(testPos.offset((EnumFacing) facing, i)).getBlock() == Blocks.AIR || event.getWorld().getBlockState(testPos.offset((EnumFacing) facing, i)).getPushReaction().equals(EnumPushReaction.DESTROY)) {
								pushRange = i;
								break;
							}
						//TODO handle blocks when pushing slime block
						if ((Boolean) extended) {
							for(int i=pushRange-1;i>1;i--) {
								newPos = event.getPos().offset((EnumFacing) facing, i);
								oldPos = event.getPos().offset((EnumFacing) facing, i-1);
								Chunk oldChunk = event.getWorld().getChunk(oldPos);
								Chunk newChunk = event.getWorld().getChunk(newPos);
								shiftBlocks(event, oldPos, newPos, oldChunk, newChunk);
							}
						} else if(state.getBlock().equals(Blocks.STICKY_PISTON)) {
							oldPos = oldPos.offset((EnumFacing) facing, 2);
							newPos = newPos.offset((EnumFacing) facing);
							Chunk oldChunk = event.getWorld().getChunk(oldPos);
							Chunk newChunk = event.getWorld().getChunk(newPos);
							shiftBlocks(event, oldPos, newPos, oldChunk, newChunk);
						}
					}
				}
			}
		}
	}

	private static void shiftBlocks(BlockEvent.NeighborNotifyEvent event, BlockPos oldPos, BlockPos newPos, Chunk oldChunk, Chunk newChunk) {
		String oldBlock = RaidRestoreDatabase.popRestoreBlock(event.getWorld().provider.getDimension(), oldChunk, oldPos);
		if (oldBlock != null)
			RaidRestoreDatabase.addRestoreBlock(event.getWorld().provider.getDimension(), newChunk, newPos, oldBlock);
		if(RaidRestoreDatabase.delRemoveBlock(event.getWorld().provider.getDimension(), oldChunk, oldPos))
			RaidRestoreDatabase.addRemoveBlock(event.getWorld().provider.getDimension(), newChunk, newPos);
	}
}
