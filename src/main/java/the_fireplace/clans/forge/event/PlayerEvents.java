package the_fireplace.clans.forge.event;

import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import the_fireplace.clans.Clans;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.data.PlayerDataManager;
import the_fireplace.clans.forge.legacy.CapHelper;
import the_fireplace.clans.forge.legacy.PlayerClanCapability;
import the_fireplace.clans.logic.PlayerEventLogic;

@Mod.EventBusSubscriber(modid = Clans.MODID)
public class PlayerEvents {
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        //TODO remove in 1.4
        PlayerClanCapability c = CapHelper.getPlayerClanCapability(event.player);
        if(c.getDefaultClan() != null) {
            PlayerDataManager.setDefaultClan(event.player.getUniqueID(), c.getDefaultClan());
            c.setDefaultClan(null);
        }
        if(c.getCooldown() != 0) {
            PlayerDataManager.setCooldown(event.player.getUniqueID(), c.getCooldown());
            c.setCooldown(0);
        }
        PlayerEventLogic.onPlayerLoggedIn(event.player);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        PlayerEventLogic.checkUpdateDefaultClan(event.player);
        if(event.player.bedLocation == null)
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

    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
        if(event.getPlayer() != null) {
            if(ClanCache.getClanChattingPlayers().containsKey(event.getPlayer().getUniqueID())) {
                event.setCanceled(true);
                PlayerEventLogic.sendClanChat(event.getPlayer(), event.getComponent());
            } else if (Clans.getConfig().isShowDefaultClanInChat()) {
                event.setComponent(PlayerEventLogic.getPlayerChatMessage(event.getPlayer(), event.getComponent()));
            }
        }
    }
}
