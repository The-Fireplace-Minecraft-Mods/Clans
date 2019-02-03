package the_fireplace.clans;

import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.nbt.NBTBase;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import the_fireplace.clans.clan.ClaimedLandCapability;
import the_fireplace.clans.commands.*;
import the_fireplace.clans.payment.IPaymentHandler;
import the_fireplace.clans.payment.PaymentHandlerDummy;
import the_fireplace.clans.payment.PaymentHandlerGE;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static the_fireplace.clans.Clans.MODID;

@SuppressWarnings("WeakerAccess")
@Mod.EventBusSubscriber(modid = MODID)
@Mod(modid = MODID, name = Clans.MODNAME, version = Clans.VERSION, acceptedMinecraftVersions = "[1.12,1.13)", acceptableRemoteVersions = "*")
public final class Clans {
    public static final String MODID = "clans";
    public static final String MODNAME = "Clans";
    public static final String VERSION = "${version}";
    @Mod.Instance(MODID)
    public static Clans instance;

    @CapabilityInject(ClaimedLandCapability.class)
    public static final Capability<ClaimedLandCapability> CLAIMED_LAND = null;

    private IPaymentHandler paymentHandler;
    public static IPaymentHandler getPaymentHandler(){
        return instance.paymentHandler;
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event){
        CapabilityManager.INSTANCE.register(ClaimedLandCapability.class, new ClaimedLandCapability.Storage(), ClaimedLandCapability.Default::new);
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
        manager.registerCommand(new CommandOpClan());
        manager.registerCommand(new CommandRaid());
    }

    @SubscribeEvent
    public static void attachChunkCaps(AttachCapabilitiesEvent<Chunk> e){
        //noinspection ConstantConditions
        assert CLAIMED_LAND != null;
        e.addCapability(new ResourceLocation("clans", "claimData"), new ICapabilitySerializable() {
            ClaimedLandCapability inst = CLAIMED_LAND.getDefaultInstance();

            @Override
            public NBTBase serializeNBT() {
                return CLAIMED_LAND.getStorage().writeNBT(CLAIMED_LAND, inst, null);
            }

            @Override
            public void deserializeNBT(NBTBase nbt) {
                CLAIMED_LAND.getStorage().readNBT(CLAIMED_LAND, inst, null, nbt);
            }

            @Override
            public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
                return capability == CLAIMED_LAND;
            }

            @Nullable
            @Override
            public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
                //noinspection unchecked
                return (T) inst;
            }
        });
    }
}
