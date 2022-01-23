package dev.the_fireplace.clans.legacy.config;

import blue.endless.jankson.Comment;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.common.collect.Lists;
import dev.the_fireplace.clans.legacy.ClansModContainer;
import dev.the_fireplace.clans.legacy.config.constraints.RangeDecimal;
import dev.the_fireplace.clans.legacy.config.constraints.RangeNumber;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class Config
{
    public static final File configFile = new File("config", ClansModContainer.MODID + ".conf");
    private static Config instance = null;

    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
            instance.load();
        }
        return instance;
    }

    public General general = new General();

    public static class General
    {
        @Comment("Allow protection against breaking and placing blocks on claimed land.")
        public boolean allowBuildProtection = true;
        @Comment("Allow protection against interacting on claimed land.")
        public boolean allowInteractProtection = true;
        @Comment("Allow protection against injury on claimed land.")
        public boolean allowInjuryProtection = true;
    }

    public GeneralClanSettings generalClan = new GeneralClanSettings();

    public static class GeneralClanSettings
    {
        public boolean allowMultiClanMembership = true;
        public String serverDefaultClan = "N/A";
        //TODO min 0 for these three
        public double formClanCost = 0;
        public double formClanBankAmount = 0;
        public int maxNameLength = 32;
    }

    public Clan clan = new Clan();

    public static class Clan
    {
        public String chatPrefix = "[%s]";
        public boolean multipleClanLeaders = true;
        @RangeNumber(min = -1)
        public int clanHomeWarmupTime = 0;
        @RangeNumber(min = 0)
        public int clanHomeCooldownTime = 0;
        public String maxClaimCountFormula = "-1*p";
        public String disbandFeeFormula = "0";
        public boolean clanHomeFallbackSpawn = true;
        public String claimChunkCostFormula = "0";
        @RangeNumber(min = 0)
        public int clanUpkeepDays = 0;
        public String clanUpkeepCostFormula = "0*c";
        public boolean disbandNoUpkeep = false;
        public boolean leaderWithdrawFunds = false;
        public boolean leaderRecieveDisbandFunds = true;
        @RangeNumber(min = 0)
        public int chargeRentDays = 0;
        public boolean evictNonpayers = false;
        public boolean evictNonpayerAdmins = false;
        public String maxRentFormula = "0*c";
    }

    @Comment("Config values related to raiding.")
    public Raid raid = new Raid();

    public static class Raid
    {
        public String maxRaidersOffset = "0";
        @RangeNumber(min = 0, max = Integer.MAX_VALUE / 60)
        public int maxRaidDuration = 30;
        @RangeNumber(min = 0)
        public int raidBufferTime = 90;
        @RangeNumber(min = 0, max = Integer.MAX_VALUE / 60)
        public int remainingTimeToGlow = 10;
        @RangeNumber(min = 0)
        public int maxAttackerAbandonmentTime = 30;
        @RangeNumber(min = 0)
        public int maxClanDesertionTime = 60;
        @RangeNumber(min = 0)
        public int defenseShield = 24 * 5;
        @RangeNumber(min = 0)
        public int initialShield = 24 * 3;
        public boolean noReclaimTNT = true;
        @RangeDecimal(min = 0.0, max = 10.0)
        public double raidBreakSpeedMultiplier = 1.0;
        public boolean disableRaidRollback = false;
        public boolean enableStealing = false;
        public Collection<String> raidItemList = Lists.newArrayList("*", "minecraft:bedrock");
        public boolean teleportOnRaidStart = true;
        public String startRaidCostFormula = "0*c";
        @Comment("Value or formula for reward for winning a raid. This requires a compatible economy to be installed. See https://gist.github.com/The-Fireplace/2b6e21b1892bc5eafc4c70ab49ed3505 for formula writing details.")
        public String winRaidAmountFormula = "0*m";
        @Comment("If enabled, rewards will increase as a clan gets repeatedly defeated. This requires a compatible economy to be installed.")
        public boolean increasingRewards = true;
        @Comment("Win-Loss Ratio threshold for decreasing the clan's reward multiplier - Helps prevent rich clans from hiring people to purposely fail a raid and reduce their reward multiplier. This requires a compatible economy to be installed.")
        @RangeDecimal(min = 0)
        public double wlrThreshold = 0.66;
        @Comment("Formula to increase the clan's reward multiplier to after a clan loses to raiders. This requires increasingRewards to do anything. See https://gist.github.com/The-Fireplace/2b6e21b1892bc5eafc4c70ab49ed3505 for formula writing details.")
        public String multiplierIncreaseFormula = "m^1.05";
        @Comment("Formula to decrease the clan's reward multiplier to after a clan wins against raiders. This requires increasingRewards to do anything. This formula will not make the multiplier go below 1. See https://gist.github.com/The-Fireplace/2b6e21b1892bc5eafc4c70ab49ed3505 for formula writing details.")
        public String multiplierDecreaseFormula = "m^0.95";
    }

    @Comment("Config values related to protecting things.")
    public Protection protection = new Protection();

    public static class Protection
    {
        @Comment("Minimum number of blocks between clan homes.")
        @RangeNumber(min = 0)
        public int minClanHomeDist = 320;
        @Comment("This multiplied by the minimum clan home distance is how far away from other clans' homes to make the initial claim for a clan.")
        @RangeDecimal(min = 0)
        public double initialClaimSeparationMultiplier = 1.25;
        @Comment("If set to false, players will be warned if making the initial claim within the claim separation range, but not prevented from making the claim if they want to.")
        public boolean enforceInitialClaimSeparation = true;
        @Comment("Force clans to have connected claims.")
        public boolean forceConnectedClaims = true;
        @Comment("What algorithm is used for the connected claim check. 'quick' is generally quicker, but may falsely prevent claim abandonment. 'smart' is generally slower, but enforces connected claims the best.")
        public String connectedClaimCheck = "smart";
        @Comment("Prevents other clans from claiming around the edges of a clan's territory. When a chunk of land is between two clans' territories, the first clan to be near it takes precedence.")
        public boolean enableBorderlands = true;
        @Comment("Prevent mobs from spawning on claimed land")
        public boolean preventMobsOnClaims = true;
        @Comment("Prevents mobs from spawning in borderlands. Requires enableBorderlands and preventMobsOnClaims.")
        public boolean preventMobsOnBorderlands = true;
        @Comment("Always allow TNT to ignite other TNT, regardless of whether other blocks are protected.")
        public boolean chainTNT = true;
        @Comment("A list of blocks that are able to be locked.")
        public Collection<String> lockableBlocks = Lists.newArrayList(
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
            "minecraft:lever",
            "minecraft:stone_pressure_plate",
            "minecraft:wooden_pressure_plate",
            "minecraft:stone_button",
            "minecraft:trapdoor",
            "minecraft:fence_gate",
            "minecraft:wooden_button",
            "minecraft:trapped_chest",
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
            "minecraft:brewing_stand"
        );
        @Comment("Protect the wilderness from damage above a specific Y level")
        public boolean protectWilderness = false;
        @Comment("Minimum Y level to protect with the Protect Wilderness option, inclusive. Set to a negative number to use sea level.")
        public int minWildernessY = -1;
        @Comment("A list of dimensions players are allowed to claim in. If it contains a *, this list is a blacklist. Otherwise, it is a whitelist, so by default it is a whitelist containing the overworld and the nether.")
        public Collection<String> claimableDimensions = Lists.newArrayList("overworld", "the_nether");
    }

    @Comment("Config values related to integration with Chat Censor.")
    public Censor chatCensor = new Censor();

    public static class Censor
    {
        @Comment("Censor clan names before they are set. This requires Chat Censor to do anything.")
        public boolean censorClanNames = true;
        @Comment("Censor clan descriptions before they are set. This is a bit excessive since currently Chat Censor will censor the description before it reaches the user anyways, but good if something else uses the clan description that is not fixed by Chat Censor. This requires Chat Censor to do anything.")
        public boolean censorClanDescriptions = false;
        @Comment("Censor clan names and descriptions that get sent to Dynmap. This requires Chat Censor and Dynmap to do anything.")
        public boolean censorDynmapDetails = true;
    }

    @Comment("Config values related to integration with Dynmap.")
    public Dynmap dynmap = new Dynmap();

    public static class Dynmap
    {
        @Comment("The weight of the dynmap border for claims. This requires Dynmap to be installed.")
        @RangeNumber(min = 0)
        public int dynmapBorderWeight = 0;
        @Comment("The opacity of the border for claims. 0.0=0%, 1.0=100%. This requires Dynmap to be installed.")
        @RangeDecimal(min = 0, max = 1)
        public double dynmapBorderOpacity = 0.9;
        @Comment("The opacity of the fill color for claims. 0.0=0%, 1.0=100%. This requires Dynmap to be installed.")
        @RangeDecimal(min = 0, max = 1)
        public double dynmapFillOpacity = 0.75;
        @Comment("Max number of connected claims Dynmap can show at once. Try lowering this if you're getting a StackOverflowException crash with Dynmap installed. Claims larger than this many chunks will be displayed inaccurately. Set to 0 for no limit.")
        @RangeNumber(min = 0)
        public int maxDisplayedConnectedClaims = Integer.MAX_VALUE;
    }

    public void load() {
        if (!configFile.exists()) {
            createDefaultConfigFile();
        } else {
            try {
                CommentedFileConfig cfg = CommentedFileConfig.of(configFile);
                cfg.load();
                Map<Field, List<Field>> toAdd = new HashMap<>();
                for (Field f : getClass().getFields()) {
                    if (Modifier.isStatic(f.getModifiers()) || Modifier.isFinal(f.getModifiers())) {
                        continue;
                    }
                    Object obj = cfg.get(f.getName());
                    com.electronwill.nightconfig.core.Config subcfg;
                    if (obj instanceof com.electronwill.nightconfig.core.Config) {
                        subcfg = (com.electronwill.nightconfig.core.Config) obj;
                        for (Field f1 : f.getType().getFields()) {
                            if (Modifier.isStatic(f1.getModifiers()) || Modifier.isFinal(f1.getModifiers())) {
                                continue;
                            }
                            Object value = subcfg.get(f1.getName());
                            if (value != null) {
                                //Ensure that values are in an acceptable range
                                RangeDecimal decRange = f1.getAnnotation(RangeDecimal.class);
                                RangeNumber numRange = f1.getAnnotation(RangeNumber.class);
                                Object prevVal = value;
                                if (numRange != null) {
                                    value = Math.max(numRange.min(), Math.min(numRange.max(), Long.parseLong(String.valueOf(value))));
                                }
                                if (decRange != null) {
                                    value = Math.max(decRange.min(), Math.min(decRange.max(), Double.parseDouble(String.valueOf(value))));
                                }
                                if ((numRange != null || decRange != null) && Double.parseDouble(String.valueOf(value)) - Double.parseDouble(String.valueOf(prevVal)) != 0) {
                                    ClansModContainer.getLogger().warn("Adjusted value of {} from {} to {} due to Range constraint.", f1.getName(), prevVal, value);
                                }
                                //Apply appropriate conversions to avoid ClassCastExceptions
                                if (f1.getType().equals(String.class)) {
                                    value = String.valueOf(value);
                                } else if (f1.getType().equals(double.class) || f1.getType().equals(Double.class)) {
                                    value = Double.parseDouble(String.valueOf(value));
                                } else if (f1.getType().equals(float.class) || f1.getType().equals(Float.class)) {
                                    value = Float.parseFloat(String.valueOf(value));
                                } else if (f1.getType().equals(long.class) || f1.getType().equals(Long.class)) {
                                    value = Long.parseLong(String.valueOf(value));
                                } else if (f1.getType().equals(int.class) || f1.getType().equals(Integer.class)) {
                                    value = Integer.parseInt(String.valueOf(value));
                                } else if (f1.getType().equals(short.class) || f1.getType().equals(Short.class)) {
                                    value = Short.parseShort(String.valueOf(value));
                                } else if (f1.getType().equals(byte.class) || f1.getType().equals(Byte.class)) {
                                    value = Byte.parseByte(String.valueOf(value));
                                }
                                //Put the value in the field
                                f1.set(f.get(this), value);
                            } else {
                                if (!toAdd.containsKey(f)) {
                                    toAdd.put(f, new ArrayList<>());
                                }
                                toAdd.get(f).add(f1);
                            }
                        }
                    } else {
                        toAdd.put(f, null);
                    }
                }
                //Now add the missing fields from the default config, if any, then save
                if (!toAdd.isEmpty()) {
                    for (Field f : toAdd.keySet()) {
                        CommentedConfig subcfg = cfg.get(f.getName());
                        if (subcfg == null) {
                            subcfg = cfg.createSubConfig();
                        }
                        for (Field f1 : toAdd.get(f) != null ? toAdd.get(f) : Lists.newArrayList(f.getType().getFields())) {
                            if (Modifier.isStatic(f1.getModifiers()) || Modifier.isFinal(f1.getModifiers())) {
                                continue;
                            }
                            Comment comment = f1.getAnnotation(Comment.class);
                            if (comment != null) {
                                subcfg.setComment(f1.getName(), comment.value());
                            }
                            subcfg.add(f1.getName(), f1.get(f.get(this)));
                        }
                        Comment comment = f.getAnnotation(Comment.class);
                        if (comment != null) {
                            cfg.setComment(f.getName(), comment.value());
                        }
                        cfg.set(f.getName(), subcfg.unmodifiable());
                    }
                    cfg.save();
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void createDefaultConfigFile() {
        try {
            //noinspection ResultOfMethodCallIgnored
            configFile.getParentFile().mkdirs();
            CommentedFileConfig cfg = CommentedFileConfig.of(configFile);
            for (Field f : getClass().getFields()) {
                if (Modifier.isStatic(f.getModifiers()) || Modifier.isFinal(f.getModifiers())) {
                    continue;
                }
                CommentedConfig subcfg = cfg.createSubConfig();
                for (Field f1 : f.getType().getFields()) {
                    if (Modifier.isStatic(f1.getModifiers()) || Modifier.isFinal(f1.getModifiers())) {
                        continue;
                    }
                    Comment comment = f1.getAnnotation(Comment.class);
                    if (comment != null) {
                        subcfg.setComment(f1.getName(), comment.value());
                    }
                    subcfg.add(f1.getName(), f1.get(f.get(this)));
                }
                Comment comment = f.getAnnotation(Comment.class);
                if (comment != null) {
                    cfg.setComment(f.getName(), comment.value());
                }
                cfg.set(f.getName(), subcfg.unmodifiable());
            }
            cfg.save();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
