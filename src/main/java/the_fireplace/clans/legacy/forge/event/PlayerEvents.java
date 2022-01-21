package the_fireplace.clans.legacy.forge.event;

import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import the_fireplace.clans.legacy.ClansModContainer;
import the_fireplace.clans.legacy.cache.PlayerCache;
import the_fireplace.clans.legacy.logic.PlayerEventLogic;

@Mod.EventBusSubscriber(modid = ClansModContainer.MODID)
public class PlayerEvents
{
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        PlayerEventLogic.onPlayerLoggedIn(event.player);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        PlayerEventLogic.checkUpdateDefaultClan(event.player);
        PlayerEventLogic.onPlayerRespawn(event.player);
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        PlayerEventLogic.onPlayerLoggedOut(event.player.getUniqueID());
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        PlayerEventLogic.onPlayerChangedDimension(event.player);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onServerChat(ServerChatEvent event) {
        if (event.getPlayer() != null) {
            if (PlayerCache.isClanChatting(event.getPlayer().getUniqueID())) {
                event.setCanceled(true);
                PlayerEventLogic.sendClanChat(event.getPlayer(), event.getComponent());
            } else {
                event.setComponent(PlayerEventLogic.getPlayerChatMessage(event.getPlayer(), event.getComponent()));
            }
        }
    }

    @SubscribeEvent
    public static void breakSpeed(net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed event) {
        event.setNewSpeed(PlayerEventLogic.breakSpeed(event.getEntityPlayer(), event.getOriginalSpeed()));
    }
}
