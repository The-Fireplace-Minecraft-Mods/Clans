package the_fireplace.clans;

import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.*;
import org.apache.logging.log4j.Logger;
import the_fireplace.clans.legacy.abstraction.*;
import the_fireplace.clans.legacy.abstraction.dummy.ChatCensorCompatDummy;
import the_fireplace.clans.legacy.abstraction.dummy.DynmapCompatDummy;
import the_fireplace.clans.legacy.abstraction.dummy.PaymentHandlerDummy;
import the_fireplace.clans.legacy.config.Config;
import the_fireplace.clans.legacy.forge.ForgePermissionHandler;
import the_fireplace.clans.legacy.forge.compat.*;
import the_fireplace.clans.legacy.logic.ServerEventLogic;
import the_fireplace.clans.legacy.sponge.PaymentHandlerSponge;
import the_fireplace.clans.legacy.sponge.SpongePermissionHandler;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static the_fireplace.clans.ClansModContainer.MODID;

@Mod.EventBusSubscriber(modid = MODID)
@Mod(modid = MODID, name = ClansModContainer.MODNAME, version = ClansModContainer.VERSION, acceptedMinecraftVersions = "[1.12,1.13)", acceptableRemoteVersions = "*", dependencies="after:grandeconomy;after:dynmap;after:spongeapi;required-after:forge@[14.23.5.2817,)", certificateFingerprint = "${fingerprint}")
public final class ClansModContainer {
    public static final String MODID = "clans";
    public static final String MODNAME = "Clans";
    public static final String VERSION = "${version}";
    private static final IConfig config = new IConfig() {
        @Override
        public boolean allowsMultipleClanLeaders() {
            return Config.getInstance().clan.multipleClanLeaders;
        }

        @Override
        public int getMaxNameLength() {
            return Config.getInstance().generalClan.maxNameLength;
        }

        @Override
        public boolean isAllowMultiClanMembership() {
            return Config.getInstance().generalClan.allowMultiClanMembership;
        }

        @Override
        public int getClanHomeWarmupTime() {
            return Config.getInstance().clan.clanHomeWarmupTime;
        }

        @Override
        public int getClanHomeCooldownTime() {
            return Config.getInstance().clan.clanHomeCooldownTime;
        }

        @Override
        public int getMaxClaims() {
            return Config.getInstance().clan.maxClaims;
        }

        @Override
        public boolean isMultiplyMaxClaimsByPlayers() {
            return Config.getInstance().clan.multiplyMaxClaimsByPlayers;
        }

        @Override
        public String getDefaultClanPrefix() {
            return Config.getInstance().clan.chatPrefix;
        }

        @Override
        public String getServerDefaultClan() {
            return Config.getInstance().generalClan.serverDefaultClan;
        }

        @Override
        public String getDisbandFeeFormula() {
            return Config.getInstance().clan.disbandFeeFormula;
        }

        @Override
        public boolean isClanHomeFallbackSpawnpoint() {
            return Config.getInstance().clan.clanHomeFallbackSpawn;
        }

        @Override
        public String getLocale() {
            return Config.getInstance().general.locale;
        }

        @Override
        public Collection<String> getTolerableFakePlayers() {
            return Config.getInstance().general.tolerableFakePlayers;
        }

        @Override
        public boolean isFakePlayerDump() {
            return Config.getInstance().general.fakePlayerDump;
        }

        @Override
        public boolean isForgePermissionPrecedence() {
            return Config.getInstance().general.forgePermissionPrecedence;
        }

        @Override
        public int getMinClanHomeDist() {
            return Config.getInstance().protection.minClanHomeDist;
        }

        @Override
        public double getInitialClaimSeparationMultiplier() {
            return Config.getInstance().protection.initialClaimSeparationMultiplier;
        }

        @Override
        public boolean isEnforceInitialClaimSeparation() {
            return Config.getInstance().protection.enforceInitialClaimSeparation;
        }

        @Override
        public boolean isForceConnectedClaims() {
            return Config.getInstance().protection.forceConnectedClaims;
        }

        @Override
        public String getConnectedClaimCheck() {
            return Config.getInstance().protection.connectedClaimCheck;
        }

        @Override
        public boolean isEnableBorderlands() {
            return Config.getInstance().protection.enableBorderlands;
        }

        @Override
        public boolean isPreventMobsOnClaims() {
            return Config.getInstance().protection.preventMobsOnClaims;
        }

        @Override
        public boolean isPreventMobsOnBorderlands() {
            return Config.getInstance().protection.preventMobsOnBorderlands;
        }

        @Override
        public boolean allowBuildProtection() {
            return Config.getInstance().general.allowBuildProtection;
        }

        @Override
        public boolean allowInjuryProtection() {
            return Config.getInstance().general.allowInjuryProtection;
        }

        @Override
        public boolean allowInteractionProtection() {
            return Config.getInstance().general.allowInteractProtection;
        }

        @Override
        public boolean isChainTNT() {
            return Config.getInstance().protection.chainTNT;
        }

        @Override
        public Collection<String> getLockableBlocks() {
            return Config.getInstance().protection.lockableBlocks;
        }

        @Override
        public boolean shouldProtectWilderness() {
            return Config.getInstance().protection.protectWilderness;
        }

        @Override
        public int getMinWildernessY() {
            return Config.getInstance().protection.minWildernessY;
        }

        @Override
        public Collection<String> getClaimableDimensions() {
            return Config.getInstance().protection.claimableDimensions;
        }

        @Override
        public int getMaxRaidersOffset() {
            return Config.getInstance().raid.maxRaidersOffset;
        }

        @Override
        public int getMaxRaidDuration() {
            return Config.getInstance().raid.maxRaidDuration;
        }

        @Override
        public int getRaidBufferTime() {
            return Config.getInstance().raid.raidBufferTime;
        }

        @Override
        public int getRemainingTimeToGlow() {
            return Config.getInstance().raid.remainingTimeToGlow;
        }

        @Override
        public int getMaxAttackerAbandonmentTime() {
            return Config.getInstance().raid.maxAttackerAbandonmentTime;
        }

        @Override
        public int getMaxClanDesertionTime() {
            return Config.getInstance().raid.maxClanDesertionTime;
        }

        @Override
        public int getDefenseShield() {
            return Config.getInstance().raid.defenseShield;
        }

        @Override
        public int getInitialShield() {
            return Config.getInstance().raid.initialShield;
        }

        @Override
        public boolean isNoReclaimTNT() {
            return Config.getInstance().raid.noReclaimTNT;
        }

        @Override
        public boolean isDisableRaidRollback() {
            return Config.getInstance().raid.disableRaidRollback;
        }

        @Override
        public boolean isEnableStealing() {
            return Config.getInstance().raid.enableStealing;
        }

        @Override
        public double getRaidBreakSpeedMultiplier() {
            return Config.getInstance().raid.raidBreakSpeedMultiplier;
        }

        @Override
        public Collection<String> getRaidItemList() {
            return Config.getInstance().raid.raidItemList;
        }

        @Override
        public boolean isTeleportToRaidStart() {
            return Config.getInstance().raid.teleportOnRaidStart;
        }

        @Override
        public double getFormClanCost() {
            return Config.getInstance().generalClan.formClanCost;
        }

        @Override
        public double getFormClanBankAmount() {
            return Config.getInstance().generalClan.formClanBankAmount;
        }

        @Override
        public String getClaimChunkCostFormula() {
            return Config.getInstance().clan.claimChunkCostFormula;
        }

        @Override
        public double getReducedChunkClaimCost() {
            return Config.getInstance().clan.reducedClaimChunkCost;
        }

        @Override
        public int getReducedCostClaimCount() {
            return Config.getInstance().clan.reducedCostClaimCount;
        }

        @Override
        public String getStartRaidCostFormula() {
            return Config.getInstance().raid.startRaidCostFormula;
        }

        @Override
        public String getWinRaidAmountFormula() {
            return Config.getInstance().raid.winRaidAmountFormula;
        }

        @Override
        public int getClanUpkeepDays() {
            return Config.getInstance().clan.clanUpkeepDays;
        }

        @Override
        public String getClanUpkeepCostFormula() {
            return Config.getInstance().clan.clanUpkeepCostFormula;
        }

        @Override
        public boolean isDisbandNoUpkeep() {
            return Config.getInstance().clan.disbandNoUpkeep;
        }

        @Override
        public boolean isIncreasingRewards() {
            return Config.getInstance().raid.increasingRewards;
        }

        @Override
        public double getWLRThreshold() {
            return Config.getInstance().raid.wlrThreshold;
        }

        @Override
        public String getIncreasedMultiplierFormula() {
            return Config.getInstance().raid.multiplierIncreaseFormula;
        }

        @Override
        public String getDecreasedMultiplierFormula() {
            return Config.getInstance().raid.multiplierDecreaseFormula;
        }

        @Override
        public boolean isLeaderWithdrawFunds() {
            return Config.getInstance().clan.leaderWithdrawFunds;
        }

        @Override
        public boolean isLeaderRecieveDisbandFunds() {
            return Config.getInstance().clan.leaderRecieveDisbandFunds;
        }

        @Override
        public int getChargeRentDays() {
            return Config.getInstance().clan.chargeRentDays;
        }

        @Override
        public boolean isEvictNonpayers() {
            return Config.getInstance().clan.evictNonpayers;
        }

        @Override
        public boolean isEvictNonpayerAdmins() {
            return Config.getInstance().clan.evictNonpayerAdmins;
        }

        @Override
        public String getMaxRentFormula() {
            return Config.getInstance().clan.maxRentFormula;
        }

        @Override
        public int getDynmapBorderWeight() {
            return Config.getInstance().dynmap.dynmapBorderWeight;
        }

        @Override
        public double getDynmapBorderOpacity() {
            return Config.getInstance().dynmap.dynmapBorderOpacity;
        }

        @Override
        public double getDynmapFillOpacity() {
            return Config.getInstance().dynmap.dynmapFillOpacity;
        }
    };
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
    private static IPaymentHandler paymentHandler = new PaymentHandlerDummy();
    private static IPermissionHandler permissionManager;
    private boolean validJar = true;

    public static Logger getLogger() {
        return LOGGER;
    }

    public static IConfig getConfig() {
        return config;
    }

    public static IPaymentHandler getPaymentHandler(){
        return paymentHandler;
    }

    private static void setPaymentHandler(IPaymentHandler paymentHandler) {
        ClansModContainer.paymentHandler = paymentHandler;
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

        if(!validJar)
            ClansModContainer.getMinecraftHelper().getLogger().error("The jar's signature is invalid! Please redownload from "+ Objects.requireNonNull(Loader.instance().activeModContainer()).getUpdateUrl());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event){
        getDynmapCompat().init();
        getProtectionCompat().init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event){
        if(getMinecraftHelper().isPluginLoaded("grandeconomy"))
            setPaymentHandler(new PaymentHandlerGE());
        else if(getMinecraftHelper().isPluginLoaded("spongeapi"))
            setPaymentHandler(new PaymentHandlerSponge());
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

    @Mod.EventHandler
    public void invalidFingerprint(FMLFingerprintViolationEvent e) {
        if(!e.isDirectory())
            validJar = false;
    }
}
