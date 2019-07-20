package the_fireplace.clans;

import com.google.inject.Inject;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import the_fireplace.clans.Clans;
import the_fireplace.clans.logic.ServerEventLogic;
import the_fireplace.clans.sponge.compat.SpongeMinecraftHelper;
import the_fireplace.clans.sponge.event.LandProtectionEvents;

//@Plugin(id = Clans.MODID+"sponge", name = Clans.MODNAME, version = Clans.VERSION, description = "A server-side land protection and PVP system.", url = "https://www.curseforge.com/minecraft/mc-mods/clans", authors = {"The_Fireplace"})
public class ClansSponge {

    @Inject
    public static Logger logger;

    @Listener()
    public void init(GameInitializationEvent event) {
        if(Clans.getMinecraftHelper() == null) {
            Clans.setMinecraftHelper(new SpongeMinecraftHelper());
            Clans.initialize();
            Sponge.getEventManager().registerListeners(this, new LandProtectionEvents());
        }
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        if(Clans.getMinecraftHelper() instanceof SpongeMinecraftHelper)
            ServerEventLogic.onServerStarting(Clans.getMinecraftHelper().getServer());
    }

    @Listener
    public void onServerStop(GameStoppingServerEvent event) {
        if(Clans.getMinecraftHelper() instanceof SpongeMinecraftHelper)
            ServerEventLogic.onServerStopping();
    }
}
