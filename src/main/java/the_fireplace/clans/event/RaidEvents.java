package the_fireplace.clans.event;

import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
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
		Chunk c = event.getWorld().getChunk(event.getPos());
		UUID chunkOwner = ChunkUtils.getChunkOwner(c);
		if(chunkOwner != null) {
			Clan chunkClan = ClanCache.getClan(chunkOwner);
			if(chunkClan != null) {
				if(RaidingParties.hasActiveRaid(chunkClan)) {
					//Triple check that nothing gets dropped during a raid, to avoid block duping.
					event.setCanceled(true);
					event.getDrops().clear();
					event.setDropChance(0.0f);
				}
			} else {
				//Remove the uuid as the chunk owner since the uuid is not associated with a clan.
				ChunkUtils.clearChunkOwner(c);
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerDeath(LivingDeathEvent event) {
		if(event.getEntityLiving() instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP) event.getEntityLiving();
			Clan clan = ClanCache.getPlayerClan(player.getUniqueID());
			if(clan != null && RaidingParties.hasActiveRaid(clan))
				RaidingParties.getActiveRaid(clan).removeDefender(player);
			if(RaidingParties.getRaidingPlayers().contains(player) && RaidingParties.getRaid(player).isActive())
				RaidingParties.getRaid(player).removeMember(player);
		}
	}

	@SubscribeEvent
	public static void onChunkLoaded(ChunkEvent.Load event) {
		if(!RaidingParties.hasActiveRaid(ClanCache.getClan(Objects.requireNonNull(event.getChunk().getCapability(Clans.CLAIMED_LAND, null)).getClan()))) {
			ChunkRestoreData data = RaidRestoreDatabase.popChunkRestoreData(event.getChunk().getWorld().provider.getDimension(), event.getChunk());
			if(data != null)
				data.restore(event.getChunk());
		}
	}

	@SubscribeEvent
	public static void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
		IBlockState state = event.getState();
		if(state.getBlock() instanceof BlockPistonBase) {
			if(event.getState().getProperties().containsKey(BlockPistonBase.FACING)) {
				Comparable facing = event.getState().getProperties().get(BlockPistonBase.FACING);
				Comparable extended = event.getState().getProperties().get(BlockPistonBase.EXTENDED);
				BlockPos oldPos = event.getPos();
				BlockPos newPos = event.getPos();

				if(facing instanceof EnumFacing && extended instanceof Boolean) {
					switch ((EnumFacing) facing) {
						case NORTH://TODO handle blocks when pushing slime block
							if ((Boolean) extended) {//TODO extend the reach further out when extended because it pushes more than one block
								newPos = newPos.north(2);
								oldPos = oldPos.north();
							} else {//TODO make sure this only gets called if the piston is sticky
								oldPos = oldPos.north(2);
								newPos = newPos.north();
							}
							break;
						case SOUTH:
							if ((Boolean) extended) {
								newPos = newPos.south(2);
								oldPos = oldPos.south();
							} else {
								oldPos = oldPos.south(2);
								newPos = newPos.south();
							}
							break;
						case EAST:
							if ((Boolean) extended) {
								newPos = newPos.east(2);
								oldPos = oldPos.east();
							} else {
								oldPos = oldPos.east(2);
								newPos = newPos.east();
							}
							break;
						case WEST:
							if ((Boolean) extended) {
								newPos = newPos.west(2);
								oldPos = oldPos.west();
							} else {
								oldPos = oldPos.west(2);
								newPos = newPos.west();
							}
							break;
						case UP:
							if ((Boolean) extended) {
								newPos = newPos.up(2);
								oldPos = oldPos.up();
							} else {
								oldPos = oldPos.up(2);
								newPos = newPos.up();
							}
							break;
						case DOWN:
							if ((Boolean) extended) {
								newPos = newPos.down(2);
								oldPos = oldPos.down();
							} else {
								oldPos = oldPos.down(2);
								newPos = newPos.down();
							}
							break;
					}
					Chunk oldChunk = event.getWorld().getChunk(oldPos);
					Chunk newChunk = event.getWorld().getChunk(newPos);
					String oldBlock = RaidRestoreDatabase.popBlock(event.getWorld().provider.getDimension(), oldChunk, oldPos);
					if(oldBlock != null)
						RaidRestoreDatabase.addBlock(event.getWorld().provider.getDimension(), newChunk, newPos, oldBlock);
				}
			}
		}
	}
}
