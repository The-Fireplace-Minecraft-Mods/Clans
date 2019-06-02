package the_fireplace.clans.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import the_fireplace.clans.Clans;

/**
 * @author coolAlias
 * @author The_Fireplace
 */
public final class PacketDispatcher {
    private static byte packetId = 0;

    private static final SimpleNetworkWrapper dispatcher = NetworkRegistry.INSTANCE.newSimpleChannel(Clans.MODID);

    public static void registerPackets() {
        PacketDispatcher.registerMessage(ClansClientConnectedMessage.Handler.class, ClansClientConnectedMessage.class, Side.SERVER);
    }

    @SuppressWarnings("unchecked")
    private static void registerMessage(Class handlerClass, Class messageClass, Side targetSide) {
        PacketDispatcher.dispatcher.registerMessage(handlerClass, messageClass, packetId++, targetSide);
    }

    //Wrapper methods
    /*public static void sendTo(IMessage message, EntityPlayerMP player) {
        PacketDispatcher.dispatcher.sendTo(message, player);
    }

    public static void sendToAll(IMessage message) {
        PacketDispatcher.dispatcher.sendToAll(message);
    }

    public static void sendToAllAround(IMessage message, NetworkRegistry.TargetPoint point) {
        PacketDispatcher.dispatcher.sendToAllAround(message, point);
    }

    public static void sendToAllAround(IMessage message, int dimension, double x, double y, double z, double range) {
        PacketDispatcher.dispatcher.sendToAllAround(message, new NetworkRegistry.TargetPoint(dimension, x, y, z, range));
    }

    public static void sendToAllAround(IMessage message, EntityPlayer player, double range) {
        PacketDispatcher.dispatcher.sendToAllAround(message, new NetworkRegistry.TargetPoint(player.world.provider.getDimension(), player.posX, player.posY, player.posZ, range));
    }

    public static void sendToDimension(IMessage message, int dimensionId) {
        PacketDispatcher.dispatcher.sendToDimension(message, dimensionId);
    }*/

    public static void sendToServer(IMessage message) {
        dispatcher.sendToServer(message);
    }
}