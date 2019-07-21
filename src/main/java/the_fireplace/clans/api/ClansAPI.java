package the_fireplace.clans.api;

import the_fireplace.clans.Clans;
import the_fireplace.clans.abstraction.IConfig;
import the_fireplace.clans.abstraction.IMinecraftHelper;
import the_fireplace.clans.abstraction.IPaymentHandler;
import the_fireplace.clans.api.event.IClansEventHandler;
import the_fireplace.clans.util.ClansEventManager;


/**
 * Several useful files for accessing data are:
 * {@link the_fireplace.clans.cache.ClanCache}
 * {@link the_fireplace.clans.data.PlayerDataManager}
 * {@link the_fireplace.clans.data.ClaimDataManager}
 */
public final class ClansAPI {
    public static IMinecraftHelper getMinecraftHelper() {
        return Clans.getMinecraftHelper();
    }

    public static IConfig getConfig() {
        return Clans.getConfig();
    }

    public static IPaymentHandler getPaymentHandler(){
        return Clans.getPaymentHandler();
    }

    public static <V> void registerEvent(Class<V> eventType, IClansEventHandler<V> handler) {
        ClansEventManager.registerEvent(eventType, handler);
    }

    public static <V> void unregisterEvent(Class<V> eventType, IClansEventHandler<V> handler) {
        ClansEventManager.unregisterEvent(eventType, handler);
    }
}
