package the_fireplace.clans.event;

import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.ClaimedLandCapability;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.ClanChunkCache;
import the_fireplace.clans.util.CapHelper;
import the_fireplace.clans.util.ChunkUtils;

@Mod.EventBusSubscriber(modid = Clans.MODID)
public class LegacyCompatEvents {
    @SubscribeEvent
    public static void onChunkLoad(ChunkDataEvent.Load event) {
        Chunk c = event.getChunk();
        ClaimedLandCapability cap = CapHelper.getClaimedLandCapability(c);
        if(cap.pre120() && cap.getClan() != null) {
            Clan clan = ClanCache.getClanById(cap.getClan());
            if(clan == null) {
                ChunkUtils.clearChunkOwner(c);
                return;
            }
            ClanChunkCache.addChunk(clan, c.x, c.z, c.getWorld().provider.getDimension());
            cap.setPre120(false);
        }
    }
}
