package the_fireplace.clans;

import the_fireplace.clans.abstraction.*;
import the_fireplace.clans.abstraction.dummy.PaymentHandlerDummy;

@SuppressWarnings("WeakerAccess")
public final class ClansHelper {
    public static final String MODID = "clans";
    public static final String MODNAME = "Clans";
    public static final String VERSION = "${version}";
    private static IPaymentHandler paymentHandler = new PaymentHandlerDummy();
    private static IConfig config;
    private static IPermissionHandler permissionManager;

    public static IConfig getConfig() {
        return config;
    }

    public static IPaymentHandler getPaymentHandler(){
        return paymentHandler;
    }

    static void setPaymentHandler(IPaymentHandler paymentHandler) {
        ClansHelper.paymentHandler = paymentHandler;
    }

    static void setConfig(IConfig config) {
        ClansHelper.config = config;
    }

    static void setDynmapCompat(IDynmapCompat dynmapCompat) {
        Clans.dynmapCompat = dynmapCompat;
    }

    static void initialize() {
        Clans.getDynmapCompat().init();
    }

    public static IPermissionHandler getPermissionManager() {
        return permissionManager;
    }

    static void setPermissionManager(IPermissionHandler permissionManager) {
        ClansHelper.permissionManager = permissionManager;
    }
}
