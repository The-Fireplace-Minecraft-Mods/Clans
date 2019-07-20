package the_fireplace.clans;

import the_fireplace.clans.abstraction.IConfig;
import the_fireplace.clans.abstraction.IDynmapCompat;
import the_fireplace.clans.abstraction.IMinecraftHelper;
import the_fireplace.clans.abstraction.IPaymentHandler;
import the_fireplace.clans.abstraction.dummy.DynmapCompatDummy;
import the_fireplace.clans.abstraction.dummy.PaymentHandlerDummy;
import the_fireplace.clans.util.PermissionManager;

public final class Clans {
    public static final String MODID = "clans";
    @SuppressWarnings("WeakerAccess")
    public static final String MODNAME = "Clans";
    static final String VERSION = "${version}";
    private static IMinecraftHelper minecraftHelper;
    private static IPaymentHandler paymentHandler = new PaymentHandlerDummy();
    private static IDynmapCompat dynmapCompat = new DynmapCompatDummy();
    private static IConfig config;

    public static IMinecraftHelper getMinecraftHelper() {
        return minecraftHelper;
    }

    public static IConfig getConfig() {
        return config;
    }

    public static IPaymentHandler getPaymentHandler(){
        return paymentHandler;
    }

    public static IDynmapCompat getDynmapCompat(){
        return dynmapCompat;
    }

    static void setMinecraftHelper(IMinecraftHelper minecraftHelper) {
        Clans.minecraftHelper = minecraftHelper;
    }

    static void setPaymentHandler(IPaymentHandler paymentHandler) {
        Clans.paymentHandler = paymentHandler;
    }

    static void setConfig(IConfig config) {
        Clans.config = config;
    }

    static void setDynmapCompat(IDynmapCompat dynmapCompat) {
        Clans.dynmapCompat = dynmapCompat;
    }

    static void initialize() {
        getDynmapCompat().init();
        PermissionManager.registerPermissionHandlers();
    }
}
