package the_fireplace.clans;

import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.nbt.NBTBase;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.config.Config;
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

    @Config(modid = MODID)
    public static class ConfigValues{
        //General clan config
        @Config.Comment("Allow clans to have multiple leaders.")
        public static boolean multipleClanLeaders = true;
        //Costs, rewards, and multipliers
        @Config.Comment("Cost of forming a clan. This requires a compatible economy to be installed.")
        @Config.RangeInt(min=0)
        public static int formClanCost = 0;
        @Config.Comment("Cost of claiming a chunk. This requires a compatible economy to be installed.")
        @Config.RangeInt(min=0)
        public static int claimChunkCost = 0;
        @Config.Comment("Cost of starting a raid. This requires a compatible economy to be installed.")
        @Config.RangeInt(min=0)
        public static int startRaidCost = 0;
        @Config.Comment("Multiply the cost of starting a raid by the number of enemy claims. This requires a compatible economy to be installed.")
        public static boolean startRaidMultiplier = true;
        @Config.Comment("Reward for winning a raid. This requires a compatible economy to be installed.")
        @Config.RangeInt(min=0)
        public static int winRaidAmount = 0;
        @Config.Comment("Multiply the reward for winning a raid by the number of enemy claims. This requires a compatible economy to be installed.")
        public static boolean winRaidMultiplier = true;
        @Config.Comment("Multiply the reward for winning a raid by the number of online enemy players. This requires a compatible economy to be installed.")
        public static boolean winRaidMultiplierPlayers = false;
        @Config.Comment("How often to charge clans upkeep(in days). Set to 0 to disable the need for upkeep. This requires a compatible economy to be installed.")
        @Config.RangeInt(min=0)
        public static int clanUpkeepDays = 0;
        @Config.Comment("Amount to charge a clan for upkeep. This requires a compatible economy to be installed.")
        @Config.RangeInt(min=0)
        public static int clanUpkeepCost = 0;
        @Config.Comment("Multiply the clan upkeep by the number of claims. This requires a compatible economy to be installed.")
        public static boolean multiplyUpkeepClaims = true;
        @Config.Comment("Multiply the clan upkeep by the number of members. This requires a compatible economy to be installed.")
        public static boolean multiplyUpkeepMembers = false;
        //Clan finance management
        @Config.Comment("Allow the clan leader to withdraw funds from the clan bank account. This requires a compatible economy to be installed.")
        public static boolean leaderWithdrawFunds = false;
        @Config.Comment("When enabled, remaining clan funds go to the clan leader when the clan is disbanded. When disabled, remaining clan funds get split evenly among all clan members when the clan is disbanded. This requires a compatible economy to be installed.")
        public static boolean leaderRecieveDisbandFunds = true;
        @Config.Comment("Frequency to charge clan members rent to go into the clan bank account (in days). If enabled, allows clan leaders to set the amount for their clans. Set to 0 to disable clan rent. This requires a compatible economy to be installed.")
        @Config.RangeInt(min=0)
        public static int chargeRentDays = 0;
        @Config.Comment("Kick clan members out who can't afford rent. This will not kick out leaders. This requires a compatible economy to be installed.")
        public static boolean evictNonpayers = false;
        @Config.Comment("Kick clan admins out who can't afford rent. This will not kick out leaders. This requires a compatible economy to be installed.")
        public static boolean evictNonpayerAdmins = false;
        @Config.Comment("Maximum amount of rent a clan can charge. Set to 0 for no maximum. This requires a compatible economy to be installed.")
        @Config.RangeInt(min=0)
        public static int maxRent = 0;
        @Config.Comment("Multiply the max rent by the number of claims. This requires a compatible economy to be installed.")
        public static boolean multiplyRentClaims = true;
    }
}
