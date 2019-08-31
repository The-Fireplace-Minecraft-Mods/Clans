package the_fireplace.clans.forge.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.EnumHand;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.minecart.MinecartInteractEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import the_fireplace.clans.Clans;
import the_fireplace.clans.logic.LandProtectionEventLogic;
import the_fireplace.clans.logic.PlayerEventLogic;

@Mod.EventBusSubscriber(modid= Clans.MODID)
public class LandProtectionEvents {
	@SubscribeEvent
	public static void onBreakBlock(BlockEvent.BreakEvent event){
		event.setCanceled(LandProtectionEventLogic.shouldCancelBlockBroken(event.getWorld(), event.getPos(), event.getPlayer()));
		if(!event.isCanceled())
			LandProtectionEventLogic.onBlockBroken(event.getWorld(), event.getPos(), event.getPlayer());
	}

	@SubscribeEvent
	public static void onCropTrample(BlockEvent.FarmlandTrampleEvent event){
		event.setCanceled(LandProtectionEventLogic.shouldCancelCropTrample(event.getWorld(), event.getPos(), event.getEntity() instanceof EntityPlayer ? (EntityPlayer)event.getEntity() : null));
	}

	@SubscribeEvent
	public static void onBlockPlace(BlockEvent.PlaceEvent event) {
		event.setCanceled(LandProtectionEventLogic.shouldCancelBlockPlacement(event.getWorld(), event.getPos(), event.getPlayer(), event.getHand().equals(EnumHand.MAIN_HAND) ? EntityEquipmentSlot.MAINHAND : EntityEquipmentSlot.OFFHAND, event.getBlockSnapshot().getCurrentBlock().getBlock()));
		if(!event.isCanceled())
			LandProtectionEventLogic.onBlockPlaced(event.getWorld(), event.getPos(), event.getPlayer(), event.getHand().equals(EnumHand.MAIN_HAND) ? EntityEquipmentSlot.MAINHAND : EntityEquipmentSlot.OFFHAND, event.getBlockSnapshot().getCurrentBlock().getBlock());
	}

	@SubscribeEvent
	public static void onFluidPlaceBlock(BlockEvent.FluidPlaceBlockEvent event) {
		event.setCanceled(LandProtectionEventLogic.shouldCancelFluidPlaceBlock(event.getWorld(), event.getLiquidPos(), event.getPos()));
	}

	@SubscribeEvent
	public static void rightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		event.setCanceled(LandProtectionEventLogic.shouldCancelRightClickBlock(event.getWorld(), event.getPos(), event.getEntityPlayer(), event.getItemStack()));
	}

	@SubscribeEvent
	public static void minecartInteract(MinecartInteractEvent event) {
		event.setCanceled(LandProtectionEventLogic.shouldCancelMinecartInteract(event.getMinecart(), event.getPlayer()));
	}

	@SubscribeEvent
	public static void onDetonate(ExplosionEvent.Detonate event) {
		LandProtectionEventLogic.onDetonate(event.getWorld(), event.getAffectedBlocks(), event.getAffectedEntities());
	}

	@SubscribeEvent
	public static void onLivingDamage(LivingDamageEvent event) {
		event.setCanceled(LandProtectionEventLogic.shouldCancelEntityDamage(event.getEntity(), event.getSource().getTrueSource()));
		if(!event.isCanceled() && event.getEntityLiving() instanceof EntityPlayer)
			PlayerEventLogic.onPlayerDamage((EntityPlayer) event.getEntityLiving());
	}

	@SubscribeEvent
	public static void onAttackEntity(AttackEntityEvent event) {
		event.setCanceled(LandProtectionEventLogic.shouldCancelEntityDamage(event.getEntity(), event.getEntityPlayer()));
		if(!event.isCanceled() && event.getEntityLiving() instanceof EntityPlayer)
			PlayerEventLogic.onPlayerDamage((EntityPlayer) event.getEntityLiving());
	}

	@SubscribeEvent
	public static void onEntitySpawn(EntityJoinWorldEvent event) {
		event.setCanceled(LandProtectionEventLogic.shouldCancelEntitySpawn(event.getWorld(), event.getEntity()));
	}
}
