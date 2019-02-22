package the_fireplace.clans.event;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.commands.members.CommandLeave;
import the_fireplace.clans.util.PlayerClanCapability;

@Mod.EventBusSubscriber(modid = Clans.MODID)
public class OtherEvents {
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        //noinspection ConstantConditions
        assert Clans.CLAN_DATA_CAP != null;
        if(!event.player.world.isRemote && event.player instanceof EntityPlayerMP && event.player.hasCapability(Clans.CLAN_DATA_CAP, null)) {
            PlayerClanCapability c = event.player.getCapability(Clans.CLAN_DATA_CAP, null);
            assert c != null;
            if (c.getDefaultClan() != null && ClanCache.getClan(c.getDefaultClan()) == null)
                CommandLeave.updateDefaultClan((EntityPlayerMP)event.player, null);
        }
    }
}
