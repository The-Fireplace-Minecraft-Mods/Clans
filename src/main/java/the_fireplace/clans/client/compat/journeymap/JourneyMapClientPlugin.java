package the_fireplace.clans.client.compat.journeymap;

import journeymap.client.api.ClientPlugin;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.IClientPlugin;
import journeymap.client.api.event.ClientEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import the_fireplace.clans.client.ClansClientModContainer;

import javax.annotation.ParametersAreNonnullByDefault;

@SideOnly(Side.CLIENT)
@ClientPlugin
@ParametersAreNonnullByDefault
public class JourneyMapClientPlugin implements IClientPlugin {

    @Override
    public void initialize(IClientAPI iClientAPI) {
        MinecraftForge.EVENT_BUS.register(new ClaimRequester());
        MinecraftForge.EVENT_BUS.register(new OverlayTracker(iClientAPI));
    }

    @Override
    public String getModId() {
        return ClansClientModContainer.MODID;
    }

    @Override
    public void onEvent(ClientEvent clientEvent) {

    }
}
