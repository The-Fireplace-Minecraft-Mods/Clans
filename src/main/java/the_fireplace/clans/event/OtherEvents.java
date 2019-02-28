package the_fireplace.clans.event;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.commands.members.CommandLeave;
import the_fireplace.clans.util.CapHelper;
import the_fireplace.clans.util.PlayerClanCapability;

@Mod.EventBusSubscriber(modid = Clans.MODID)
public class OtherEvents {
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        //noinspection ConstantConditions
        assert Clans.CLAN_DATA_CAP != null;
        if(!event.getPlayer().world.isRemote && event.getPlayer() instanceof EntityPlayerMP && event.getPlayer().getCapability(Clans.CLAN_DATA_CAP).isPresent()) {
            PlayerClanCapability c = CapHelper.getPlayerClanCapability(event.getPlayer());
            if ((c.getDefaultClan() != null && ClanCache.getClan(c.getDefaultClan()) == null) || (c.getDefaultClan() == null && !ClanCache.getPlayerClans(event.getPlayer().getUniqueID()).isEmpty()))
                CommandLeave.updateDefaultClan((EntityPlayerMP)event.getPlayer(), null);
        }
    }
}
