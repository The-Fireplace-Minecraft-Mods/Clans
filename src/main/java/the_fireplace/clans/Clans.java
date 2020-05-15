package the_fireplace.clans;

import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.*;
import org.apache.logging.log4j.Logger;
import the_fireplace.clans.abstraction.IConfig;
import the_fireplace.clans.abstraction.IDynmapCompat;
import the_fireplace.clans.abstraction.IProtectionCompat;
import the_fireplace.clans.abstraction.dummy.DynmapCompatDummy;
import the_fireplace.clans.forge.ForgePermissionHandler;
import the_fireplace.clans.forge.compat.DynmapCompat;
import the_fireplace.clans.forge.compat.ForgeMinecraftHelper;
import the_fireplace.clans.forge.compat.IceAndFireCompat;
import the_fireplace.clans.forge.compat.PaymentHandlerGE;
import the_fireplace.clans.logic.ServerEventLogic;
import the_fireplace.clans.sponge.PaymentHandlerSponge;
import the_fireplace.clans.sponge.SpongePermissionHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static the_fireplace.clans.ClansHelper.MODID;

@Mod.EventBusSubscriber(modid = MODID)
@Mod(modid = MODID, name = ClansHelper.MODNAME, version = ClansHelper.VERSION, acceptedMinecraftVersions = "[1.12,1.13)", acceptableRemoteVersions = "*", dependencies="after:grandeconomy;after:dynmap;after:spongeapi;required-after:forge@[14.23.5.2817,)", certificateFingerprint = "${fingerprint}")
public final class Clans {
    @Mod.Instance(MODID)
    public static Clans instance;

    private static Logger LOGGER = FMLLog.log;
    private static final ForgeMinecraftHelper minecraftHelper = new ForgeMinecraftHelper();
    private static IDynmapCompat dynmapCompat = new DynmapCompatDummy();
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
    private boolean validJar = true;

    public static Logger getLogger() {
        return LOGGER;
    }

    static void setDynmapCompat(IDynmapCompat dynmapCompat) {
        Clans.dynmapCompat = dynmapCompat;
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ClansHelper.setConfig(new cfg());
        LOGGER = event.getModLog();

        if(getMinecraftHelper().isPluginLoaded("dynmap"))
            setDynmapCompat(new DynmapCompat());
        if(getMinecraftHelper().isPluginLoaded("iceandfire"))
            addProtectionCompat(new IceAndFireCompat());

        if(!validJar)
            Clans.getMinecraftHelper().getLogger().error("The jar's signature is invalid! Please redownload from "+ Objects.requireNonNull(Loader.instance().activeModContainer()).getUpdateUrl());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event){
        getDynmapCompat().init();
        getProtectionCompat().init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event){
        if(getMinecraftHelper().isPluginLoaded("grandeconomy"))
            ClansHelper.setPaymentHandler(new PaymentHandlerGE());
        else if(getMinecraftHelper().isPluginLoaded("spongeapi"))
            ClansHelper.setPaymentHandler(new PaymentHandlerSponge());
        if(getMinecraftHelper().isPluginLoaded("spongeapi") && !ClansHelper.getConfig().isForgePermissionPrecedence())
            ClansHelper.setPermissionManager(new SpongePermissionHandler());
        else
            ClansHelper.setPermissionManager(new ForgePermissionHandler());
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

    @Mod.EventHandler
    public void invalidFingerprint(FMLFingerprintViolationEvent e) {
        if(!e.isDirectory())
            validJar = false;
    }

    @SuppressWarnings("WeakerAccess")
    @Config(modid = MODID)
    private static class cfg implements IConfig {
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
        @Config.Comment("Max claims per clan. Set to 0 for infinite.")
        @Config.RangeInt(min=0)
        public static int maxClaims = 0;
        @Config.Comment("If enabled, multiplies the max claim count by the number of players in the clan.")
        public static boolean multiplyMaxClaimsByPlayers = true;
        @Config.Comment("Clan prefix for chat. Make empty for no prefix. %s is where the clan name will go.")
        public static String defaultClanPrefix = "[%s]";
        @Config.Comment("Default clan to put players in when they first join the server. Leave empty for no clan.")
        public static String serverDefaultClan = "";
        @Config.Comment("Value or formula for the cost of disbanding a clan. See https://gist.github.com/The-Fireplace/2b6e21b1892bc5eafc4c70ab49ed3505 for formula writing details.")
        public static String disbandFeeFormula = "2^(m-0.25)";
        @Config.Comment("Should the player spawn at the clan home if they don't have a bed?")
        public static boolean clanHomeFallbackSpawn = true;
        //General mod configuration
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
        @Config.Comment("Allow protection against breaking and placing blocks on claimed land.")
        public static boolean allowBuildProtection = true;
        @Config.Comment("Allow protection against interacting on claimed land.")
        public static boolean allowInteractProtection = true;
        @Config.Comment("Allow protection against injury on claimed land.")
        public static boolean allowInjuryProtection = true;
        @Config.Comment("Always allow TNT to ignite other TNT, regardless of whether other blocks are protected.")
        public static boolean chainTNT = true;
        @Config.Comment("A list of blocks that are able to be locked.")
        public static String[] lockableBlocks = {
                "minecraft:chest",
                "minecraft:furnace",
                "minecraft:lit_furnace",
                "minecraft:jukebox",
                "minecraft:white_shulker_box",
                "minecraft:orange_shulker_box",
                "minecraft:magenta_shulker_box",
                "minecraft:light_blue_shulker_box",
                "minecraft:yellow_shulker_box",
                "minecraft:lime_shulker_box",
                "minecraft:pink_shulker_box",
                "minecraft:gray_shulker_box",
                "minecraft:silver_shulker_box",
                "minecraft:cyan_shulker_box",
                "minecraft:purple_shulker_box",
                "minecraft:blue_shulker_box",
                "minecraft:brown_shulker_box",
                "minecraft:green_shulker_box",
                "minecraft:red_shulker_box",
                "minecraft:black_shulker_box",
                "minecraft:bed",
                "minecraft:dispenser",
                "minecraft:lever",//TODO find out if pressure plates can be locked
                //"minecraft:stone_pressure_plate",
                //"minecraft:wooden_pressure_plate",
                "minecraft:stone_pressure_plate",
                "minecraft:wooden_pressure_plate",
                "minecraft:stone_button",
                "minecraft:trapdoor",
                "minecraft:fence_gate",
                "minecraft:wooden_button",
                "minecraft:trapped_chest",
                //"minecraft:light_weighted_pressure_plate",
                //"minecraft:heavy_weighted_pressure_plate",
                "minecraft:daylight_detector",
                "minecraft:hopper",
                "minecraft:dropper",
                "minecraft:spruce_fence_gate",
                "minecraft:birch_fence_gate",
                "minecraft:jungle_fence_gate",
                "minecraft:dark_oak_fence_gate",
                "minecraft:acacia_fence_gate",
                "minecraft:wooden_door",
                "minecraft:repeater",
                "minecraft:comparator",
                "minecraft:spruce_door",
                "minecraft:birch_door",
                "minecraft:jungle_door",
                "minecraft:acacia_door",
                "minecraft:dark_oak_door",
                "minecraft:beacon",
                "minecraft:brewing_stand",
        };
        //Wilderness guard
        @Config.Comment("Protect the wilderness from damage above a specific Y level")
        public static boolean protectWilderness = false;
        @Config.Comment("Minimum Y level to protect with the Protect Wilderness option, inclusive. Set to a negative number to use sea level.")
        public static int minWildernessY = -1;
        @Config.Comment("A list of dimensions players are allowed to claim in. If it contains a *, this list is a blacklist. Otherwise, it is a whitelist, so by default it is a whitelist containing the overworld and the nether.")
        public static String[] claimableDimensions = {"overworld", "the_nether"};
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
        @Config.Comment("Raid break speed multiplier")
        @Config.RangeDouble(min=0.0, max=10.0)
        public static double raidBreakSpeedMultiplier = 1.0;
        @Config.Comment("This option disables rollback of raids.")
        public static boolean disableRaidRollback = false;
        @Config.Comment("Controls if stealing from containers(even locked ones) is allowed during raids. This theft does not get rolled back at the end of the raid.")
        public static boolean enableStealing = false;
        @Config.Comment("A list of items allowed in a raid. If it contains a *, this list is a blacklist, so everything except bedrock is allowed by default. Otherwise, it is a whitelist.")
        public static String[] raidItemList = {"*", "minecraft:bedrock"};
        @Config.Comment("If enabled, raiders will be teleported outside the raid target's home territory when they start the raid.")
        public static boolean teleportOnRaidStart = true;
        //Costs, rewards, and multipliers
        @Config.Comment("Cost of forming a clan. This requires a compatible economy to be installed.")
        @Config.RangeDouble(min=0)
        public static double formClanCost = 0;
        @Config.Comment("Initial amount in a clan account's balance when it is formed. This requires a compatible economy to be installed.")
        @Config.RangeDouble(min=0)
        public static double formClanBankAmount = 0;
        @Config.Comment("Value or formula for cost of claiming a chunk. This requires a compatible economy to be installed. See https://gist.github.com/The-Fireplace/2b6e21b1892bc5eafc4c70ab49ed3505 for formula writing details.")
        public static String claimChunkCostFormula = "0";
        @Config.Comment("Reduced cost of claiming a chunk for the first x claims. This requires a compatible economy to be installed.")
        @Config.RangeDouble(min=0)
        public static double reducedClaimChunkCost = 0;
        @Config.Comment("Use the reduced cost for this many claims. This requires a compatible economy to be installed.")
        @Config.RangeInt(min=0)
        public static int reducedCostClaimCount = 0;
        @Config.Comment("Value or formula for cost of starting a raid. This requires a compatible economy to be installed. See https://gist.github.com/The-Fireplace/2b6e21b1892bc5eafc4c70ab49ed3505 for formula writing details.")
        public static String startRaidCostFormula = "0*c";
        @Config.Comment("Value or formula for reward for winning a raid. This requires a compatible economy to be installed. See https://gist.github.com/The-Fireplace/2b6e21b1892bc5eafc4c70ab49ed3505 for formula writing details.")
        public static String winRaidAmountFormula = "0*m";
        @Config.Comment("How often to charge clans upkeep(in days). Set to 0 to disable the need for upkeep. This requires a compatible economy to be installed.")
        @Config.RangeInt(min=0)
        public static int clanUpkeepDays = 0;
        @Config.Comment("Value or formula for amount to charge a clan for upkeep. This requires a compatible economy to be installed. See https://gist.github.com/The-Fireplace/2b6e21b1892bc5eafc4c70ab49ed3505 for formula writing details.")
        public static String clanUpkeepCostFormula = "0*c";
        @Config.Comment("Disband the clan when it can't afford upkeep. This requires a compatible economy to be installed.")
        public static boolean disbandNoUpkeep = false;
        @Config.Comment("If enabled, rewards will increase as a clan gets repeatedly defeated. This requires a compatible economy to be installed.")
        public static boolean increasingRewards = true;
        @Config.Comment("Win-Loss Ratio threshold for decreasing the reward multiplier - Helps prevent rich clans from hiring people to purposely fail a raid and reduce their reward multiplier. This requires a compatible economy to be installed.")
        @Config.RangeDouble(min=0)
        public static double wlrThreshold = 0.66;
        @Config.Comment("Formula to increase the reward multiplier to after a clan loses to raiders. This requires increasingRewards to do anything. See https://gist.github.com/The-Fireplace/2b6e21b1892bc5eafc4c70ab49ed3505 for formula writing details.")
        public static String multiplierIncreaseFormula = "m^1.05";
        @Config.Comment("Formula to decrease the reward multiplier to after a clan wins against raiders. This requires increasingRewards to do anything. This formula will not make the multiplier go below 1. See https://gist.github.com/The-Fireplace/2b6e21b1892bc5eafc4c70ab49ed3505 for formula writing details.")
        public static String multiplierDecreaseFormula = "m^0.95";
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
        @Config.Comment("Value or formula for maximum amount of rent a clan can charge. Set to 0 for no maximum. This requires a compatible economy to be installed. See https://gist.github.com/The-Fireplace/2b6e21b1892bc5eafc4c70ab49ed3505 for formula writing details.")
        public static String maxRentFormula = "0*c";
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

        @Override
        public boolean isMultipleClanLeaders() {
            return multipleClanLeaders;
        }

        @Override
        public int getMaxNameLength() {
            return maxNameLength;
        }

        @Override
        public boolean isAllowMultiClanMembership() {
            return allowMultiClanMembership;
        }

        @Override
        public int getClanHomeWarmupTime() {
            return clanHomeWarmupTime;
        }

        @Override
        public int getClanHomeCooldownTime() {
            return clanHomeCooldownTime;
        }

        @Override
        public String getDefaultClanPrefix() {
            return defaultClanPrefix;
        }

        @Override
        public String getServerDefaultClan() {
            return serverDefaultClan;
        }

        @Override
        public String getDisbandFeeFormula() {
            return disbandFeeFormula;
        }

        @Override
        public boolean isClanHomeFallbackSpawnpoint() {
            return clanHomeFallbackSpawn;
        }

        @Override
        public String getLocale() {
            return locale;
        }

        @Override
        public List<String> getTolerableFakePlayers() {
            return Lists.newArrayList(tolerableFakePlayers);
        }

        @Override
        public boolean isFakePlayerDump() {
            return fakePlayerDump;
        }

        @Override
        public boolean isForgePermissionPrecedence() {
            return forgePermissionPrecedence;
        }

        @Override
        public int getMinClanHomeDist() {
            return minClanHomeDist;
        }

        @Override
        public double getInitialClaimSeparationMultiplier() {
            return initialClaimSeparationMultiplier;
        }

        @Override
        public boolean isEnforceInitialClaimSeparation() {
            return enforceInitialClaimSeparation;
        }

        @Override
        public boolean isForceConnectedClaims() {
            return forceConnectedClaims;
        }

        @Override
        public String getConnectedClaimCheck() {
            return connectedClaimCheck;
        }

        @Override
        public boolean isEnableBorderlands() {
            return enableBorderlands;
        }

        @Override
        public boolean isPreventMobsOnClaims() {
            return preventMobsOnClaims;
        }

        @Override
        public boolean isPreventMobsOnBorderlands() {
            return preventMobsOnBorderlands;
        }

        @Override
        public int getMaxClaims() {
            return maxClaims;
        }

        @Override
        public boolean isMultiplyMaxClaimsByPlayers() {
            return multiplyMaxClaimsByPlayers;
        }

        @Override
        public boolean isProtectWilderness() {
            return protectWilderness;
        }

        @Override
        public int getMinWildernessY() {
            return minWildernessY;
        }

        @Override
        public String[] getClaimableDimensions() {
            return claimableDimensions;
        }

        @Override
        public boolean isChainTNT() {
            return chainTNT;
        }

        @Override
        public List<String> getLockableBlocks() {
            return Lists.newArrayList(lockableBlocks);
        }

        @Override
        public int getMaxRaidersOffset() {
            return maxRaidersOffset;
        }

        @Override
        public int getMaxRaidDuration() {
            return maxRaidDuration;
        }

        @Override
        public int getRaidBufferTime() {
            return raidBufferTime;
        }

        @Override
        public int getRemainingTimeToGlow() {
            return remainingTimeToGlow;
        }

        @Override
        public int getMaxAttackerAbandonmentTime() {
            return maxAttackerAbandonmentTime;
        }

        @Override
        public int getMaxClanDesertionTime() {
            return maxClanDesertionTime;
        }

        @Override
        public int getDefenseShield() {
            return defenseShield;
        }

        @Override
        public int getInitialShield() {
            return initialShield;
        }

        @Override
        public boolean isNoReclaimTNT() {
            return noReclaimTNT;
        }

        @Override
        public double getFormClanCost() {
            return formClanCost;
        }

        @Override
        public double getFormClanBankAmount() {
            return formClanBankAmount;
        }

        @Override
        public String getClaimChunkCostFormula() {
            return claimChunkCostFormula;
        }

        @Override
        public double getReducedChunkClaimCost() {
            return reducedClaimChunkCost;
        }

        @Override
        public int getReducedCostClaimCount() {
            return reducedCostClaimCount;
        }

        @Override
        public String getStartRaidCostFormula() {
            return startRaidCostFormula;
        }

        @Override
        public String getWinRaidAmountFormula() {
            return winRaidAmountFormula;
        }

        @Override
        public boolean isDisableRaidRollback() {
            return disableRaidRollback;
        }

        @Override
        public boolean isEnableStealing() {
            return enableStealing;
        }

        @Override
        public int getClanUpkeepDays() {
            return clanUpkeepDays;
        }

        @Override
        public String getClanUpkeepCostFormula() {
            return clanUpkeepCostFormula;
        }

        @Override
        public boolean isDisbandNoUpkeep() {
            return disbandNoUpkeep;
        }

        @Override
        public boolean isIncreasingRewards() {
            return increasingRewards;
        }

        @Override
        public double getWLRThreshold() {
            return wlrThreshold;
        }

        @Override
        public String getIncreasedMultiplierFormula() {
            return multiplierIncreaseFormula;
        }

        @Override
        public String getDecreasedMultiplierFormula() {
            return multiplierDecreaseFormula;
        }

        @Override
        public boolean isLeaderWithdrawFunds() {
            return leaderWithdrawFunds;
        }

        @Override
        public boolean isLeaderRecieveDisbandFunds() {
            return leaderRecieveDisbandFunds;
        }

        @Override
        public int getChargeRentDays() {
            return chargeRentDays;
        }

        @Override
        public boolean isEvictNonpayers() {
            return evictNonpayers;
        }

        @Override
        public boolean isEvictNonpayerAdmins() {
            return evictNonpayerAdmins;
        }

        @Override
        public String getMaxRentFormula() {
            return maxRentFormula;
        }

        @Override
        public int getDynmapBorderWeight() {
            return dynmapBorderWeight;
        }

        @Override
        public double getDynmapBorderOpacity() {
            return dynmapBorderOpacity;
        }

        @Override
        public double getDynmapFillOpacity() {
            return dynmapFillOpacity;
        }

        @Override
        public double getRaidBreakSpeedMultiplier() {
            return raidBreakSpeedMultiplier;
        }

        @Override
        public List<String> getRaidItemList() {
            return Lists.newArrayList(raidItemList);
        }

        @Override
        public boolean isTeleportToRaidStart() {
            return teleportOnRaidStart;
        }

        @Override
        public boolean allowBuildProtection() {
            return allowBuildProtection;
        }

        @Override
        public boolean allowInjuryProtection() {
            return allowInjuryProtection;
        }

        @Override
        public boolean allowInteractionProtection() {
            return allowInteractProtection;
        }
    }
}
