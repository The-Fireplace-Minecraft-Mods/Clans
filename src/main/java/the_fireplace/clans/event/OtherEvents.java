package the_fireplace.clans.event;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.commands.members.CommandLeave;
import the_fireplace.clans.util.CapHelper;
import the_fireplace.clans.util.PlayerClanCapability;
import the_fireplace.clans.util.TextStyles;

@Mod.EventBusSubscriber(modid = Clans.MODID)
public class OtherEvents {
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        //noinspection ConstantConditions
        assert Clans.CLAN_DATA_CAP != null;
        if(!event.player.world.isRemote && event.player instanceof EntityPlayerMP) {
            PlayerClanCapability c = CapHelper.getPlayerClanCapability(event.player);
            assert c != null;
            if ((c.getDefaultClan() != null && ClanCache.getClanById(c.getDefaultClan()) == null) || (c.getDefaultClan() == null && !ClanCache.getPlayerClans(event.player.getUniqueID()).isEmpty()))
                CommandLeave.updateDefaultClan((EntityPlayerMP)event.player, null);
        }
    }

    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
        if(Clans.cfg.showDefaultClanInChat && event.getPlayer() != null) {
            PlayerClanCapability playerClanCap = CapHelper.getPlayerClanCapability(event.getPlayer());
            if(playerClanCap != null && playerClanCap.getDefaultClan() != null) {
                Clan playerDefaultClan = ClanCache.getClanById(playerClanCap.getDefaultClan());
                if(playerDefaultClan != null)
                    event.setComponent(new TextComponentString('<'+playerDefaultClan.getClanName()+"> ").setStyle(TextStyles.GREEN).appendSibling(event.getComponent().setStyle(TextStyles.WHITE)));
                else
                    CommandLeave.updateDefaultClan(event.getPlayer(), null);
            }
        }
    }
}
