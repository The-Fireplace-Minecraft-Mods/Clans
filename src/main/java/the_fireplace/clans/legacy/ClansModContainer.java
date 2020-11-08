package the_fireplace.clans.legacy;

import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.*;
import org.apache.logging.log4j.Logger;
import the_fireplace.clans.economy.Economy;
import the_fireplace.clans.legacy.abstraction.*;
import the_fireplace.clans.legacy.abstraction.dummy.ChatCensorCompatDummy;
import the_fireplace.clans.legacy.abstraction.dummy.DynmapCompatDummy;
import the_fireplace.clans.legacy.config.Config;
import the_fireplace.clans.legacy.config.ConfigWrapper;
import the_fireplace.clans.legacy.forge.ForgePermissionHandler;
import the_fireplace.clans.legacy.forge.compat.ChatCensorCompat;
import the_fireplace.clans.legacy.forge.compat.DynmapCompat;
import the_fireplace.clans.legacy.forge.compat.ForgeMinecraftHelper;
import the_fireplace.clans.legacy.forge.compat.IceAndFireCompat;
import the_fireplace.clans.legacy.logic.ServerEventLogic;
import the_fireplace.clans.legacy.sponge.SpongePermissionHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

import static the_fireplace.clans.legacy.ClansModContainer.MODID;

@Mod.EventBusSubscriber(modid = MODID)
@Mod(modid = MODID, name = ClansModContainer.MODNAME, version = ClansModContainer.VERSION, acceptedMinecraftVersions = "[1.12,1.13)", acceptableRemoteVersions = "*", dependencies="after:grandeconomy;after:dynmap;after:spongeapi;required-after:forge@[14.23.5.2817,)")
public final class ClansModContainer {
    public static final String MODID = "clans";
    public static final String MODNAME = "Clans";
    public static final String VERSION = "${version}";
    private static final IConfig config = new ConfigWrapper();
    @Mod.Instance(MODID)
    public static ClansModContainer instance;

    private static Logger LOGGER = FMLLog.log;
    private static final ForgeMinecraftHelper minecraftHelper = new ForgeMinecraftHelper();
    private static IDynmapCompat dynmapCompat = new DynmapCompatDummy();
    private static IChatCensorCompat chatCensorCompat = new ChatCensorCompatDummy();
    private static final List<IProtectionCompat> protectionCompats = Lists.newArrayList();
    private static final IProtectionCompat protectionCompatManager = new IProtectionCompat() {
        @Override
        public void init() {
            for(IProtectionCompat compat: protectionCompats)
                compat.init();
        }

        @Override
        public boolean isOwnable(Entity entity) {
            for(IProtectionCompat compat: protectionCompats)
                if(compat.isOwnable(entity))
                    return true;
            return false;
        }

        @Nullable
        @Override
        public UUID getOwnerId(Entity entity) {
            for(IProtectionCompat compat: protectionCompats)
                if(compat.getOwnerId(entity) != null)
                    return compat.getOwnerId(entity);
            return null;
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
    private static IPermissionHandler permissionManager;

    public static Logger getLogger() {
        return LOGGER;
    }

    public static IConfig getConfig() {
        return config;
    }

    public static IPermissionHandler getPermissionManager() {
        return permissionManager;
    }

    private static void setPermissionManager(IPermissionHandler permissionManager) {
        ClansModContainer.permissionManager = permissionManager;
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER = event.getModLog();

        if(getMinecraftHelper().isPluginLoaded("dynmap"))
            dynmapCompat = new DynmapCompat();
        if(getMinecraftHelper().isPluginLoaded("chatcensor"))
            chatCensorCompat = new ChatCensorCompat();
        if(getMinecraftHelper().isPluginLoaded("iceandfire"))
            addProtectionCompat(new IceAndFireCompat());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event){
        getDynmapCompat().init();
        getProtectionCompat().init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event){
        Economy.detectAndUseExternalEconomy();
        if(getMinecraftHelper().isPluginLoaded("spongeapi") && !Config.getInstance().general.forgePermissionPrecedence)
            setPermissionManager(new SpongePermissionHandler());
        else
            setPermissionManager(new ForgePermissionHandler());
    }

    @Mod.EventHandler
    public void onServerStart(FMLServerStartingEvent event) {
        ServerEventLogic.onServerStarting(event.getServer());
    }

    @Mod.EventHandler
    public void onServerStop(FMLServerStoppingEvent event) {
        ServerEventLogic.onServerStopping();
    }

    public static ForgeMinecraftHelper getMinecraftHelper() {
        return minecraftHelper;
    }

    public static IProtectionCompat getProtectionCompat() {
        return protectionCompatManager;
    }

    public static void addProtectionCompat(IProtectionCompat compat) {
        protectionCompats.add(compat);
    }

    public static IDynmapCompat getDynmapCompat(){
        return dynmapCompat;
    }

    public static IChatCensorCompat getChatCensorCompat(){
        return chatCensorCompat;
    }
}
