package the_fireplace.clans.event;

import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.ClaimedLandCapability;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.ClanChunkCache;
import the_fireplace.clans.clan.NewClan;
import the_fireplace.clans.util.CapHelper;
import the_fireplace.clans.util.ChunkUtils;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = Clans.MODID)
public class LegacyCompatEvents {
    @SubscribeEvent
    public static void onChunkLoad(ChunkDataEvent.Load event) {
        IChunk c = event.getChunk();
        ClaimedLandCapability cap = CapHelper.getClaimedLandCapability(c);
        if(cap.pre120() && cap.getClan() != null) {
            NewClan clan = ClanCache.getClanById(cap.getClan());
            if(clan == null) {
                ChunkUtils.clearChunkOwner(c);
                return;
            }
            ClanChunkCache.addChunk(clan, c.getPos().x, c.getPos().z, Objects.requireNonNull(c.getWorldForge()).getDimension().getType().getId());
            cap.setPre120(false);
        }
    }
}