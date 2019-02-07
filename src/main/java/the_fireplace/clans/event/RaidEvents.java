package the_fireplace.clans.event;

import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import the_fireplace.clans.util.ChunkUtils;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.raid.RaidingParties;

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
}
