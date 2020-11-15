package the_fireplace.clans.client.compat.journeymap;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import the_fireplace.clans.client.ClansClientModContainer;
import the_fireplace.clans.client.mapinterceptor.MapInterceptedEvent;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = ClansClientModContainer.MODID, value = Side.CLIENT)
public class MapChunkUpdater {
    @SubscribeEvent
    public static void onMapUpdateRecieved(MapInterceptedEvent event) {
        System.out.println("We got here.");
    }
}
