package the_fireplace.clans.client.mapinterceptor;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import the_fireplace.clans.client.ClansClientModContainer;
import the_fireplace.clans.legacy.logic.ClaimMapToChat;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = ClansClientModContainer.MODID)
public final class FancyMapInterceptor {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onChatReceived(ClientChatReceivedEvent event) {
        String line = event.getMessage().getUnformattedText();
        line = line.replaceAll(ClaimMapToChat.SECTION_SYMBOL+"[0-9a-g]", "");
        boolean processedLine = FancyMapProcessor.processLine(line);
        event.setCanceled(processedLine);
    }
}
