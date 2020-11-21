package the_fireplace.clans.client.compat.journeymap;

import journeymap.client.api.IClientAPI;
import journeymap.client.api.display.DisplayType;
import journeymap.client.api.display.PolygonOverlay;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import the_fireplace.clans.client.ClansClientModContainer;
import the_fireplace.clans.client.mapprocessing.MapInterceptedEvent;
import the_fireplace.clans.legacy.model.ChunkPosition;
import the_fireplace.clans.multithreading.ConcurrentExecutionManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SideOnly(Side.CLIENT)
public class OverlayTracker {
    private final IClientAPI jmAPI;
    private final Map<ChunkPosition, PolygonOverlay> mapDataOverlays = new ConcurrentHashMap<>();

    public OverlayTracker(IClientAPI jmAPI) {
        this.jmAPI = jmAPI;
    }

    @SubscribeEvent
    public void onMapDataReceived(MapInterceptedEvent event) {
        if (jmAPI.playerAccepts(ClansClientModContainer.MODID, DisplayType.Polygon)) {
            event.getInterceptedMapData().forEach((chunkPosition, clanName) -> {
                ConcurrentExecutionManager.runKillable(() -> {
                    if (clanName != null) {
                        updatePositionOwner(chunkPosition, clanName);
                    } else {
                        clearPositionOwner(chunkPosition);
                    }
                });
            });
        }
    }

    private void updatePositionOwner(ChunkPosition chunkPosition, String chunkOwner) {
        PolygonOverlay overlay = ClaimedChunkOverlayFactory.create(chunkPosition, chunkOwner);
        mapDataOverlays.put(chunkPosition, overlay);
        try {
            jmAPI.show(overlay);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearPositionOwner(ChunkPosition chunkPosition) {
        PolygonOverlay overlay = mapDataOverlays.remove(chunkPosition);
        if (overlay != null) {
            jmAPI.remove(overlay);
        }
    }
}
