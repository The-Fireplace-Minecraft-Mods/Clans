package the_fireplace.clans.client;

import net.minecraftforge.fml.common.Mod;
import the_fireplace.clans.legacy.ClansModContainer;

import static the_fireplace.clans.client.ClansClientModContainer.MODID;

@Mod.EventBusSubscriber(modid = MODID)
@Mod(modid = MODID, name = ClansModContainer.MODNAME, version = ClansModContainer.VERSION, acceptedMinecraftVersions = "[1.12,1.13)", clientSideOnly = true)
public class ClansClientModContainer {
    public static final String MODID = "clansclient";


}
