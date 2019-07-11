package the_fireplace.clans;

import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Logger;
import the_fireplace.clans.legacy.ClaimedLandCapability;
import the_fireplace.clans.data.ClanChunkData;
import the_fireplace.clans.data.ClanDatabase;
import the_fireplace.clans.commands.CommandClan;
import the_fireplace.clans.commands.CommandOpClan;
import the_fireplace.clans.commands.CommandRaid;
import the_fireplace.clans.compat.DynmapCompat;
import the_fireplace.clans.abstraction.dummy.DynmapCompatDummy;
import the_fireplace.clans.abstraction.IDynmapCompat;
import the_fireplace.clans.abstraction.IPaymentHandler;
import the_fireplace.clans.abstraction.dummy.PaymentHandlerDummy;
import the_fireplace.clans.compat.PaymentHandlerGE;
import the_fireplace.clans.permissions.PermissionManager;
import the_fireplace.clans.data.RaidBlockPlacementDatabase;
import the_fireplace.clans.data.RaidRestoreDatabase;
import the_fireplace.clans.legacy.PlayerClanCapability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static the_fireplace.clans.Clans.MODID;

@SuppressWarnings({"WeakerAccess"})
@Mod.EventBusSubscriber(modid = MODID)
@Mod(modid = MODID, name = Clans.MODNAME, version = Clans.VERSION, acceptedMinecraftVersions = "[1.12,1.13)", acceptableRemoteVersions = "*", dependencies="after:grandeconomy;after:dynmap;after:spongeapi")
public final class Clans {
    public static final String MODID = "clans";
    public static final String MODNAME = "Clans";
    public static final String VERSION = "${version}";
    @Mod.Instance(MODID)
    public static Clans instance;

    public static Logger LOGGER = FMLLog.log;

    @CapabilityInject(ClaimedLandCapability.class)
    public static final Capability<ClaimedLandCapability> CLAIMED_LAND = null;
    private static final ResourceLocation claimed_land_res = new ResourceLocation(MODID, "claimData");
    @CapabilityInject(PlayerClanCapability.class)
    public static final Capability<PlayerClanCapability> CLAN_DATA_CAP = null;
    private static final ResourceLocation clan_home_res = new ResourceLocation(MODID, "homeCooldownData");

    private IPaymentHandler paymentHandler;
    public static IPaymentHandler getPaymentHandler(){
        return instance.paymentHandler;
    }

    private IDynmapCompat dynmapCompat;
    public static IDynmapCompat getDynmapCompat(){
        return instance.dynmapCompat;
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event){
        CapabilityManager.INSTANCE.register(ClaimedLandCapability.class, new ClaimedLandCapability.Storage(), ClaimedLandCapability.Default::new);
        CapabilityManager.INSTANCE.register(PlayerClanCapability.class, new PlayerClanCapability.Storage(), PlayerClanCapability.Default::new);
        LOGGER = event.getModLog();
        if(Loader.isModLoaded("dynmap"))
            dynmapCompat = new DynmapCompat();
        else
            dynmapCompat = new DynmapCompatDummy();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event){
        dynmapCompat.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event){
        if(Loader.isModLoaded("grandeconomy"))
            paymentHandler = new PaymentHandlerGE();
        else
            paymentHandler = new PaymentHandlerDummy();
        PermissionManager.registerPermissionHandlers();
    }

    @Mod.EventHandler
    public void onServerStart(FMLServerStartingEvent event) {
        MinecraftServer server = event.getServer();
        ICommandManager command = server.getCommandManager();
        ServerCommandManager manager = (ServerCommandManager) command;
        manager.registerCommand(new CommandClan());
        manager.registerCommand(new CommandOpClan());
        manager.registerCommand(new CommandRaid());
        dynmapCompat.serverStart();
    }

    @Mod.EventHandler
    public void onServerStop(FMLServerStoppingEvent event) {
        ClanChunkData.save();
        ClanDatabase.save();
        RaidRestoreDatabase.save();
        RaidBlockPlacementDatabase.save();
    }

    @SubscribeEvent
    public static void attachChunkCaps(AttachCapabilitiesEvent<Chunk> e){
        attachClanTagCap(e);
    }

    @SubscribeEvent
    public static void attachPlayerCaps(AttachCapabilitiesEvent<Entity> e){
        if(e.getObject() instanceof EntityPlayer) {
            attachClanTagCap(e);
            //noinspection ConstantConditions
            assert CLAN_DATA_CAP != null;
            e.addCapability(clan_home_res, new ICapabilitySerializable() {
                PlayerClanCapability inst = CLAN_DATA_CAP.getDefaultInstance();

                @Override
                public NBTBase serializeNBT() {
                    return CLAN_DATA_CAP.getStorage().writeNBT(CLAN_DATA_CAP, inst, null);
                }

                @Override
                public void deserializeNBT(NBTBase nbt) {
                    CLAN_DATA_CAP.getStorage().readNBT(CLAN_DATA_CAP, inst, null, nbt);
                }

                @Override
                public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
                    return capability == CLAN_DATA_CAP;
                }

                @Nullable
                @Override
                public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
                    //noinspection unchecked
                    return capability == CLAN_DATA_CAP ? (T) inst : null;
                }
            });
        }
    }

    private static void attachClanTagCap(AttachCapabilitiesEvent e) {
        //noinspection ConstantConditions
        assert CLAIMED_LAND != null;
        e.addCapability(claimed_land_res, new ICapabilitySerializable() {
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
                return capability == CLAIMED_LAND ? (T) inst : null;
            }
        });
    }

    @Config(modid = MODID)
    public static class cfg {
        //General clan config
        @Config.Comment("Allow clans to have multiple leaders.")
        public static boolean multipleClanLeaders = true;
        @Config.Comment("Maximum clan name length. Larger values allow more characters to be typed for the clan name, but also increase the chance of clans making their name hard to type to avoid getting raided. Set to 0 for no limit.")
        @Config.RangeInt(min=0)
        public static int maxNameLength = 32;
        @Config.Comment("Allow players to be a member of multiple clans at once.")
        public static boolean allowMultiClanMembership = true;
        @Config.Comment("The amount of time, in seconds, the player must wait after typing /clan home before being teleported. Set to -1 to disable /clan home.")
        @Config.RangeInt(min=-1)
        public static int clanHomeWarmupTime = 0;
        @Config.Comment("The amount of time, in seconds, the player must wait after teleporting to the clan home before they can use /clan home again.")
        @Config.RangeInt(min=0)
        public static int clanHomeCooldownTime = 0;
        @Config.Comment("Max claims per player per clan. Set to 0 for infinite.")
        @Config.RangeInt(min=0)
        public static int maxClanPlayerClaims = 0;
        @Config.Comment("Show player's default clan in chat.")
        public static boolean showDefaultClanInChat = true;
        @Config.Comment("Server locale - the client's locale takes precedence if Clans is installed there.")
        public static String locale = "en_us";
        @Config.Comment("A list of Fake Players that are allowed to build, destroy, interact, and injure things on claimed land. These are typically machines added by mods. If it contains a *, this list is a blacklist, so everything is allowed by default. Otherwise, it is a whitelist.")
        public static String[] tolerableFakePlayers = {"*"};
        @Config.Comment("Dump FakePlayer names to the console/log as they're discovered. Used to find out what to use to enable/disable a FakePlayer in tolerableFakePlayers")
        public static boolean fakePlayerDump = false;
        @Config.Comment("Whether Forge takes precedence over Sponge when finding permissions. Set this to true if your permissions manager uses Forge.")
        public static boolean forgePermissionPrecedence = false;
        //Clan guard
        @Config.Comment("Minimum number of blocks between clan homes.")
        @Config.RangeInt(min=0)
        public static int minClanHomeDist = 320;
        @Config.Comment("This multiplied by the minimum clan home distance is how far away from other clans' homes to make the initial claim for a clan.")
        @Config.RangeDouble(min=0)
        public static double initialClaimSeparationMultiplier = 1.25;
        @Config.Comment("If set to false, players will be warned if making the initial claim within the claim separation range, but not prevented from making the claim if they want to.")
        public static boolean enforceInitialClaimSeparation = true;
        @Config.Comment("Force clans to have connected claims.")
        public static boolean forceConnectedClaims = true;
        @Config.Comment("What algorithm is used for the connected claim check. 'quick' is generally quicker, but may falsely prevent claim abandonment. 'smart' is generally slower, but enforces connected claims the best.")
        public static String connectedClaimCheck = "smart";
        @Config.Comment("Prevents other clans from claiming around the edges of a clan's territory. When a chunk of land is between two clans' territories, the first clan to be near it takes precedence.")
        public static boolean enableBorderlands = true;
        @Config.Comment("Prevent mobs from spawning on claimed land")
        public static boolean preventMobsOnClaims = true;
        @Config.Comment("Prevents mobs from spawning in borderlands. Requires enableBorderlands and preventMobsOnClaims.")
        public static boolean preventMobsOnBorderlands = true;
        @Config.Comment("Maximum number of claims per clan. Set to 0 for unlimited.")
        @Config.RangeInt(min=0)
        public static int maxClaims = 0;
        @Config.Comment("If enabled, multiplies the max claim count by the number of players in the clan.")
        public static boolean multiplyMaxClaimsByPlayers = true;
        //Wilderness guard
        @Config.Comment("Protect the wilderness from damage above a specific Y level")
        public static boolean protectWilderness = true;
        @Config.Comment("Protect borderland from the members of the clan it borders. Requires protectWilderness and enableBorderlands.")
        public static boolean protectEdgeTerritory = false;
        @Config.Comment("Minimum Y level to protect with the Protect Wilderness option, inclusive. Set to a negative number to use sea level.")
        public static int minWildernessY = -1;
        @Config.Comment("Always allow TNT to ignite other TNT, regardless of whether other blocks are protected.")
        public static boolean chainTNT = true;
        //Raid configuration
        @Config.Comment("Offset the maximum number of raiders by this much when determining how many people can join a raiding party. Formula is: (# raiders) - (maxRaiderOffset) <= (# defenders)")
        public static int maxRaidersOffset = 0;
        @Config.Comment("Maximum duration a raid can last for, in minutes. If set to 0, raids will be disabled.")
        @Config.RangeInt(min=0,max=Integer.MAX_VALUE/60)
        public static int maxRaidDuration = 30;
        @Config.Comment("The amount of time the defenders are given to prepare for a raid, in seconds.")
        @Config.RangeInt(min=0)
        public static int raidBufferTime = 90;
        @Config.Comment("Amount of time before the end of the raid to make all defenders glow, in minutes.")
        @Config.RangeInt(min=0,max=Integer.MAX_VALUE/60)
        public static int remainingTimeToGlow = 10;
        @Config.Comment("Maximum amount of consecutive time raiding parties can remain outside their target's territory, in seconds.")
        @Config.RangeInt(min=0)
        public static int maxAttackerAbandonmentTime = 30;
        @Config.Comment("Maximum amount of consecutive time defending clans can remain outside their territory during a raid, in seconds.")
        @Config.RangeInt(min=0)
        public static int maxClanDesertionTime = 60;
        @Config.Comment("Amount of shield given to the defending clan after a raid, in hours.")
        @Config.RangeInt(min=0)
        public static int defenseShield = 24*5;
        @Config.Comment("Amount of shield given to newly formed clans, in hours.")
        @Config.RangeInt(min=0)
        public static int initialShield = 24*3;
        @Config.Comment("Prevents reclaiming TNT that was placed while raiding.")
        public static boolean noReclaimTNT = true;
        //Costs, rewards, and multipliers
        @Config.Comment("Cost of forming a clan. This requires a compatible economy to be installed.")
        @Config.RangeInt(min=0)
        public static int formClanCost = 0;
        @Config.Comment("Initial amount in a clan account's balance when it is formed. This requires a compatible economy to be installed.")
        @Config.RangeInt(min=0)
        public static int formClanBankAmount = 0;
        @Config.Comment("Cost of claiming a chunk. This requires a compatible economy to be installed.")
        @Config.RangeInt(min=0)
        public static int claimChunkCost = 0;
        @Config.Comment("Cost of forming a new raiding party. This requires a compatible economy to be installed.")
        @Config.RangeInt(min=0)
        public static int startRaidCost = 0;
        @Config.Comment("Multiply the cost of starting a raid by the number of enemy claims. This requires a compatible economy to be installed.")
        public static boolean startRaidMultiplier = true;
        @Config.Comment("Reward for winning a raid. This requires a compatible economy to be installed.")
        @Config.RangeInt(min=0)
        public static int winRaidAmount = 0;
        @Config.Comment("Multiply the reward for winning a raid by the number of enemy claims. This requires a compatible economy to be installed.")
        public static boolean winRaidMultiplierClaims = true;
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
        @Config.Comment("Disband the clan when it can't afford upkeep. This requires a compatible economy to be installed.")
        public static boolean disbandNoUpkeep = false;
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
        public static boolean multiplyMaxRentClaims = true;
        //Dynmap settings
        @Config.Comment("The weight of the dynmap border for claims. This requires Dynmap to be installed.")
        @Config.RangeInt(min=0)
        public static int dynmapBorderWeight = 0;
        @Config.Comment("The opacity of the border for claims. 0.0=0%, 1.0=100%. This requires Dynmap to be installed.")
        @Config.RangeDouble(min=0, max=1)
        public static double dynmapBorderOpacity = 0.9;
        @Config.Comment("The opacity of the fill color for claims. 0.0=0%, 1.0=100%. This requires Dynmap to be installed.")
        @Config.RangeDouble(min=0, max=1)
        public static double dynmapFillOpacity = 0.75;
    }
}
