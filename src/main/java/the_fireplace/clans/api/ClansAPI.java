package the_fireplace.clans.api;

import the_fireplace.clans.api.event.IClansEventHandler;
import the_fireplace.clans.legacy.ClansModContainer;
import the_fireplace.clans.legacy.abstraction.IConfig;
import the_fireplace.clans.legacy.api.ClaimAccessor;
import the_fireplace.clans.legacy.data.ClaimData;
import the_fireplace.clans.legacy.util.ClansEventManager;

/**
 * Several useful places to look for data are:
 * {@link the_fireplace.clans.clan}
 * {@link the_fireplace.clans.player}
 * {@link ClaimData}
 */
public final class ClansAPI
{
    public static IConfig getConfig() {
        return ClansModContainer.getConfig();
    }

    public static boolean isClaimed(int chunkX, int chunkZ, int dimension) {
        return ClaimAccessor.getInstance().getChunkClan(chunkX, chunkZ, dimension) != null;
    }

    public static <V> void registerEvent(Class<V> eventType, IClansEventHandler<V> handler) {
        ClansEventManager.registerEvent(eventType, handler);
    }

    public static <V> void unregisterEvent(Class<V> eventType, IClansEventHandler<V> handler) {
        ClansEventManager.unregisterEvent(eventType, handler);
    }
}
