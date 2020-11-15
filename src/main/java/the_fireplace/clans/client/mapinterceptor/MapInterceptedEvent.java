package the_fireplace.clans.client.mapinterceptor;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import the_fireplace.clans.legacy.model.ChunkPosition;

import java.util.Collections;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class MapInterceptedEvent extends Event {
    public static void fire(Map<ChunkPosition, String> interceptedMapData) {
        MinecraftForge.EVENT_BUS.post(new MapInterceptedEvent(interceptedMapData));
    }

    private final Map<ChunkPosition, String> interceptedMapData;

    private MapInterceptedEvent(Map<ChunkPosition, String> interceptedMapData) {
        this.interceptedMapData = Collections.unmodifiableMap(interceptedMapData);
    }

    public Map<ChunkPosition, String> getInterceptedMapData() {
        return interceptedMapData;
    }
}
