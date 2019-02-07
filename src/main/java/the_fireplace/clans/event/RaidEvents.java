package the_fireplace.clans.event;

import net.minecraft.entity.player.EntityPlayerMP;
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
				ChunkUtils.setChunkOwner(c, null);
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerDeath(LivingDeathEvent event) {
		if(event.getEntityLiving() instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP) event.getEntityLiving();
			Clan clan = ClanCache.getPlayerClan(player.getUniqueID());
			if(clan != null && RaidingParties.hasActiveRaid(clan))
				RaidingParties.getActiveRaid(clan).addDeadDefender(player);
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
}
