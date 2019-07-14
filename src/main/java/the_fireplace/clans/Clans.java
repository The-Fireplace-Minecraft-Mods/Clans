package the_fireplace.clans;

import the_fireplace.clans.abstraction.IConfig;
import the_fireplace.clans.abstraction.IDynmapCompat;
import the_fireplace.clans.abstraction.IMinecraftHelper;
import the_fireplace.clans.abstraction.IPaymentHandler;
import the_fireplace.clans.abstraction.dummy.DynmapCompatDummy;
import the_fireplace.clans.abstraction.dummy.PaymentHandlerDummy;

public final class Clans {
    public static final String MODID = "clans";
    public static final String MODNAME = "ClansForge";
    public static final String VERSION = "${version}";
    private static IMinecraftHelper minecraftHelper;
    private static IPaymentHandler paymentHandler = new PaymentHandlerDummy();
    private static IConfig config;

    private static IDynmapCompat dynmapCompat = new DynmapCompatDummy();

    public static IMinecraftHelper getMinecraftHelper() {
        return minecraftHelper;
    }

    public static void setMinecraftHelper(IMinecraftHelper minecraftHelper) {
        Clans.minecraftHelper = minecraftHelper;
    }

    public static IPaymentHandler getPaymentHandler(){
        return paymentHandler;
    }

    public static void setPaymentHandler(IPaymentHandler paymentHandler) {
        Clans.paymentHandler = paymentHandler;
    }

    public static IConfig getConfig() {
        return config;
    }

    public static void setConfig(IConfig config) {
        Clans.config = config;
    }

    public static IDynmapCompat getDynmapCompat(){
        return dynmapCompat;
    }

    public static void setDynmapCompat(IDynmapCompat dynmapCompat) {
        Clans.dynmapCompat = dynmapCompat;
    }
}
