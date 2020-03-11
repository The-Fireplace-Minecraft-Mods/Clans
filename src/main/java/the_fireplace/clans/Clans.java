package the_fireplace.clans;

import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import the_fireplace.clans.abstraction.*;
import the_fireplace.clans.abstraction.dummy.DynmapCompatDummy;
import the_fireplace.clans.abstraction.dummy.PaymentHandlerDummy;

import javax.annotation.Nullable;
import java.util.List;

public final class Clans {
    public static final String MODID = "clans";
    @SuppressWarnings("WeakerAccess")
    public static final String MODNAME = "Clans";
    static final String VERSION = "${version}";
    private static IMinecraftHelper minecraftHelper;
    private static IPaymentHandler paymentHandler = new PaymentHandlerDummy();
    private static IDynmapCompat dynmapCompat = new DynmapCompatDummy();
    private static IConfig config;
    private static IPermissionHandler permissionManager;
    private static List<IProtectionCompat> protectionCompats = Lists.newArrayList();
    private static IProtectionCompat protectionCompatManager = new IProtectionCompat() {
        @Override
        public void init() {
            for(IProtectionCompat compat: protectionCompats)
                compat.init();
        }

        @Override
        public boolean isMob(Entity entity) {
            for(IProtectionCompat compat: protectionCompats)
                if(compat.isMob(entity))
                    return true;
            return false;
        }

        @Override
        public boolean isContainer(World world, BlockPos pos, @Nullable IBlockState state, @Nullable TileEntity tileEntity) {
            for(IProtectionCompat compat: protectionCompats)
                if(compat.isContainer(world, pos, state, tileEntity))
                    return true;
            return false;
        }
    };

    public static IMinecraftHelper getMinecraftHelper() {
        return minecraftHelper;
    }

    public static IConfig getConfig() {
        return config;
    }

    public static IProtectionCompat getProtectionCompat() {
        return protectionCompatManager;
    }

    public static void addProtectionCompat(IProtectionCompat compat) {
        protectionCompats.add(compat);
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
    }

    public static IPermissionHandler getPermissionManager() {
        return permissionManager;
    }

    static void setPermissionManager(IPermissionHandler permissionManager) {
        Clans.permissionManager = permissionManager;
    }
}
