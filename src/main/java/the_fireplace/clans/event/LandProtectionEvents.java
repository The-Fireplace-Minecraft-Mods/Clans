package the_fireplace.clans.event;

import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import the_fireplace.clans.util.BlockSerializeUtil;
import the_fireplace.clans.util.ChunkUtils;
import the_fireplace.clans.Clans;
import the_fireplace.clans.util.MinecraftColors;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.raid.RaidRestoreDatabase;
import the_fireplace.clans.raid.RaidingParties;

import java.util.UUID;

@Mod.EventBusSubscriber(modid=Clans.MODID)
public class LandProtectionEvents {
	@SubscribeEvent
	public static void onBreakBlock(BlockEvent.BreakEvent event){
		Chunk c = event.getWorld().getChunk(event.getPos());
		UUID chunkOwner = ChunkUtils.getChunkOwner(c);
		if(chunkOwner != null) {
			Clan chunkClan = ClanCache.getClan(chunkOwner);
			if(chunkClan != null) {
				EntityPlayer breakingPlayer = event.getPlayer();
				if(breakingPlayer != null) {
					Clan playerClan = ClanCache.getPlayerClan(breakingPlayer.getUniqueID());
					boolean isRaided = RaidingParties.isRaidedBy(chunkClan, breakingPlayer);
					if((playerClan == null || !playerClan.getClanId().equals(chunkClan.getClanId())) && !isRaided) {
						event.setCanceled(true);
						breakingPlayer.sendMessage(new TextComponentString(MinecraftColors.RED + "You cannot break blocks in another clan's territory."));
					} else if(isRaided) {
						IBlockState targetState = event.getWorld().getBlockState(event.getPos());
						if(targetState.getBlock().hasTileEntity(targetState)) {
							event.setCanceled(true);
							breakingPlayer.sendMessage(new TextComponentString(MinecraftColors.RED + "You cannot break this block while in another clan's territory."));
						} else
							RaidRestoreDatabase.addBlock(c.getWorld().provider.getDimension(), c, event.getPos(), BlockSerializeUtil.blockToString(targetState));
					}
				}
			} else {
				//Remove the uuid as the chunk owner since the uuid is not associated with a clan.
				ChunkUtils.clearChunkOwner(c);
			}
		}
	}

	@SubscribeEvent
	public static void onBlockPlace(BlockEvent.PlaceEvent event) {
		Chunk c = event.getWorld().getChunk(event.getPos());
		UUID chunkOwner = ChunkUtils.getChunkOwner(c);
		if(chunkOwner != null) {
			Clan chunkClan = ClanCache.getClan(chunkOwner);
			if(chunkClan != null) {
				EntityPlayer placingPlayer = event.getPlayer();
				if(placingPlayer != null) {
					Clan playerClan = ClanCache.getPlayerClan(placingPlayer.getUniqueID());
					if(playerClan == null || !playerClan.getClanId().equals(chunkClan.getClanId())) {
						event.setCanceled(true);
						placingPlayer.sendMessage(new TextComponentString(MinecraftColors.RED + "You cannot place blocks in another clan's territory."));
					} else if(RaidingParties.isRaidedBy(chunkClan, placingPlayer)) {
						event.setCanceled(true);
						placingPlayer.sendMessage(new TextComponentString(MinecraftColors.RED + "You cannot place blocks in your territory while you are being raided."));
					}
				}
			} else {
				//Remove the uuid as the chunk owner since the uuid is not associated with a clan.
				ChunkUtils.clearChunkOwner(c);
			}
		}
	}

	@SubscribeEvent
	public static void onFluidPlaceBlock(BlockEvent.FluidPlaceBlockEvent event) {
		Chunk c = event.getWorld().getChunk(event.getPos());
		UUID chunkOwner = ChunkUtils.getChunkOwner(c);
		if(chunkOwner != null) {
			Chunk sourceChunk = event.getWorld().getChunk(event.getLiquidPos());
			UUID sourceChunkOwner = ChunkUtils.getChunkOwner(sourceChunk);
			if(!chunkOwner.equals(sourceChunkOwner))
				event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onPortalPlace(BlockEvent.PortalSpawnEvent event) {//TODO: Ensure that no part of the portal can enter a claimed chunk
		Chunk c = event.getWorld().getChunk(event.getPos());
		UUID chunkOwner = ChunkUtils.getChunkOwner(c);
		if(chunkOwner != null)
			event.setCanceled(true);
	}

	@SubscribeEvent
	public static void rightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		Chunk c = event.getWorld().getChunk(event.getPos());
		UUID chunkOwner = ChunkUtils.getChunkOwner(c);
		if(chunkOwner != null) {
			Clan chunkClan = ClanCache.getClan(chunkOwner);
			if(chunkClan != null) {
				EntityPlayer placingPlayer = event.getEntityPlayer();
				if(placingPlayer != null) {
					Clan playerClan = ClanCache.getPlayerClan(placingPlayer.getUniqueID());
					if((playerClan == null || !playerClan.getClanId().equals(chunkClan.getClanId())) && (!RaidingParties.isRaidedBy(chunkClan, placingPlayer) || !(event.getWorld().getBlockState(event.getPos()).getBlock() instanceof BlockDoor))) {
						event.setCanceled(true);
						placingPlayer.sendMessage(new TextComponentString(MinecraftColors.RED + "You cannot interact with blocks in another clan's territory."));
					}
				}
			} else {
				//Remove the uuid as the chunk owner since the uuid is not associated with a clan.
				ChunkUtils.clearChunkOwner(c);
			}
		}
	}
}
