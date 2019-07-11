package the_fireplace.clans.forge.event;

import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import the_fireplace.clans.Clans;
import the_fireplace.clans.forge.legacy.ClaimedLandCapability;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.data.ClanChunkData;
import the_fireplace.clans.forge.legacy.CapHelper;
import the_fireplace.clans.util.ChunkUtils;

@Mod.EventBusSubscriber(modid = Clans.MODID)
public class LegacyCompatEvents {
    @SubscribeEvent
    public static void onChunkLoad(ChunkDataEvent.Load event) {
        Chunk c = event.getChunk();
        checkPre120Compat(c);
    }

    static void checkPre120Compat(Chunk c) {
        ClaimedLandCapability cap = CapHelper.getClaimedLandCapability(c);
        if(cap.pre120() && cap.getClan() != null) {
            Clan clan = ClanCache.getClanById(cap.getClan());
            if(clan == null) {
                ChunkUtils.clearChunkOwner(c);
                return;
            }
            ClanChunkData.addChunk(clan, c.x, c.z, c.getWorld().provider.getDimension());
            cap.setPre120(false);
        }
    }
}
