package the_fireplace.clans.event;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.commands.CommandClan;
import the_fireplace.clans.util.CapHelper;
import the_fireplace.clans.util.PlayerClanCapability;

public class OtherEvents {
    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        //noinspection ConstantConditions
        assert Clans.CLAN_DATA_CAP != null;
        if(!event.getPlayer().world.isRemote && event.getPlayer() instanceof EntityPlayerMP && event.getPlayer().getCapability(Clans.CLAN_DATA_CAP).isPresent()) {
            PlayerClanCapability c = CapHelper.getPlayerClanCapability(event.getPlayer());
            if ((c.getDefaultClan() != null && ClanCache.getClan(c.getDefaultClan()) == null) || (c.getDefaultClan() == null && !ClanCache.getPlayerClans(event.getPlayer().getUniqueID()).isEmpty()))
                CommandClan.updateDefaultClan((EntityPlayerMP)event.getPlayer(), null);
        }
    }
}
