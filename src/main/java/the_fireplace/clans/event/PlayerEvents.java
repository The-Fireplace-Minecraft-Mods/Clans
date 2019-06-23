package the_fireplace.clans.event;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import the_fireplace.clans.Clans;
import the_fireplace.clans.util.PlayerPositionCache;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.ClanDatabase;
import the_fireplace.clans.util.CapHelper;
import the_fireplace.clans.util.Pair;
import the_fireplace.clans.util.PlayerClanCapability;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

@Mod.EventBusSubscriber(modid = Clans.MODID)
public class PlayerEvents {
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        checkUpdateDefaultClan(event);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        checkUpdateDefaultClan(event);
    }

    private static void checkUpdateDefaultClan(PlayerEvent event) {
        //noinspection ConstantConditions
        assert Clans.CLAN_DATA_CAP != null;
        if(!event.player.world.isRemote && event.player instanceof EntityPlayerMP) {
            PlayerClanCapability c = CapHelper.getPlayerClanCapability(event.player);
            assert c != null;
            if ((c.getDefaultClan() != null && ClanCache.getClanById(c.getDefaultClan()) == null) || (c.getDefaultClan() == null && !ClanCache.getPlayerClans(event.player.getUniqueID()).isEmpty()) || (c.getDefaultClan() != null && !ClanCache.getPlayerClans(event.player.getUniqueID()).contains(ClanCache.getClanById(c.getDefaultClan()))))
                PlayerClanCapability.updateDefaultClan((EntityPlayerMP)event.player, null);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if(CapHelper.getPlayerClanCapability(event.player).getClaimWarning()) {
            CapHelper.getPlayerClanCapability(event.player).setClaimWarning(false);
            PlayerPositionCache.prevChunkXs.remove(event.player);
            PlayerPositionCache.prevChunkZs.remove(event.player);
            ClanCache.opAutoClaimLands.remove(event.player.getUniqueID());
            ClanCache.opAutoAbandonClaims.remove(event.player.getUniqueID());
            ClanCache.autoAbandonClaims.remove(event.player.getUniqueID());
            ClanCache.autoClaimLands.remove(event.player.getUniqueID());
            ClanCache.clanChattingPlayers.remove(event.player.getUniqueID());
        }
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if(CapHelper.getPlayerClanCapability(event.player).getClaimWarning()) {
            CapHelper.getPlayerClanCapability(event.player).setClaimWarning(false);
            PlayerPositionCache.prevChunkXs.remove(event.player);
            PlayerPositionCache.prevChunkZs.remove(event.player);
            Pair<Clan, Boolean> ocAutoClaim = ClanCache.opAutoClaimLands.remove(event.player.getUniqueID());
            Boolean ocAutoAbandon = ClanCache.opAutoAbandonClaims.remove(event.player.getUniqueID());
            Clan cAutoAbandon = ClanCache.autoAbandonClaims.remove(event.player.getUniqueID());
            Clan cAutoClaim = ClanCache.autoClaimLands.remove(event.player.getUniqueID());
            if(ocAutoAbandon != null) {
                if (ocAutoAbandon)
                    event.player.sendMessage(TranslationUtil.getTranslation(event.player.getUniqueID(), "commands.opclan.autoabandon.stop").setStyle(TextStyles.GREEN));
                else
                    event.player.sendMessage(TranslationUtil.getTranslation(event.player.getUniqueID(), "commands.clan.autoabandon.stop", ClanDatabase.getOpClan()).setStyle(TextStyles.GREEN));
            }
            if(cAutoAbandon != null)
                event.player.sendMessage(TranslationUtil.getTranslation(event.player.getUniqueID(), "commands.clan.autoabandon.stop", cAutoAbandon.getClanName()).setStyle(TextStyles.GREEN));
            if(ocAutoClaim != null)
                event.player.sendMessage(TranslationUtil.getTranslation(event.player.getUniqueID(), "commands.clan.autoclaim.stop", ocAutoClaim.getValue1().getClanName()).setStyle(TextStyles.GREEN));
            if(cAutoClaim != null)
                event.player.sendMessage(TranslationUtil.getTranslation(event.player.getUniqueID(), "commands.clan.autoclaim.stop", cAutoClaim.getClanName()).setStyle(TextStyles.GREEN));
        }
    }

    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
        if(Clans.cfg.showDefaultClanInChat && event.getPlayer() != null) {
            PlayerClanCapability playerClanCap = CapHelper.getPlayerClanCapability(event.getPlayer());
            if(playerClanCap != null && playerClanCap.getDefaultClan() != null) {
                Clan playerDefaultClan = ClanCache.getClanById(playerClanCap.getDefaultClan());
                if(playerDefaultClan != null)
                    event.setComponent(new TextComponentString('<'+playerDefaultClan.getClanName()+"> ").setStyle(new Style().setColor(playerDefaultClan.getTextColor())).appendSibling(event.getComponent().setStyle(TextStyles.RESET)));
                else
                    PlayerClanCapability.updateDefaultClan(event.getPlayer(), null);
            }
        }
    }
}
