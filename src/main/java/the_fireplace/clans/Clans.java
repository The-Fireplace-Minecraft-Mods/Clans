package the_fireplace.clans;

import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import the_fireplace.clans.commands.*;
import the_fireplace.clans.payment.IPaymentHandler;
import the_fireplace.clans.payment.PaymentHandlerDummy;
import the_fireplace.clans.payment.PaymentHandlerGE;

@SuppressWarnings("WeakerAccess")
@Mod(modid = Clans.MODID, name = Clans.MODNAME, version = Clans.VERSION, acceptedMinecraftVersions = "[1.12,1.13)", acceptableRemoteVersions = "*")
public final class Clans {
    public static final String MODID = "clans";
    public static final String MODNAME = "Clans";
    public static final String VERSION = "${version}";
    @Mod.Instance(MODID)
    public static Clans instance;

    private IPaymentHandler paymentHandler;
    public static IPaymentHandler getPaymentHandler(){
        return instance.paymentHandler;
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event){
        if(Loader.isModLoaded("grandeconomy"))
            paymentHandler = new PaymentHandlerGE();
        else
            paymentHandler = new PaymentHandlerDummy();
    }

    @Mod.EventHandler
    public void onServerStart(FMLServerStartingEvent event) {
        MinecraftServer server = event.getServer();
        ICommandManager command = server.getCommandManager();
        ServerCommandManager manager = (ServerCommandManager) command;
        manager.registerCommand(new CommandClan());
    }
}
