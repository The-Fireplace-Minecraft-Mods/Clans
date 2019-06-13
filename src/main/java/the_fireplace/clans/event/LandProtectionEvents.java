package the_fireplace.clans.event;

import com.google.common.collect.Lists;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketEntityEquipment;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.NewClan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.raid.NewRaidBlockPlacementDatabase;
import the_fireplace.clans.raid.NewRaidRestoreDatabase;
import the_fireplace.clans.raid.RaidingParties;
import the_fireplace.clans.util.BlockSerializeUtil;
import the_fireplace.clans.util.ChunkUtils;
import the_fireplace.clans.util.TextStyles;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class LandProtectionEvents {
	@SubscribeEvent
	public void onBreakBlock(BlockEvent.BreakEvent event){
		if(!event.getWorld().isRemote()) {
			IChunk c = event.getWorld().getChunkDefault(event.getPos());
			UUID chunkOwner = ChunkUtils.getChunkOwner(c);
			if (chunkOwner != null) {
				NewClan chunkClan = ClanCache.getClanById(chunkOwner);
				if (chunkClan != null) {
					EntityPlayer breakingPlayer = event.getPlayer();
					if (breakingPlayer instanceof EntityPlayerMP) {
						ArrayList<NewClan> playerClans = ClanCache.getClansByPlayer(breakingPlayer.getUniqueID());
						boolean isRaided = RaidingParties.isRaidedBy(chunkClan, breakingPlayer.getUniqueID());
						if (!ClanCache.isClaimAdmin(breakingPlayer.getUniqueID()) && (playerClans.isEmpty() || !playerClans.contains(chunkClan)) && !isRaided) {
							event.setCanceled(true);
							breakingPlayer.sendMessage(new TextComponentString("You cannot break blocks in another clan's territory.").setStyle(TextStyles.RED));
						} else if (isRaided) {
							IBlockState targetState = event.getWorld().getBlockState(event.getPos());
							if (targetState.getBlock().hasTileEntity(targetState)) {
								event.setCanceled(true);
								if(ClanCache.isClaimAdmin(breakingPlayer.getUniqueID()))
									breakingPlayer.sendMessage(new TextComponentString("You cannot break this block during a raid. Please wait until the raid is completed and try again.").setStyle(TextStyles.RED));
								else
									breakingPlayer.sendMessage(new TextComponentString("You cannot break this block while in another clan's territory.").setStyle(TextStyles.RED));
							} else
								NewRaidRestoreDatabase.addRestoreBlock(Objects.requireNonNull(c.getWorldForge()).getDimension().getType().getId(), c, event.getPos(), BlockSerializeUtil.blockToString(targetState), chunkOwner);
						}
					}
					return;
				} else {
					//Remove the uuid as the chunk owner since the uuid is not associated with a clan.
					ChunkUtils.clearChunkOwner(c);
				}
			}
			if (Clans.cfg.protectWilderness && (Clans.cfg.minWildernessY < 0 ? event.getPos().getY() >= event.getWorld().getSeaLevel() : event.getPos().getY() >= Clans.cfg.minWildernessY) && (!(event.getPlayer() instanceof EntityPlayerMP) || !ClanCache.isClaimAdmin(event.getPlayer().getUniqueID()))) {
				event.setCanceled(true);
				event.getPlayer().sendMessage(new TextComponentString("You cannot break blocks in Wilderness.").setStyle(TextStyles.RED));
			}
		}
	}

	@SubscribeEvent
	public void onCropTrample(BlockEvent.FarmlandTrampleEvent event){
		if(!event.getWorld().isRemote()) {
			IChunk c = event.getWorld().getChunkDefault(event.getPos());
			UUID chunkOwner = ChunkUtils.getChunkOwner(c);
			if (chunkOwner != null) {
				NewClan chunkClan = ClanCache.getClanById(chunkOwner);
				if (chunkClan != null) {
					EntityPlayer breakingPlayer = event.getEntity() instanceof EntityPlayer ? (EntityPlayer) event.getEntity() : null;
					if (breakingPlayer != null) {
						ArrayList<NewClan> playerClans = ClanCache.getClansByPlayer(breakingPlayer.getUniqueID());
						boolean isRaided = RaidingParties.isRaidedBy(chunkClan, breakingPlayer.getUniqueID());
						if ((playerClans.isEmpty() || !playerClans.contains(chunkClan)) && !isRaided)
							event.setCanceled(true);
					}
				} else {
					//Remove the uuid as the chunk owner since the uuid is not associated with a clan.
					ChunkUtils.clearChunkOwner(c);
				}
			}
		}
	}

	@SuppressWarnings("Duplicates")
	@SubscribeEvent
	public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
		if(!event.getWorld().isRemote()) {
			IChunk c = event.getWorld().getChunkDefault(event.getPos());
			UUID chunkOwner = ChunkUtils.getChunkOwner(c);
			Entity placingPlayer = event.getEntity();
			if (placingPlayer instanceof EntityPlayerMP) {
				if (chunkOwner != null) {
					NewClan chunkClan = ClanCache.getClanById(chunkOwner);
					if (chunkClan != null) {
						ArrayList<NewClan> playerClans = ClanCache.getClansByPlayer(placingPlayer.getUniqueID());
						if (!ClanCache.isClaimAdmin(placingPlayer.getUniqueID()) && (playerClans.isEmpty() || (!playerClans.contains(chunkClan) && !RaidingParties.isRaidedBy(chunkClan, placingPlayer.getUniqueID())))) {
							event.setCanceled(true);
							EntityEquipmentSlot hand = ((EntityPlayerMP)event.getEntity()).getActiveHand().equals(EnumHand.MAIN_HAND) ? EntityEquipmentSlot.MAINHAND : EntityEquipmentSlot.OFFHAND;
							if(((EntityPlayerMP) placingPlayer).connection != null)
								((EntityPlayerMP) placingPlayer).connection.sendPacket(new SPacketEntityEquipment(placingPlayer.getEntityId(), hand, ((EntityPlayerMP)event.getEntity()).getItemStackFromSlot(hand)));
							placingPlayer.sendMessage(new TextComponentString("You cannot place blocks in another clan's territory.").setStyle(TextStyles.RED));
						} else if (RaidingParties.hasActiveRaid(chunkClan)) {
							ItemStack out = ((EntityPlayerMP)event.getEntity()).getHeldItem(((EntityPlayerMP)event.getEntity()).getActiveHand()).copy();
							out.setCount(1);
							NewRaidBlockPlacementDatabase.getInstance().addPlacedBlock(placingPlayer.getUniqueID(), out);
							NewRaidRestoreDatabase.addRemoveBlock(event.getWorld().getDimension().getType().getId(), c, event.getPos());
						}
					}
					return;
				} else {
					//Remove the uuid as the chunk owner since the uuid is not associated with a clan.
					ChunkUtils.clearChunkOwner(c);
				}
				if (!ClanCache.isClaimAdmin(placingPlayer.getUniqueID()) && Clans.cfg.protectWilderness && (Clans.cfg.minWildernessY < 0 ? event.getPos().getY() >= event.getWorld().getSeaLevel() : event.getPos().getY() >= Clans.cfg.minWildernessY)) {
					event.setCanceled(true);
					EntityEquipmentSlot hand = ((EntityPlayerMP)event.getEntity()).getActiveHand().equals(EnumHand.MAIN_HAND) ? EntityEquipmentSlot.MAINHAND : EntityEquipmentSlot.OFFHAND;
					if(((EntityPlayerMP) placingPlayer).connection != null)
						((EntityPlayerMP) placingPlayer).connection.sendPacket(new SPacketEntityEquipment(placingPlayer.getEntityId(), hand, ((EntityPlayerMP)event.getEntity()).getItemStackFromSlot(hand)));
					((EntityPlayerMP)event.getEntity()).inventory.markDirty();
					event.getEntity().sendMessage(new TextComponentString("You cannot place blocks in Wilderness.").setStyle(TextStyles.RED));
				}
			}
		}
	}

	@SubscribeEvent
	public void onFluidPlaceBlock(BlockEvent.FluidPlaceBlockEvent event) {
		if(!event.getWorld().isRemote()) {
			IChunk c = event.getWorld().getChunkDefault(event.getPos());
			UUID chunkOwner = ChunkUtils.getChunkOwner(c);
			if (chunkOwner != null) {
				IChunk sourceChunk = event.getWorld().getChunkDefault(event.getLiquidPos());
				UUID sourceChunkOwner = ChunkUtils.getChunkOwner(sourceChunk);
				if (!chunkOwner.equals(sourceChunkOwner))
					event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public void rightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		if(!event.getWorld().isRemote) {
			Chunk c = event.getWorld().getChunk(event.getPos());
			UUID chunkOwner = ChunkUtils.getChunkOwner(c);
			if (chunkOwner != null) {
				NewClan chunkClan = ClanCache.getClanById(chunkOwner);
				if (chunkClan != null) {
					EntityPlayer interactingPlayer = event.getEntityPlayer();
					if (interactingPlayer instanceof EntityPlayerMP) {
						ArrayList<NewClan> playerClan = ClanCache.getClansByPlayer(interactingPlayer.getUniqueID());
						IBlockState targetState = event.getWorld().getBlockState(event.getPos());
						if (!ClanCache.isClaimAdmin(interactingPlayer.getUniqueID()) && (playerClan.isEmpty() || !playerClan.contains(chunkClan)) && (!RaidingParties.isRaidedBy(chunkClan, interactingPlayer.getUniqueID()) || targetState.getBlock() instanceof BlockContainer || targetState.getBlock() instanceof BlockDragonEgg)) {
							if (!(event.getItemStack().getItem() instanceof ItemBlock))
								cancelBlockInteraction(event, interactingPlayer, targetState);
							else if (targetState.getBlock() instanceof BlockContainer || targetState.getBlock() instanceof BlockDragonEgg)
								cancelBlockInteraction(event, interactingPlayer, targetState);
						}
					}
				} else {
					//Remove the uuid as the chunk owner since the uuid is not associated with a clan.
					ChunkUtils.clearChunkOwner(c);
				}
			}
		}
	}

	private static void cancelBlockInteraction(PlayerInteractEvent.RightClickBlock event, EntityPlayer placingPlayer, IBlockState targetState) {
		event.setCanceled(true);
		placingPlayer.sendMessage(new TextComponentString("You cannot interact with blocks in another clan's territory.").setStyle(TextStyles.RED));
		//Update the client informing it the interaction did not happen. Go in all directions in case surrounding blocks would have been affected.
		event.getWorld().notifyBlockUpdate(event.getPos(), targetState, targetState, 2);
		event.getWorld().notifyBlockUpdate(event.getPos().up(), targetState, targetState, 2);
		event.getWorld().notifyBlockUpdate(event.getPos().down(), targetState, targetState, 2);
		event.getWorld().notifyBlockUpdate(event.getPos().east(), targetState, targetState, 2);
		event.getWorld().notifyBlockUpdate(event.getPos().west(), targetState, targetState, 2);
		event.getWorld().notifyBlockUpdate(event.getPos().north(), targetState, targetState, 2);
		event.getWorld().notifyBlockUpdate(event.getPos().south(), targetState, targetState, 2);
	}

	@SubscribeEvent
	public void onDetonate(ExplosionEvent.Detonate event) {
		if(!event.getWorld().isRemote) {
			ArrayList<BlockPos> removeBlocks = Lists.newArrayList();
			for (BlockPos pos : event.getAffectedBlocks()) {
				Chunk c = event.getWorld().getChunk(pos);
				UUID chunkOwner = ChunkUtils.getChunkOwner(c);
				NewClan chunkClan = ClanCache.getClanById(chunkOwner);
				if (chunkClan != null) {
					IBlockState targetState = event.getWorld().getBlockState(pos);
					if (RaidingParties.hasActiveRaid(chunkClan) && !targetState.getBlock().hasTileEntity(targetState) && !(targetState.getBlock() instanceof BlockAir) && !(targetState.getBlock() instanceof BlockFlowingFluid))
						NewRaidRestoreDatabase.addRestoreBlock(c.getWorld().getDimension().getType().getId(), c, pos, BlockSerializeUtil.blockToString(targetState), chunkOwner);
					else
						removeBlocks.add(pos);
				} else if (Clans.cfg.protectWilderness && (Clans.cfg.minWildernessY < 0 ? pos.getY() >= event.getWorld().getSeaLevel() : pos.getY() >= Clans.cfg.minWildernessY))
					removeBlocks.add(pos);
			}
			for (BlockPos pos : removeBlocks)
				event.getAffectedBlocks().remove(pos);
			ArrayList<Entity> removeEntities = Lists.newArrayList();
			for (Entity entity : event.getAffectedEntities()) {
				if (entity instanceof EntityPlayer || (entity instanceof EntityTameable && ((EntityTameable) entity).getOwnerId() != null)) {
					Chunk c = event.getWorld().getChunk(entity.getPosition());
					UUID chunkOwner = ChunkUtils.getChunkOwner(c);
					NewClan chunkClan = ClanCache.getClanById(chunkOwner);
					ArrayList<NewClan> entityClans = entity instanceof EntityPlayer ? ClanCache.getClansByPlayer(entity.getUniqueID()) : ClanCache.getClansByPlayer(((EntityTameable) entity).getOwnerId());
					if (chunkClan != null && !entityClans.isEmpty() && entityClans.contains(chunkClan) && !RaidingParties.hasActiveRaid(chunkClan))
						removeEntities.add(entity);
				}
			}
			for (Entity entity : removeEntities)
				event.getAffectedEntities().remove(entity);
		}
	}

	@SubscribeEvent
	public void onLivingDamage(LivingDamageEvent event) {
		Entity entity = event.getEntityLiving();
		if(!entity.getEntityWorld().isRemote) {
            Chunk c = entity.getEntityWorld().getChunk(entity.getPosition());
            NewClan chunkClan = ClanCache.getClanById(ChunkUtils.getChunkOwner(c));
            Entity source = event.getSource().getTrueSource();
			if(source != null && ClanCache.isClaimAdmin(source.getUniqueID()))
				return;
            if (entity instanceof EntityPlayer || (entity instanceof EntityTameable && ((EntityTameable) entity).getOwnerId() != null)) {
				ArrayList<NewClan> entityClans = entity instanceof EntityPlayer ? ClanCache.getClansByPlayer(entity.getUniqueID()) : ClanCache.getClansByPlayer(((EntityTameable) entity).getOwnerId());
				if (chunkClan != null && !entityClans.isEmpty() && entityClans.contains(chunkClan) && !RaidingParties.hasActiveRaid(chunkClan) && (source instanceof EntityPlayer || (source instanceof EntityTameable && ((EntityTameable) source).getOwnerId() != null)))
					event.setCanceled(true);
				else if(RaidingParties.hasActiveRaid(chunkClan) && (source instanceof EntityPlayer || (source instanceof EntityTameable && ((EntityTameable) source).getOwnerId() != null))) {
					for (NewClan entityClan : entityClans)
						if (RaidingParties.isRaidedBy(entityClan, source instanceof EntityPlayer ? source.getUniqueID() : (((EntityTameable) source).getOwner() instanceof EntityPlayer ? ((EntityTameable) source).getOwnerId() : null)))
							return;
					event.setCanceled(true);
				}
			} else {//Entity is not a player
                if((source instanceof EntityPlayer || (source instanceof EntityTameable && ((EntityTameable) source).getOwnerId() != null))) {
                    if (RaidingParties.isRaidedBy(chunkClan, source instanceof EntityPlayer ? source.getUniqueID() : (((EntityTameable) source).getOwner() instanceof EntityPlayer ? ((EntityTameable) source).getOwnerId() : null)))
                        return;//Raiders can harm things
                    UUID sourceId = source instanceof EntityPlayer ? source.getUniqueID() : ((EntityTameable) source).getOwnerId();
                    ArrayList<NewClan> sourceClans = ClanCache.getClansByPlayer(sourceId);
                    if(sourceClans.contains(chunkClan) || chunkClan == null || RaidingParties.hasActiveRaid(chunkClan))
                        return;//Players can harm things
                    event.setCanceled(true);
                }
            }
		}
	}
}
