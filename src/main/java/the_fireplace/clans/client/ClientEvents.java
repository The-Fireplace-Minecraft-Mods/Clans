package the_fireplace.clans.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import the_fireplace.clans.Clans;
import the_fireplace.clans.network.ClansClientConnectedMessage;
import the_fireplace.clans.network.PacketDispatcher;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = Clans.MODID)
public class ClientEvents {
    @SubscribeEvent
    public static void configChanged(ConfigChangedEvent event) {
        if (event.getModID().equals(Clans.MODID))
            ConfigManager.sync(Clans.MODID, Config.Type.INSTANCE);
    }

    @SubscribeEvent
    public static void onConnectToServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        PacketDispatcher.sendToServer(new ClansClientConnectedMessage(Minecraft.getMinecraft().getSession().getProfile().getId()));
    }
}
