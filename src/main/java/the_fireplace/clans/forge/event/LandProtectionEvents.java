package the_fireplace.clans.forge.event;

import net.minecraft.block.BlockFire;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.minecart.MinecartInteractEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.UseHoeEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import the_fireplace.clans.ClansHelper;
import the_fireplace.clans.logic.LandProtectionEventLogic;
import the_fireplace.clans.logic.PlayerEventLogic;
import the_fireplace.clans.logic.RaidManagementLogic;
import the_fireplace.clans.util.EntityUtil;

import java.util.Objects;

@Mod.EventBusSubscriber(modid= ClansHelper.MODID)
public class LandProtectionEvents {
	@SubscribeEvent
	public static void onBreakBlock(BlockEvent.BreakEvent event){
		event.setCanceled(LandProtectionEventLogic.shouldCancelBlockBroken(event.getWorld(), event.getPos(), event.getPlayer()));
		if(!event.isCanceled())
			RaidManagementLogic.onBlockBroken(event.getWorld(), event.getPos(), event.getState());
	}

	@SubscribeEvent
	public static void onCropTrample(BlockEvent.FarmlandTrampleEvent event){
		event.setCanceled(LandProtectionEventLogic.shouldCancelCropTrample(event.getWorld(), event.getPos(), event.getEntity() instanceof EntityPlayer ? (EntityPlayer)event.getEntity() : null));
	}

	@SubscribeEvent
	public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
		if(!(event.getEntity() instanceof EntityPlayer))
			return;
		event.setCanceled(LandProtectionEventLogic.shouldCancelBlockPlacement(event.getWorld(), event.getPos(), (EntityPlayer) event.getEntity(), EnumHand.OFF_HAND.equals(((EntityPlayer) event.getEntity()).getActiveHand()) ? EntityEquipmentSlot.OFFHAND : EntityEquipmentSlot.MAINHAND));
		if(!event.isCanceled())
			RaidManagementLogic.onBlockPlaced(event.getWorld(), event.getPos(), (EntityPlayer) event.getEntity(), EnumHand.OFF_HAND.equals(((EntityPlayer) event.getEntity()).getActiveHand()) ? EntityEquipmentSlot.OFFHAND : EntityEquipmentSlot.MAINHAND, event.getBlockSnapshot().getCurrentBlock().getBlock());
	}

	@SubscribeEvent
	public static void onFluidPlaceBlock(BlockEvent.FluidPlaceBlockEvent event) {
		event.setCanceled(LandProtectionEventLogic.shouldCancelFluidPlaceBlock(event.getWorld(), event.getLiquidPos(), event.getPos()));
        if(!event.isCanceled())
            RaidManagementLogic.onBlockBroken(event.getWorld(), event.getPos(), event.getOriginalState());
	}

	@SubscribeEvent
	public static void rightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		event.setCanceled(LandProtectionEventLogic.shouldCancelRightClickBlock(event.getWorld(), event.getPos(), event.getEntityPlayer(), event.getItemStack(), event.getHand()));
	}

	@SubscribeEvent
	public static void leftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
		//Don't cancel all left click blocks in case player is allowed to build/destroy but not interact
		//This prevents unauthorized interaction with Storage Drawers and stops players from putting out fires in others' territory
		if(LandProtectionEventLogic.isContainer(event.getWorld(), event.getPos(), null, null))
			event.setCanceled(LandProtectionEventLogic.shouldCancelRightClickBlock(event.getWorld(), event.getPos(), event.getEntityPlayer(), event.getItemStack(), event.getHand()));
		else if(event.getWorld().getBlockState(event.getPos().offset(Objects.requireNonNull(event.getFace()))).getBlock() instanceof BlockFire)
			event.setCanceled(LandProtectionEventLogic.shouldCancelBlockBroken(event.getWorld(), event.getPos(), event.getEntityPlayer()));
	}

	@SubscribeEvent
	public static void fillBucket(FillBucketEvent event) {
		if(event.getTarget() != null)
			event.setCanceled(LandProtectionEventLogic.shouldCancelRightClickBlock(event.getWorld(), event.getTarget().typeOfHit == RayTraceResult.Type.ENTITY ? event.getTarget().entityHit.getPosition() : event.getTarget().getBlockPos(), event.getEntityPlayer(), event.getEmptyBucket(), event.getEntityPlayer().getActiveHand()));
	}

	@SubscribeEvent
	public static void useHoe(UseHoeEvent event) {
		event.setCanceled(LandProtectionEventLogic.shouldCancelRightClickBlock(event.getWorld(), event.getPos(), event.getEntityPlayer(), event.getCurrent(), event.getEntityPlayer().getActiveHand()));
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
	public static void onLivingDamage(LivingHurtEvent event) {
		event.setCanceled(LandProtectionEventLogic.shouldCancelEntityDamage(event.getEntity(), event.getSource(), event.getSource().getTrueSource()));
		if(!event.isCanceled() && event.getEntityLiving() instanceof EntityPlayer)
			PlayerEventLogic.onPlayerDamage((EntityPlayer) event.getEntityLiving());
	}

	@SubscribeEvent
	public static void onProjectileImpact(ProjectileImpactEvent event) {
		if(event.getRayTraceResult().typeOfHit == RayTraceResult.Type.ENTITY)
			event.setCanceled(LandProtectionEventLogic.shouldCancelEntityDamage(event.getRayTraceResult().entityHit, null, EntityUtil.tryFindSource(event.getEntity())));
		//TODO protect from block interaction as well?
	}

	@SubscribeEvent
	public static void onAttackEntity(AttackEntityEvent event) {
		event.setCanceled(LandProtectionEventLogic.shouldCancelEntityDamage(event.getTarget(), null, event.getEntityPlayer()));
	}

	@SubscribeEvent
	public static void onKnockback(LivingKnockBackEvent event) {
		event.setCanceled(LandProtectionEventLogic.shouldCancelEntityDamage(event.getEntityLiving(), null, event.getAttacker()));
	}

	@SubscribeEvent
	public static void onEntitySpawn(LivingSpawnEvent.CheckSpawn event) {
		boolean cancel = LandProtectionEventLogic.shouldCancelEntitySpawn(event.getWorld(), event.getEntity(), new BlockPos(event.getX(), event.getY(), event.getZ()));
		event.setResult(cancel ? Event.Result.DENY : Event.Result.DEFAULT);
	}

	@SubscribeEvent
	public static void onSpecialSpawn(LivingSpawnEvent.SpecialSpawn event) {
		boolean cancel = LandProtectionEventLogic.shouldCancelEntitySpawn(event.getWorld(), event.getEntity(), new BlockPos(event.getX(), event.getY(), event.getZ()));
		event.setResult(cancel ? Event.Result.DENY : Event.Result.DEFAULT);
		event.setCanceled(cancel);
	}

	//TODO entityinteractspecific
}
