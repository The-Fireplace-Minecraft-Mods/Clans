package the_fireplace.clans.client.listener;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import the_fireplace.clans.client.clan.land.ClientChunkOwnerCache;
import the_fireplace.clans.client.mapprocessing.MapInterceptedEvent;
import the_fireplace.clans.legacy.model.ChunkPosition;

@SideOnly(Side.CLIENT)
//@Mod.EventBusSubscriber(modid = ClansClientModContainer.MODID, value = Side.CLIENT)
public final class ClientMapUpdater {

    //@SubscribeEvent(priority = EventPriority.HIGH)
    public static void onMapUpdateRecieved(MapInterceptedEvent event) {
        event.getInterceptedMapData().forEach(((chunkPosition, clanName) -> {
            if (clanName != null) {
                updatePositionOwner(chunkPosition, clanName);
            } else {
                clearPositionOwner(chunkPosition);
            }
        }));
    }

    private static void updatePositionOwner(ChunkPosition chunkPosition, String chunkOwner) {
        ClientChunkOwnerCache.setChunkOwner(chunkPosition, chunkOwner);
    }

    private static void clearPositionOwner(ChunkPosition chunkPosition) {
        ClientChunkOwnerCache.clearChunkOwner(chunkPosition);
    }
}
