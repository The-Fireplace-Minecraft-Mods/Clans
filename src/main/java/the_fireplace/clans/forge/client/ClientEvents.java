package the_fireplace.clans.forge.client;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import the_fireplace.clans.Clans;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = Clans.MODID)
public class ClientEvents {
    @SubscribeEvent
    public static void configChanged(ConfigChangedEvent event) {
        if (event.getModID().equals(Clans.MODID))
            ConfigManager.sync(Clans.MODID, Config.Type.INSTANCE);
    }
}
