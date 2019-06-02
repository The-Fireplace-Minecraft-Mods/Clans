package the_fireplace.clans.network;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import the_fireplace.clans.util.TranslationUtil;

import java.util.UUID;

/**
 * @author The_Fireplace
 */
public class ClansClientConnectedMessage implements IMessage {

    public String uuid;

    public ClansClientConnectedMessage() {
    }

    public ClansClientConnectedMessage(UUID uuid1) {
        uuid = uuid1.toString();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        uuid = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, uuid);
    }

    public static class Handler extends AbstractServerMessageHandler<ClansClientConnectedMessage> {
        @Override
        public IMessage handleServerMessage(ClansClientConnectedMessage message, MessageContext ctx) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() ->
                    TranslationUtil.clansClients.add(UUID.fromString(message.uuid)));
            return null;
        }
    }
}

