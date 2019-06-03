package the_fireplace.clans.event;

import net.minecraft.network.NetHandlerPlayServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.handshake.NetworkDispatcher;
import the_fireplace.clans.Clans;
import the_fireplace.clans.util.translation.TranslationUtil;

import java.util.Map;

@Mod.EventBusSubscriber(modid=Clans.MODID)
public class NetworkEvents {

    @SubscribeEvent
    public static void clientConnectToServer(FMLNetworkEvent.ServerConnectionFromClientEvent event) {
        Map<String, String> clientMods = NetworkDispatcher.get(event.getManager()).getModList();
        if(event.getHandler() instanceof NetHandlerPlayServer && ((NetHandlerPlayServer) event.getHandler()).player != null && clientMods.containsKey("clans") && !clientMods.get("clans").startsWith("1.0.") && !clientMods.get("clans").startsWith("1.1.") && !clientMods.get("clans").startsWith("1.2."))
            TranslationUtil.clansClients.add(((NetHandlerPlayServer) event.getHandler()).player.getUniqueID());
    }
}
