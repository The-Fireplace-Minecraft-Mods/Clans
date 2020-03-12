package the_fireplace.clans.api;

import the_fireplace.clans.ClansHelper;
import the_fireplace.clans.abstraction.IConfig;
import the_fireplace.clans.abstraction.IMinecraftHelper;
import the_fireplace.clans.abstraction.IPaymentHandler;
import the_fireplace.clans.api.event.IClansEventHandler;
import the_fireplace.clans.data.ClaimData;
import the_fireplace.clans.data.PlayerData;
import the_fireplace.clans.util.ClansEventManager;

/**
 * Several useful files for accessing data are:
 * {@link the_fireplace.clans.cache.ClanCache}
 * {@link PlayerData}
 * {@link ClaimData}
 */
public final class ClansAPI {
    public static IConfig getConfig() {
        return ClansHelper.getConfig();
    }

    public static IPaymentHandler getPaymentHandler(){
        return ClansHelper.getPaymentHandler();
    }

    public static boolean isClaimed(int chunkX, int chunkZ, int dimension) {
        return ClaimData.getChunkClan(chunkX, chunkZ, dimension) != null;
    }

    public static <V> void registerEvent(Class<V> eventType, IClansEventHandler<V> handler) {
        ClansEventManager.registerEvent(eventType, handler);
    }

    public static <V> void unregisterEvent(Class<V> eventType, IClansEventHandler<V> handler) {
        ClansEventManager.unregisterEvent(eventType, handler);
    }
}
