package the_fireplace.clans.forge.event;

import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import the_fireplace.clans.Clans;
import the_fireplace.clans.logic.RaidManagementLogic;

@Mod.EventBusSubscriber(modid = Clans.MODID)
public class RaidEvents {
	@SubscribeEvent
	public static void onBlockDrops(BlockEvent.HarvestDropsEvent event) {
		if(RaidManagementLogic.shouldCancelBlockDrops(event.getWorld(), event.getPos())) {
			//Double check that nothing gets dropped during a raid, to avoid block duping.
			event.getDrops().clear();
			event.setDropChance(0.0f);
		}
	}

	@SubscribeEvent
	public static void onPlayerDeath(LivingDeathEvent event) {
		if(event.getEntity() instanceof EntityPlayerMP)
			event.setCanceled(RaidManagementLogic.onPlayerDeath((EntityPlayerMP)event.getEntity()));
	}

	@SubscribeEvent
	public static void onChunkLoaded(ChunkEvent.Load event) {
		RaidManagementLogic.checkAndRestoreChunk(event.getChunk());
	}

	@SubscribeEvent
	public static void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
		RaidManagementLogic.onNeighborBlockNotified(event.getWorld(), event.getState(), event.getPos());
	}

	@SubscribeEvent
	public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
		if(event.getEntity() instanceof EntityFallingBlock)
			event.setCanceled(RaidManagementLogic.shouldCancelFallingBlockCreation((EntityFallingBlock)event.getEntity()));
	}

}
