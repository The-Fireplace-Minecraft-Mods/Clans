package the_fireplace.clans;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.INBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import the_fireplace.clans.clan.ClaimedLandCapability;
import the_fireplace.clans.clan.ClanChunkCache;
import the_fireplace.clans.clan.NewClanDatabase;
import the_fireplace.clans.commands.CommandClan;
import the_fireplace.clans.commands.CommandOpClan;
import the_fireplace.clans.commands.CommandRaid;
import the_fireplace.clans.compat.dynmap.DynmapCompatDummy;
import the_fireplace.clans.compat.dynmap.IDynmapCompat;
import the_fireplace.clans.event.LandProtectionEvents;
import the_fireplace.clans.event.OtherEvents;
import the_fireplace.clans.event.RaidEvents;
import the_fireplace.clans.event.Timer;
import the_fireplace.clans.compat.payment.IPaymentHandler;
import the_fireplace.clans.compat.payment.PaymentHandlerDummy;
import the_fireplace.clans.compat.payment.PaymentHandlerGE;
import the_fireplace.clans.raid.NewRaidBlockPlacementDatabase;
import the_fireplace.clans.raid.NewRaidRestoreDatabase;
import the_fireplace.clans.util.PlayerClanCapability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.io.File;

import static the_fireplace.clans.Clans.MODID;

@SuppressWarnings("WeakerAccess")
@Mod.EventBusSubscriber(modid = MODID)
@Mod(MODID)
public final class Clans {
    public static final String MODID = "clans";

    public static final Logger LOGGER = LogManager.getLogger(MODID);

    @CapabilityInject(ClaimedLandCapability.class)
    public static final Capability<ClaimedLandCapability> CLAIMED_LAND = null;
    private static final ResourceLocation claimed_land_res = new ResourceLocation(MODID, "claim_data");

    @CapabilityInject(PlayerClanCapability.class)
    public static final Capability<PlayerClanCapability> CLAN_DATA_CAP = null;
    private static final ResourceLocation clan_home_res = new ResourceLocation(MODID, "home_cooldown_data");

    private static IPaymentHandler paymentHandler;
    public static IPaymentHandler getPaymentHandler(){
        return paymentHandler;
    }

    private static IDynmapCompat dynmapCompat;
    public static IDynmapCompat getDynmapCompat(){
        return dynmapCompat;
    }

    public Clans() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, cfg.SERVER_SPEC);
        MinecraftForge.EVENT_BUS.register(this);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::preInit);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::postInit);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::serverConfig);
    }

    public void serverConfig(ModConfig.ModConfigEvent event) {
        if (event.getConfig().getType() == ModConfig.Type.SERVER)
            cfg.load();
    }

    public void preInit(FMLCommonSetupEvent event){
        CapabilityManager.INSTANCE.register(ClaimedLandCapability.class, new ClaimedLandCapability.Storage(), ClaimedLandCapability.Default::new);
        CapabilityManager.INSTANCE.register(PlayerClanCapability.class, new PlayerClanCapability.Storage(), PlayerClanCapability.Default::new);

        dynmapCompat = new DynmapCompatDummy();

        MinecraftForge.EVENT_BUS.register(new RaidEvents());
        MinecraftForge.EVENT_BUS.register(new LandProtectionEvents());
        MinecraftForge.EVENT_BUS.register(new OtherEvents());
        MinecraftForge.EVENT_BUS.register(new Timer());

        dynmapCompat.init();
    }

    public void postInit(FMLLoadCompleteEvent event){
        if(ModList.get().isLoaded("grandeconomy"))
            paymentHandler = new PaymentHandlerGE();
        else
            paymentHandler = new PaymentHandlerDummy();
    }

    @SubscribeEvent
    public void onServerStart(FMLServerStartingEvent event) {
        CommandClan.register(event.getCommandDispatcher());
        CommandOpClan.register(event.getCommandDispatcher());
        CommandRaid.register(event.getCommandDispatcher());
        dynmapCompat.serverStart();
    }

    @SubscribeEvent
    public void onServerStop(FMLServerStoppingEvent event) {
        ClanChunkCache.save();
        NewClanDatabase.save();
        NewRaidBlockPlacementDatabase.save();
        NewRaidRestoreDatabase.save();
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
                public INBTBase serializeNBT() {
                    return CLAN_DATA_CAP.getStorage().writeNBT(CLAN_DATA_CAP, inst, null);
                }

                @Override
                public void deserializeNBT(INBTBase nbt) {
                    CLAN_DATA_CAP.getStorage().readNBT(CLAN_DATA_CAP, inst, null, nbt);
                }

                @Override
                public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
                    //noinspection unchecked
                    return capability == CLAN_DATA_CAP ? LazyOptional.of(() -> (T) inst): LazyOptional.empty();
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
            public INBTBase serializeNBT() {
                return CLAIMED_LAND.getStorage().writeNBT(CLAIMED_LAND, inst, null);
            }

            @Override
            public void deserializeNBT(INBTBase nbt) {
                CLAIMED_LAND.getStorage().readNBT(CLAIMED_LAND, inst, null, nbt);
            }

            @Nonnull
            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
                //noinspection unchecked
                return capability == CLAIMED_LAND ? LazyOptional.of(() -> (T) inst) : LazyOptional.empty();
            }
        });
    }

    public static File getDataDir() {
        return ServerLifecycleHooks.getCurrentServer().getDataDirectory();
    }

    public static class cfg {
        public static final ServerConfig SERVER;
        public static final ForgeConfigSpec SERVER_SPEC;
        static {
            final Pair<ServerConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
            SERVER_SPEC = specPair.getRight();
            SERVER = specPair.getLeft();
        }
        //General clan config
        public static boolean multipleClanLeaders;
        public static int maxNameLength;
        public static int minClanHomeDist;
        public static boolean forceConnectedClaims;
        public static boolean allowMultiClanMembership;
        public static int clanHomeWarmupTime;
        public static int clanHomeCooldownTime;
        public static int maxClanPlayerClaims;
        public static boolean showDefaultClanInChat;
        //Wilderness guard
        public static boolean protectWilderness;
        public static int minWildernessY;
        //Raid configuration
        public static int maxRaidersOffset;
        public static int maxRaidDuration;
        public static int raidBufferTime;
        public static int remainingTimeToGlow;
        public static int maxAttackerAbandonmentTime;
        public static int maxClanDesertionTime;
        public static int defenseShield;
        public static int initialShield;
        //Costs, rewards, and multipliers
        public static int formClanCost;
        public static int formClanBankAmount;
        public static int claimChunkCost;
        public static int startRaidCost;
        public static boolean startRaidMultiplier;
        public static int winRaidAmount;
        public static boolean winRaidMultiplierClaims;
        public static boolean winRaidMultiplierPlayers;
        public static int clanUpkeepDays;
        public static int clanUpkeepCost;
        public static boolean multiplyUpkeepClaims;
        public static boolean multiplyUpkeepMembers;
        public static boolean disbandNoUpkeep;
        //Clan finance management
        public static boolean leaderWithdrawFunds;
        public static boolean leaderRecieveDisbandFunds;
        public static int chargeRentDays;
        public static boolean evictNonpayers;
        public static boolean evictNonpayerAdmins;
        public static int maxRent;
        public static boolean multiplyMaxRentClaims;

        public static void load() {
            //General clan config
            multipleClanLeaders = SERVER.multipleClanLeaders.get();
            maxNameLength = SERVER.maxNameLength.get();
            minClanHomeDist = SERVER.minClanHomeDist.get();
            forceConnectedClaims = SERVER.forceConnectedClaims.get();
            allowMultiClanMembership = SERVER.allowMultiClanMembership.get();
            clanHomeWarmupTime = SERVER.clanHomeWarmupTime.get();
            clanHomeCooldownTime = SERVER.clanHomeCooldownTime.get();
            maxClanPlayerClaims = SERVER.maxClanPlayerClaims.get();
            showDefaultClanInChat = SERVER.showDefaultClanInChat.get();
            //Wilderness guard
            protectWilderness = SERVER.protectWilderness.get();
            minWildernessY = SERVER.minWildernessY.get();
            //Raid configuration
            maxRaidersOffset = SERVER.maxRaidersOffset.get();
            maxRaidDuration = SERVER.maxRaidDuration.get();
            raidBufferTime = SERVER.raidBufferTime.get();
            remainingTimeToGlow = SERVER.remainingTimeToGlow.get();
            maxAttackerAbandonmentTime = SERVER.maxAttackerAbandonmentTime.get();
            maxClanDesertionTime = SERVER.maxClanDesertionTime.get();
            defenseShield = SERVER.defenseShield.get();
            initialShield = SERVER.initialShield.get();
            //Costs, rewards, and multipliers
            formClanCost = SERVER.formClanCost.get();
            formClanBankAmount = SERVER.formClanBankAmount.get();
            claimChunkCost = SERVER.claimChunkCost.get();
            startRaidCost = SERVER.startRaidCost.get();
            startRaidMultiplier = SERVER.startRaidMultiplier.get();
            winRaidAmount = SERVER.winRaidAmount.get();
            winRaidMultiplierClaims = SERVER.winRaidMultiplierClaims.get();
            winRaidMultiplierPlayers = SERVER.winRaidMultiplierPlayers.get();
            clanUpkeepDays = SERVER.clanUpkeepDays.get();
            clanUpkeepCost = SERVER.clanUpkeepCost.get();
            multiplyUpkeepClaims = SERVER.multiplyUpkeepClaims.get();
            multiplyUpkeepMembers = SERVER.multiplyUpkeepMembers.get();
            disbandNoUpkeep = SERVER.disbandNoUpkeep.get();
            //Clan finance management
            leaderWithdrawFunds = SERVER.leaderWithdrawFunds.get();
            leaderRecieveDisbandFunds = SERVER.leaderRecieveDisbandFunds.get();
            chargeRentDays = SERVER.chargeRentDays.get();
            evictNonpayers = SERVER.evictNonpayers.get();
            evictNonpayerAdmins = SERVER.evictNonpayerAdmins.get();
            maxRent = SERVER.maxRent.get();
            multiplyMaxRentClaims = SERVER.multiplyMaxRentClaims.get();
        }

        public static class ServerConfig {
            //General clan config
            public ForgeConfigSpec.BooleanValue multipleClanLeaders;
            public ForgeConfigSpec.IntValue maxNameLength;
            public ForgeConfigSpec.IntValue minClanHomeDist;
            public ForgeConfigSpec.BooleanValue forceConnectedClaims;
            public ForgeConfigSpec.BooleanValue allowMultiClanMembership;
            public ForgeConfigSpec.IntValue clanHomeWarmupTime;
            public ForgeConfigSpec.IntValue clanHomeCooldownTime;
            public ForgeConfigSpec.IntValue maxClanPlayerClaims;
            public ForgeConfigSpec.BooleanValue showDefaultClanInChat;
            //Wilderness guard
            public ForgeConfigSpec.BooleanValue protectWilderness;
            public ForgeConfigSpec.IntValue minWildernessY;
            //Raid configuration
            public ForgeConfigSpec.IntValue maxRaidersOffset;
            public ForgeConfigSpec.IntValue maxRaidDuration;
            public ForgeConfigSpec.IntValue raidBufferTime;
            public ForgeConfigSpec.IntValue remainingTimeToGlow;
            public ForgeConfigSpec.IntValue maxAttackerAbandonmentTime;
            public ForgeConfigSpec.IntValue maxClanDesertionTime;
            public ForgeConfigSpec.IntValue defenseShield;
            public ForgeConfigSpec.IntValue initialShield;
            //Costs, rewards, and multipliers
            public ForgeConfigSpec.IntValue formClanCost;
            public ForgeConfigSpec.IntValue formClanBankAmount;
            public ForgeConfigSpec.IntValue claimChunkCost;
            public ForgeConfigSpec.IntValue startRaidCost;
            public ForgeConfigSpec.BooleanValue startRaidMultiplier;
            public ForgeConfigSpec.IntValue winRaidAmount;
            public ForgeConfigSpec.BooleanValue winRaidMultiplierClaims;
            public ForgeConfigSpec.BooleanValue winRaidMultiplierPlayers;
            public ForgeConfigSpec.IntValue clanUpkeepDays;
            public ForgeConfigSpec.IntValue clanUpkeepCost;
            public ForgeConfigSpec.BooleanValue multiplyUpkeepClaims;
            public ForgeConfigSpec.BooleanValue multiplyUpkeepMembers;
            public ForgeConfigSpec.BooleanValue disbandNoUpkeep;
            //Clan finance management
            public ForgeConfigSpec.BooleanValue leaderWithdrawFunds;
            public ForgeConfigSpec.BooleanValue leaderRecieveDisbandFunds;
            public ForgeConfigSpec.IntValue chargeRentDays;
            public ForgeConfigSpec.BooleanValue evictNonpayers;
            public ForgeConfigSpec.BooleanValue evictNonpayerAdmins;
            public ForgeConfigSpec.IntValue maxRent;
            public ForgeConfigSpec.BooleanValue multiplyMaxRentClaims;

            ServerConfig(ForgeConfigSpec.Builder builder) {
                builder.push("general");
                multipleClanLeaders = builder
                        .comment("Allow clans to have multiple leaders.")
                        .translation("Multiple Clan Leaders")
                        .define("multipleClanLeaders", true);
                maxNameLength = builder
                        .comment("Maximum clan name length. Larger values allow more characters to be typed for the clan name, but also increase the chance of clans making their name hard to type to avoid getting raided. Set to 0 for no limit.")
                        .translation("Max Clan Name Length")
                        .defineInRange("maxNameLength", 32, 0, Integer.MAX_VALUE);
                minClanHomeDist = builder
                        .comment("Minimum number of blocks between clan homes.")
                        .translation("Minimum Clan Home Separation Distance")
                        .defineInRange("minClanHomeDist", 320, 0, Integer.MAX_VALUE);
                forceConnectedClaims = builder
                        .comment("Force clans to have connected claims.")
                        .translation("Force Connected Claims")
                        .define("forceConnectedClaims", true);
                allowMultiClanMembership = builder
                        .comment("Allow players to be a member of multiple clans at once.")
                        .translation("Allow Multi Clan Membership")
                        .define("allowMultiClanMembership", true);
                clanHomeWarmupTime = builder
                        .comment("The amount of time, in seconds, the player must wait after typing /clan home before being teleported.")
                        .translation("Clan Home Warmup Time")
                        .defineInRange("clanHomeWarmupTime", 0, 0, Integer.MAX_VALUE);
                clanHomeCooldownTime = builder
                        .comment("The amount of time, in seconds, the player must wait after teleporting to the clan home before they can use /clan home again.")
                        .translation("Clan Home Cooldown Time")
                        .defineInRange("clanHomeCooldownTime", 0, 0, Integer.MAX_VALUE);
                maxClanPlayerClaims = builder
                        .comment("Max claims per player per clan. Set to 0 for infinite.")
                        .translation("Max claims per player per clan")
                        .defineInRange("maxClanPlayerClaims", 0, 0, Integer.MAX_VALUE);
                showDefaultClanInChat = builder
                        .comment("Show player's default clan in chat.")
                        .translation("Show Default Clan in Chat")
                        .define("showDefaultClanInChat", true);
                builder.pop();

                builder.push("wilderness");
                protectWilderness = builder
                        .comment("Protect the wilderness from damage above a specific Y level.")
                        .translation("Protect Wilderness")
                        .define("protectWilderness", true);
                minWildernessY = builder
                        .comment("Minimum Y level to protect with the Protect Wilderness option, inclusive. Set to a negative number to use sea level.")
                        .translation("Minimum Wilderness Y Level")
                        .defineInRange("minWildernessY", -1, Integer.MIN_VALUE, Integer.MAX_VALUE);
                builder.pop();

                builder.push("raid");
                maxRaidersOffset = builder
                        .comment("Offset the maximum number of raiders by this much when determining how many people can join a raiding party. Formula is: (# raiders) - (maxRaiderOffset) <= (# defenders)")
                        .translation("Maximum Raider Count Offset")
                        .defineInRange("maxRaidersOffset", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
                maxRaidDuration = builder
                        .comment("Maximum duration a raid can last for, in minutes.")
                        .translation("Maximum Raid Duration")
                        .defineInRange("maxRaidDuration", 20, 0, Integer.MAX_VALUE);
                raidBufferTime = builder
                        .comment("The amount of time the defenders are given to prepare for a raid, in seconds.")
                        .translation("Raid Preparation Time")
                        .defineInRange("raidBufferTime", 120, 0, Integer.MAX_VALUE);
                remainingTimeToGlow = builder
                        .comment("Amount of time before the end of the raid to make all defenders glow, in minutes.")
                        .translation("Remaining Time To Glow")
                        .defineInRange("remainingTimeToGlow", 10, 0, Integer.MAX_VALUE);
                maxAttackerAbandonmentTime = builder
                        .comment("Maximum amount of consecutive time members of raiding parties can remain outside their target's territory, in seconds.")
                        .translation("Maximum Raider Abandonment Time")
                        .defineInRange("maxAttackerAbandonmentTime", 30, 0, Integer.MAX_VALUE);
                maxClanDesertionTime = builder
                        .comment("Maximum amount of consecutive time defending clan members can remain outside their territory during a raid, in seconds.")
                        .translation("Maximum Defender Desertion Time")
                        .defineInRange("maxClanDesertionTime", 30, 0, Integer.MAX_VALUE);
                defenseShield = builder
                        .comment("Amount of shield given to the defending clan after a raid, in hours.")
                        .translation("Post-Raid Shield")
                        .defineInRange("defenseShield", 24*5, 0, Integer.MAX_VALUE);
                initialShield = builder
                        .comment("Amount of shield given to newly formed clans, in hours.")
                        .translation("New Clan Shield")
                        .defineInRange("initialShield", 24*3, 0, Integer.MAX_VALUE);
                builder.pop();

                String economyNotice = " This requires a compatible economy to be installed.";

                builder.push("pricing");
                formClanCost = builder
                        .comment("Cost of forming a clan."+economyNotice)
                        .translation("Clan Formation Cost")
                        .defineInRange("formClanCost", 0, 0, Integer.MAX_VALUE);
                formClanBankAmount = builder
                        .comment("Initial amount in a clan account's balance when it is formed."+economyNotice)
                        .translation("Initial Clan Bank Amount")
                        .defineInRange("formClanBankAmount", 0, 0, Integer.MAX_VALUE);
                claimChunkCost = builder
                        .comment("Cost of claiming a chunk of land."+economyNotice)
                        .translation("Claim Chunk Cost")
                        .defineInRange("claimChunkCost", 0, 0, Integer.MAX_VALUE);
                startRaidCost = builder
                        .comment("Cost of forming a new raiding party."+economyNotice)
                        .translation("Form Raid Cost")
                        .defineInRange("startRaidCost", 0, 0, Integer.MAX_VALUE);
                startRaidMultiplier = builder
                        .comment("Multiply the cost of forming a raid by the number of enemy claims."+economyNotice)
                        .translation("Form Raid Cost Claim Multiplier")
                        .define("startRaidMultiplier", true);
                winRaidAmount = builder
                        .comment("Reward for winning a raid. This gets taken out of the raid target's bank account."+economyNotice)
                        .translation("Win Raid Amount")
                        .defineInRange("winRaidAmount", 0, 0, Integer.MAX_VALUE);
                winRaidMultiplierClaims = builder
                        .comment("Multiply the reward for winning a raid by the number of enemy claims."+economyNotice)
                        .translation("Win Raid Amount Claim Multiplier")
                        .define("winRaidMultiplierClaims", true);
                winRaidMultiplierPlayers = builder
                        .comment("Multiply the reward for winning a raid by the number of online enemy players."+economyNotice)
                        .translation("Win Raid Amount Defender Multiplier")
                        .define("winRaidMultiplierPlayers", false);
                clanUpkeepDays = builder
                        .comment("How often to charge clans upkeep (in days). Set to 0 to disable the need for upkeep."+economyNotice)
                        .translation("Clan Upkeep Time")
                        .defineInRange("clanUpkeepDays", 0, 0, Integer.MAX_VALUE);
                clanUpkeepCost = builder
                        .comment("Amount to charge a clan for upkeep."+economyNotice)
                        .translation("Clan Upkeep Cost")
                        .defineInRange("clanUpkeepCost", 0, 0, Integer.MAX_VALUE);
                multiplyUpkeepClaims = builder
                        .comment("Multiply the clan upkeep by the number of claims."+economyNotice)
                        .translation("Clan Upkeep Cost Claim Multiplier")
                        .define("multiplyUpkeepClaims", true);
                multiplyUpkeepMembers = builder
                        .comment("Multiply the clan upkeep by the number of members."+economyNotice)
                        .translation("Clan Upkeep Cost Player Multiplier")
                        .define("multiplyUpkeepMembers", false);
                disbandNoUpkeep = builder
                        .comment("Disband the clan when it can't afford upkeep."+economyNotice)
                        .translation("Disband Nonpaying Clans")
                        .define("disbandNoUpkeep", false);
                builder.pop();

                builder.push("finances");
                leaderWithdrawFunds = builder
                        .comment("Allow the clan leader to withdraw funds from the clan bank account."+economyNotice)
                        .translation("Leaders Can Withdraw Funds")
                        .define("leaderWithdrawFunds", false);
                leaderRecieveDisbandFunds = builder
                        .comment("When enabled, remaining clan funds go to the clan leader when the clan is disbanded. When disabled, remaining clan funds get split evenly among all clan members when the clan is disbanded."+economyNotice)
                        .translation("Leaders Recieve Disband Funds")
                        .define("leaderRecieveDisbandFunds", true);
                chargeRentDays = builder
                        .comment("Frequency to charge clan members rent to go into the clan bank account (in days). If enabled, allows clan leaders to set the amount for their clans. Set to 0 to disable clan rent."+economyNotice)
                        .translation("Charge Rent Time")
                        .defineInRange("chargeRentDays", 0, 0, Integer.MAX_VALUE);
                evictNonpayers = builder
                        .comment("Kick clan members out who can't afford rent. This will not kick out leaders."+economyNotice)
                        .translation("Evict Nonpaying Clan Members")
                        .define("evictNonpayers", false);
                evictNonpayerAdmins = builder
                        .comment("Kick clan admins out who can't afford rent. This will not kick out leaders."+economyNotice)
                        .translation("Evict Nonpaying Clan Admins")
                        .define("evictNonpayerAdmins", false);
                maxRent = builder
                        .comment("Maximum amount of rent a clan can charge. Set to 0 for no maximum."+economyNotice)
                        .translation("Max Rent")
                        .defineInRange("maxRent", 0, 0, Integer.MAX_VALUE);
                multiplyMaxRentClaims = builder
                        .comment("Multiply the max rent by the number of claims."+economyNotice)
                        .translation("Max Rent Claim Multiplier")
                        .define("multiplyMaxRentClaims", true);
                builder.pop();
            }
        }
    }
}
