package the_fireplace.clans.forge.event;

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
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import the_fireplace.clans.Clans;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.cache.RaidingParties;
import the_fireplace.clans.data.RaidBlockPlacementDatabase;
import the_fireplace.clans.data.RaidRestoreDatabase;
import the_fireplace.clans.forge.FakePlayerUtil;
import the_fireplace.clans.forge.legacy.CapHelper;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.util.*;
import the_fireplace.clans.util.translation.TranslationUtil;

import java.util.ArrayList;
import java.util.UUID;

@Mod.EventBusSubscriber(modid= Clans.MODID)
public class LandProtectionEvents {
	@SubscribeEvent
	public static void onBreakBlock(BlockEvent.BreakEvent event){
		if(!event.getWorld().isRemote) {
			Chunk c = event.getWorld().getChunk(event.getPos());
			UUID chunkOwner = ChunkUtils.getChunkOwner(c);
			if (chunkOwner != null) {
				Clan chunkClan = ClanCache.getClanById(chunkOwner);
				if (chunkClan != null) {
					EntityPlayer breakingPlayer = event.getPlayer();
					if (breakingPlayer instanceof EntityPlayerMP) {
						ArrayList<Clan> playerClans = ClanCache.getPlayerClans(breakingPlayer.getUniqueID());
						boolean isRaided = RaidingParties.isRaidedBy(chunkClan, breakingPlayer);
						if (!ClanCache.isClaimAdmin((EntityPlayerMP) breakingPlayer) && (playerClans.isEmpty() || !playerClans.contains(chunkClan)) && !isRaided && !FakePlayerUtil.isAllowedFakePlayer(breakingPlayer)) {
							event.setCanceled(true);
							breakingPlayer.sendMessage(TranslationUtil.getTranslation(breakingPlayer.getUniqueID(), "clans.protection.break.claimed").setStyle(TextStyles.RED));
						} else if (isRaided) {
							IBlockState targetState = event.getWorld().getBlockState(event.getPos());
							if (targetState.getBlock().hasTileEntity(targetState)) {
								event.setCanceled(true);
								if(ClanCache.isClaimAdmin((EntityPlayerMP) breakingPlayer))
									breakingPlayer.sendMessage(TranslationUtil.getTranslation(breakingPlayer.getUniqueID(), "clans.protection.break.raid").setStyle(TextStyles.RED));
								else
									breakingPlayer.sendMessage(TranslationUtil.getTranslation(breakingPlayer.getUniqueID(), "clans.protection.break.claimed_raid").setStyle(TextStyles.RED));
							} else
								RaidRestoreDatabase.addRestoreBlock(c.getWorld().provider.getDimension(), c, event.getPos(), BlockSerializeUtil.blockToString(targetState));
						}
					}
					return;
				} else {
					//Remove the uuid as the chunk owner since the uuid is not associated with a clan.
					ChunkUtils.clearChunkOwner(c);
				}
			}
			if (Clans.getConfig().isProtectWilderness() && (Clans.getConfig().getMinWildernessY() < 0 ? event.getPos().getY() >= event.getWorld().getSeaLevel() : event.getPos().getY() >= Clans.getConfig().getMinWildernessY()) && !FakePlayerUtil.isAllowedFakePlayer(event.getPlayer()) && (!(event.getPlayer() instanceof EntityPlayerMP) || (!ClanCache.isClaimAdmin((EntityPlayerMP) event.getPlayer()) && !PermissionManager.hasPermission((EntityPlayerMP)event.getPlayer(), PermissionManager.PROTECTION_PREFIX+"break.protected_wilderness")))) {
				event.setCanceled(true);
				event.getPlayer().sendMessage(TranslationUtil.getTranslation(event.getPlayer().getUniqueID(), "clans.protection.break.wilderness").setStyle(TextStyles.RED));
			}
		}
	}

	@SubscribeEvent
	public static void onCropTrample(BlockEvent.FarmlandTrampleEvent event){
		if(!event.getWorld().isRemote) {
			Chunk c = event.getWorld().getChunk(event.getPos());
			UUID chunkOwner = ChunkUtils.getChunkOwner(c);
			if (chunkOwner != null) {
				Clan chunkClan = ClanCache.getClanById(chunkOwner);
				if (chunkClan != null) {
					EntityPlayer breakingPlayer = event.getEntity() instanceof EntityPlayer ? (EntityPlayer) event.getEntity() : null;
					if (breakingPlayer != null) {
						ArrayList<Clan> playerClans = ClanCache.getPlayerClans(breakingPlayer.getUniqueID());
						boolean isRaided = RaidingParties.isRaidedBy(chunkClan, breakingPlayer);
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

	@SubscribeEvent
	public static void onBlockPlace(BlockEvent.PlaceEvent event) {
		if(!event.getWorld().isRemote) {
			Chunk c = event.getWorld().getChunk(event.getPos());
			UUID chunkOwner = ChunkUtils.getChunkOwner(c);
			EntityPlayer placingPlayer = event.getPlayer();
			if (placingPlayer instanceof EntityPlayerMP) {
				if (chunkOwner != null) {
					Clan chunkClan = ClanCache.getClanById(chunkOwner);
					if (chunkClan != null) {
						ArrayList<Clan> playerClans = ClanCache.getPlayerClans(placingPlayer.getUniqueID());
						if ((!ClanCache.isClaimAdmin((EntityPlayerMP) placingPlayer) && (playerClans.isEmpty() || (!playerClans.contains(chunkClan) && !RaidingParties.isRaidedBy(chunkClan, placingPlayer)))) && !FakePlayerUtil.isAllowedFakePlayer(event.getPlayer())) {
							event.setCanceled(true);
							EntityEquipmentSlot hand = event.getHand().equals(EnumHand.MAIN_HAND) ? EntityEquipmentSlot.MAINHAND : EntityEquipmentSlot.OFFHAND;
							if(((EntityPlayerMP) placingPlayer).connection != null)
								((EntityPlayerMP) placingPlayer).connection.sendPacket(new SPacketEntityEquipment(placingPlayer.getEntityId(), hand, placingPlayer.getItemStackFromSlot(hand)));
							placingPlayer.sendMessage(TranslationUtil.getTranslation(placingPlayer.getUniqueID(), "clans.protection.place.territory").setStyle(TextStyles.RED));
						} else if (RaidingParties.hasActiveRaid(chunkClan)) {
							ItemStack out = event.getPlayer().getHeldItem(event.getHand()).copy();
							out.setCount(1);
							if(!Clans.getConfig().isNoReclaimTNT() || !(event.getBlockSnapshot().getCurrentBlock().getBlock() instanceof BlockTNT))
								RaidBlockPlacementDatabase.getInstance().addPlacedBlock(placingPlayer.getUniqueID(), out);
							RaidRestoreDatabase.addRemoveBlock(event.getWorld().provider.getDimension(), c, event.getPos());
						}
					}
					return;
				} else {
					//Remove the uuid as the chunk owner since the uuid is not associated with a clan.
					ChunkUtils.clearChunkOwner(c);
				}
				if (!ClanCache.isClaimAdmin((EntityPlayerMP) event.getPlayer()) && !PermissionManager.hasPermission((EntityPlayerMP)event.getPlayer(), PermissionManager.PROTECTION_PREFIX+"build.protected_wilderness") && Clans.getConfig().isProtectWilderness() && (Clans.getConfig().getMinWildernessY() < 0 ? event.getPos().getY() >= event.getWorld().getSeaLevel() : event.getPos().getY() >= Clans.getConfig().getMinWildernessY()) && !FakePlayerUtil.isAllowedFakePlayer(event.getPlayer())) {
					event.setCanceled(true);
					EntityEquipmentSlot hand = event.getHand().equals(EnumHand.MAIN_HAND) ? EntityEquipmentSlot.MAINHAND : EntityEquipmentSlot.OFFHAND;
					if(((EntityPlayerMP) placingPlayer).connection != null)
						((EntityPlayerMP) placingPlayer).connection.sendPacket(new SPacketEntityEquipment(placingPlayer.getEntityId(), hand, placingPlayer.getItemStackFromSlot(hand)));
					event.getPlayer().inventory.markDirty();
					event.getPlayer().sendMessage(TranslationUtil.getTranslation(event.getPlayer().getUniqueID(), "clans.protection.place.wilderness").setStyle(TextStyles.RED));
				}
			}
		}
	}

	@SubscribeEvent
	public static void onFluidPlaceBlock(BlockEvent.FluidPlaceBlockEvent event) {
		if(!event.getWorld().isRemote) {
			Chunk c = event.getWorld().getChunk(event.getPos());
			UUID chunkOwner = ChunkUtils.getChunkOwner(c);
			if (chunkOwner != null) {
				Chunk sourceChunk = event.getWorld().getChunk(event.getLiquidPos());
				UUID sourceChunkOwner = ChunkUtils.getChunkOwner(sourceChunk);
				if (!chunkOwner.equals(sourceChunkOwner))
					event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public static void rightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		if(!event.getWorld().isRemote) {
			Chunk c = event.getWorld().getChunk(event.getPos());
			UUID chunkOwner = ChunkUtils.getChunkOwner(c);
			if (chunkOwner != null) {
				Clan chunkClan = ClanCache.getClanById(chunkOwner);
				if (chunkClan != null) {
					EntityPlayer interactingPlayer = event.getEntityPlayer();
					if (interactingPlayer instanceof EntityPlayerMP) {
						ArrayList<Clan> playerClan = ClanCache.getPlayerClans(interactingPlayer.getUniqueID());
						IBlockState targetState = event.getWorld().getBlockState(event.getPos());
						if (!ClanCache.isClaimAdmin((EntityPlayerMP) interactingPlayer) && (playerClan.isEmpty() || !playerClan.contains(chunkClan)) && (!RaidingParties.isRaidedBy(chunkClan, interactingPlayer) || targetState.getBlock() instanceof BlockContainer || targetState.getBlock() instanceof BlockDragonEgg)) {
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

	private static void cancelBlockInteraction(PlayerInteractEvent.RightClickBlock event, EntityPlayer interactingPlayer, IBlockState targetState) {
		event.setCanceled(true);
		interactingPlayer.sendMessage(TranslationUtil.getTranslation(interactingPlayer.getUniqueID(), "clans.protection.interact.territory").setStyle(TextStyles.RED));
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
	public static void onDetonate(ExplosionEvent.Detonate event) {
		if(!event.getWorld().isRemote) {
			ArrayList<BlockPos> removeBlocks = Lists.newArrayList();
			for (BlockPos pos : event.getAffectedBlocks()) {
				Chunk c = event.getWorld().getChunk(pos);
				UUID chunkOwner = ChunkUtils.getChunkOwner(c);
				Clan chunkClan = ClanCache.getClanById(chunkOwner);
				IBlockState targetState = event.getWorld().getBlockState(pos);
				if (chunkClan != null) {
					if (RaidingParties.hasActiveRaid(chunkClan) && !targetState.getBlock().hasTileEntity(targetState) && !(targetState.getBlock() instanceof BlockAir) && !(targetState.getBlock() instanceof BlockLiquid))
						RaidRestoreDatabase.addRestoreBlock(c.getWorld().provider.getDimension(), c, pos, BlockSerializeUtil.blockToString(targetState));
					else if(!Clans.getConfig().isChainTNT() || !(targetState.getBlock() instanceof BlockTNT))
						removeBlocks.add(pos);
				} else if (Clans.getConfig().isProtectWilderness() && (Clans.getConfig().getMinWildernessY() < 0 ? pos.getY() >= event.getWorld().getSeaLevel() : pos.getY() >= Clans.getConfig().getMinWildernessY()) && (!Clans.getConfig().isChainTNT() || !(targetState.getBlock() instanceof BlockTNT)))
					removeBlocks.add(pos);
			}
			for (BlockPos pos : removeBlocks)
				event.getAffectedBlocks().remove(pos);
			ArrayList<Entity> removeEntities = Lists.newArrayList();
			for (Entity entity : event.getAffectedEntities()) {
				if (entity instanceof EntityPlayer || (entity instanceof EntityTameable && ((EntityTameable) entity).getOwnerId() != null)) {
					Chunk c = event.getWorld().getChunk(entity.getPosition());
					UUID chunkOwner = ChunkUtils.getChunkOwner(c);
					Clan chunkClan = ClanCache.getClanById(chunkOwner);
					ArrayList<Clan> entityClans = entity instanceof EntityPlayer ? ClanCache.getPlayerClans(entity.getUniqueID()) : ClanCache.getPlayerClans(((EntityTameable) entity).getOwnerId());
					if (chunkClan != null && !entityClans.isEmpty() && entityClans.contains(chunkClan) && !RaidingParties.hasActiveRaid(chunkClan))
						removeEntities.add(entity);
				}
			}
			for (Entity entity : removeEntities)
				event.getAffectedEntities().remove(entity);
		}
	}

	@SubscribeEvent
	public static void onLivingDamage(LivingDamageEvent event) {
		Entity entity = event.getEntityLiving();
		if(!entity.getEntityWorld().isRemote) {
            Chunk c = entity.getEntityWorld().getChunk(entity.getPosition());
            Clan chunkClan = ClanCache.getClanById(ChunkUtils.getChunkOwner(c));
            Entity source = event.getSource().getTrueSource();
            if(source instanceof EntityPlayerMP && ClanCache.isClaimAdmin((EntityPlayerMP) source))
            	return;
            if(entity instanceof EntityPlayer || (entity instanceof EntityTameable && ((EntityTameable) entity).getOwnerId() != null)) {
				ArrayList<Clan> entityClans = entity instanceof EntityPlayer ? ClanCache.getPlayerClans(entity.getUniqueID()) : ClanCache.getPlayerClans(((EntityTameable) entity).getOwnerId());
				if (chunkClan != null && !entityClans.isEmpty() && entityClans.contains(chunkClan) && !RaidingParties.hasActiveRaid(chunkClan) && (source instanceof EntityPlayer || (source instanceof EntityTameable && ((EntityTameable) source).getOwnerId() != null)) && !FakePlayerUtil.isAllowedFakePlayer(event.getSource().getTrueSource()))
					event.setCanceled(true);
				else if((RaidingParties.hasActiveRaid(chunkClan) && (source instanceof EntityPlayer || (source instanceof EntityTameable && ((EntityTameable) source).getOwnerId() != null))) && !FakePlayerUtil.isAllowedFakePlayer(event.getSource().getTrueSource())) {
					for (Clan entityClan : entityClans)
						if (RaidingParties.isRaidedBy(entityClan, source instanceof EntityPlayer ? (EntityPlayer) source : (((EntityTameable) source).getOwner() instanceof EntityPlayer ? (EntityPlayer) ((EntityTameable) source).getOwner() : null)))
							return;
					event.setCanceled(true);
				}
			} else {//Entity is not a player
                if((source instanceof EntityPlayer || (source instanceof EntityTameable && ((EntityTameable) source).getOwnerId() != null))) {
                    if (RaidingParties.isRaidedBy(chunkClan, source instanceof EntityPlayer ? (EntityPlayer) source : (((EntityTameable) source).getOwner() instanceof EntityPlayer ? (EntityPlayer) ((EntityTameable) source).getOwner() : null)))
                        return;//Raiders can harm things
                    UUID sourceId = source instanceof EntityPlayer ? source.getUniqueID() : ((EntityTameable) source).getOwnerId();
                    ArrayList<Clan> sourceClans = ClanCache.getPlayerClans(sourceId);
                    if(sourceClans.contains(chunkClan) || chunkClan == null || RaidingParties.hasActiveRaid(chunkClan) || FakePlayerUtil.isAllowedFakePlayer(event.getSource().getTrueSource()))
                        return;//Players can harm things
					event.setCanceled(true);
                }
            }
		}
	}

	@SubscribeEvent
	public static void onEntitySpawn(EntityJoinWorldEvent event) {
		if(Clans.getConfig().isPreventMobsOnClaims() && !event.getWorld().isRemote && event.getEntity() instanceof IMob) {
			if(CapHelper.getClaimedLandCapability(event.getWorld().getChunk(event.getEntity().getPosition())).getClan() != null)
				event.setCanceled(true);
		}
	}
}