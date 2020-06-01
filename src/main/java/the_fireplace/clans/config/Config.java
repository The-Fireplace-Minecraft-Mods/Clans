package the_fireplace.clans.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.common.collect.Lists;
import the_fireplace.clans.Clans;
import the_fireplace.clans.config.constraints.RangeDecimal;
import the_fireplace.clans.config.constraints.RangeNumber;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class Config {
    public static final File configFile = new File("config", Clans.MODID +".conf");
    private static Config instance = null;

    public static Config getInstance() {
        if(instance == null) {
            instance = new Config();
            instance.load();
        }
        return instance;
    }

    @Comment("General config values to control the mod as a whole.")
    public General general = new General();
    public static class General {
        @Comment("Server locale - the client's locale takes precedence if Clans is installed there.")
        public String locale = "en_us";
        @Comment("A list of Fake Players that are allowed to build, destroy, interact, and injure things on claimed land. These are typically machines added by mods. If it contains a *, this list is a blacklist, so everything is allowed by default. Otherwise, it is a whitelist.")
        public Collection<String> tolerableFakePlayers = Lists.newArrayList("*");
        @Comment("Dump FakePlayer names to the console/log as they're discovered. Used to find out what to use to enable/disable a FakePlayer in tolerableFakePlayers")
        public boolean fakePlayerDump = false;
        @Comment("Whether Forge takes precedence over Sponge when finding permissions. Set this to true if your permissions manager uses Forge.")
        public boolean forgePermissionPrecedence = false;
        @Comment("Allow protection against breaking and placing blocks on claimed land.")
        public boolean allowBuildProtection = true;
        @Comment("Allow protection against interacting on claimed land.")
        public boolean allowInteractProtection = true;
        @Comment("Allow protection against injury on claimed land.")
        public boolean allowInjuryProtection = true;
    }

    @Comment("Config values related to clans, which would not be able to be overridden on a per-clan basis.")
    public GeneralClanSettings generalClan = new GeneralClanSettings();
    public static class GeneralClanSettings {
        @Comment("Allow players to be a member of multiple clans at once.")
        public boolean allowMultiClanMembership = true;
        @Comment("Default clan to put players in when they first join the server. Leave empty for no clan.")
        // Don't use empty string because night-config's HOCON writer is borked
        // https://github.com/TheElectronWill/night-config/issues/82
        public String serverDefaultClan = "N/A";
        @Comment("Cost of forming a clan. This requires a compatible economy to be installed.")
        @RangeDecimal(min=0)
        public double formClanCost = 0;
        @Comment("Initial amount in a clan account's balance when it is formed. This requires a compatible economy to be installed.")
        @RangeDecimal(min=0)
        public double formClanBankAmount = 0;
        @Comment("Maximum clan name length. Larger values allow more characters to be typed for the clan name, but also increase the chance of clans making their name hard to type in an attempt to avoid getting raided. Set to 0 for no limit.")
        @RangeNumber(min=0)
        public int maxNameLength = 32;
    }

    @Comment("Config values related to properties of specific clans, which can currently or potentially in the future be overridden on a per-clan basis.")
    public Clan clan = new Clan();
    public static class Clan {
        @Comment("Clan prefix for chat. If the player is in multiple clans, it uses the default clan. Make empty for no prefix. %s is where the clan name will go.")
        public String chatPrefix = "[%s]";
        @Comment("Allow clans to have multiple leaders.")
        public boolean multipleClanLeaders = true;
        @Comment("The amount of time, in seconds, the player must wait after typing /clan home before being teleported. Set to -1 to disable /clan home.")
        @RangeNumber(min=-1)
        public int clanHomeWarmupTime = 0;
        @Comment("The amount of time, in seconds, the player must wait after teleporting to the clan home before they can use /clan home again.")
        @RangeNumber(min=0)
        public int clanHomeCooldownTime = 0;
        @Comment("Max claims per clan. Set to 0 for infinite.")
        @RangeNumber(min=0)
        public int maxClaims = 0;//TODO max claims formula based
        @Comment("If enabled, multiplies the max claim count by the number of players in the clan.")
        public boolean multiplyMaxClaimsByPlayers = true;
        @Comment("Value or formula for the cost of disbanding a clan. See https://gist.github.com/The-Fireplace/2b6e21b1892bc5eafc4c70ab49ed3505 for formula writing details.")
        public String disbandFeeFormula = "2^(m-0.25)";
        @Comment("Should the player spawn at the clan home if they don't have a bed?")
        public boolean clanHomeFallbackSpawn = true;
        @Comment("Value or formula for cost of claiming a chunk. This requires a compatible economy to be installed. See https://gist.github.com/The-Fireplace/2b6e21b1892bc5eafc4c70ab49ed3505 for formula writing details.")
        public String claimChunkCostFormula = "0";
        @Comment("Reduced cost of claiming a chunk for the first x claims. This requires a compatible economy to be installed.")
        @RangeDecimal(min=0)
        public double reducedClaimChunkCost = 0;
        @Comment("Use the reduced cost for this many claims. This requires a compatible economy to be installed.")
        @RangeNumber(min=0)
        public int reducedCostClaimCount = 0;
        @Comment("How often to charge clans upkeep(in days). Set to 0 to disable the need for upkeep. This requires a compatible economy to be installed.")
        @RangeNumber(min=0)
        public int clanUpkeepDays = 0;
        @Comment("Value or formula for amount to charge a clan for upkeep. This requires a compatible economy to be installed. See https://gist.github.com/The-Fireplace/2b6e21b1892bc5eafc4c70ab49ed3505 for formula writing details.")
        public String clanUpkeepCostFormula = "0*c";
        @Comment("Disband the clan when it can't afford upkeep. This requires a compatible economy to be installed.")
        public boolean disbandNoUpkeep = false;
        @Comment("Allow the clan leader to withdraw funds from the clan bank account. This requires a compatible economy to be installed.")
        public boolean leaderWithdrawFunds = false;
        @Comment("When enabled, remaining clan funds go to the clan leader when the clan is disbanded. When disabled, remaining clan funds get split evenly among all clan members when the clan is disbanded. This requires a compatible economy to be installed.")
        public boolean leaderRecieveDisbandFunds = true;
        @Comment("Frequency to charge clan members rent to go into the clan bank account (in days). If enabled, allows clan leaders to set the amount for their clans. Set to 0 to disable clan rent. This requires a compatible economy to be installed.")
        @RangeNumber(min=0)
        public int chargeRentDays = 0;
        @Comment("Kick clan members out who can't afford rent. This will not kick out leaders. This requires a compatible economy to be installed.")
        public boolean evictNonpayers = false;
        @Comment("Kick clan admins out who can't afford rent. This will not kick out leaders. This requires a compatible economy to be installed.")
        public boolean evictNonpayerAdmins = false;
        @Comment("Value or formula for maximum amount of rent a clan can charge. Set to 0 for no maximum. This requires a compatible economy to be installed. See https://gist.github.com/The-Fireplace/2b6e21b1892bc5eafc4c70ab49ed3505 for formula writing details.")
        public String maxRentFormula = "0*c";
    }

    @Comment("Config values related to raiding.")
    public Raid raid = new Raid();
    public static class Raid {
        @Comment("Offset the maximum number of raiders by this much when determining how many people can join a raiding party. Formula is: (# raiders) - (maxRaiderOffset) <= (# defenders)")
        public int maxRaidersOffset = 0;
        @Comment("Maximum duration a raid can last for, in minutes. If set to 0, raids will be disabled.")
        @RangeNumber(min=0,max=Integer.MAX_VALUE/60)
        public int maxRaidDuration = 30;
        @Comment("The amount of time the defenders are given to prepare for a raid, in seconds.")
        @RangeNumber(min=0)
        public int raidBufferTime = 90;
        @Comment("Amount of time before the end of the raid to make all defenders glow, in minutes.")
        @RangeNumber(min=0,max=Integer.MAX_VALUE/60)
        public int remainingTimeToGlow = 10;
        @Comment("Maximum amount of consecutive time raiding parties can remain outside their target's territory, in seconds.")
        @RangeNumber(min=0)
        public int maxAttackerAbandonmentTime = 30;
        @Comment("Maximum amount of consecutive time defending clans can remain outside their territory during a raid, in seconds.")
        @RangeNumber(min=0)
        public int maxClanDesertionTime = 60;
        @Comment("Amount of shield given to the defending clan after a raid, in hours.")
        @RangeNumber(min=0)
        public int defenseShield = 24*5;
        @Comment("Amount of shield given to newly formed clans, in hours.")
        @RangeNumber(min=0)
        public int initialShield = 24*3;
        @Comment("Prevents reclaiming TNT that was placed while raiding.")
        public boolean noReclaimTNT = true;
        @Comment("Raid break speed multiplier")
        @RangeDecimal(min=0.0, max=10.0)
        public double raidBreakSpeedMultiplier = 1.0;
        @Comment("This option disables rollback of raids.")
        public boolean disableRaidRollback = false;
        @Comment("Controls if stealing from containers(even locked ones) is allowed during raids. This theft does not get rolled back at the end of the raid.")
        public boolean enableStealing = false;
        @Comment("A list of items allowed in a raid. If it contains a *, this list is a blacklist, so everything except bedrock is allowed by default. Otherwise, it is a whitelist.")
        public Collection<String> raidItemList = Lists.newArrayList("*", "minecraft:bedrock");
        @Comment("If enabled, raiders will be teleported outside the raid target's home territory when they start the raid.")
        public boolean teleportOnRaidStart = true;
        @Comment("Value or formula for cost of starting a raid. This requires a compatible economy to be installed. See https://gist.github.com/The-Fireplace/2b6e21b1892bc5eafc4c70ab49ed3505 for formula writing details.")
        public String startRaidCostFormula = "0*c";
        @Comment("Value or formula for reward for winning a raid. This requires a compatible economy to be installed. See https://gist.github.com/The-Fireplace/2b6e21b1892bc5eafc4c70ab49ed3505 for formula writing details.")
        public String winRaidAmountFormula = "0*m";
        @Comment("If enabled, rewards will increase as a clan gets repeatedly defeated. This requires a compatible economy to be installed.")
        public boolean increasingRewards = true;
        @Comment("Win-Loss Ratio threshold for decreasing the clan's reward multiplier - Helps prevent rich clans from hiring people to purposely fail a raid and reduce their reward multiplier. This requires a compatible economy to be installed.")
        @RangeDecimal(min=0)
        public double wlrThreshold = 0.66;
        @Comment("Formula to increase the clan's reward multiplier to after a clan loses to raiders. This requires increasingRewards to do anything. See https://gist.github.com/The-Fireplace/2b6e21b1892bc5eafc4c70ab49ed3505 for formula writing details.")
        public String multiplierIncreaseFormula = "m^1.05";
        @Comment("Formula to decrease the clan's reward multiplier to after a clan wins against raiders. This requires increasingRewards to do anything. This formula will not make the multiplier go below 1. See https://gist.github.com/The-Fireplace/2b6e21b1892bc5eafc4c70ab49ed3505 for formula writing details.")
        public String multiplierDecreaseFormula = "m^0.95";
    }

    @Comment("Config values related to protecting things.")
    public Protection protection = new Protection();
    public static class Protection {
        @Comment("Minimum number of blocks between clan homes.")
        @RangeNumber(min=0)
        public int minClanHomeDist = 320;
        @Comment("This multiplied by the minimum clan home distance is how far away from other clans' homes to make the initial claim for a clan.")
        @RangeDecimal(min=0)
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
    public static class Censor {
        @Comment("Censor clan names before they are set. This requires Chat Censor to do anything.")
        public boolean censorClanNames = true;
        @Comment("Censor clan descriptions before they are set. This is a bit excessive since currently Chat Censor will censor the description before it reaches the user anyways, but good if something else uses the clan description that is not fixed by Chat Censor. This requires Chat Censor to do anything.")
        public boolean censorClanDescriptions = false;
        @Comment("Censor clan names and descriptions that get sent to Dynmap. This requires Chat Censor and Dynmap to do anything.")
        public boolean censorDynmapDetails = true;
    }

    @Comment("Config values related to integration with Dynmap.")
    public Dynmap dynmap = new Dynmap();
    public static class Dynmap {
        @Comment("The weight of the dynmap border for claims. This requires Dynmap to be installed.")
        @RangeNumber(min=0)
        public int dynmapBorderWeight = 0;
        @Comment("The opacity of the border for claims. 0.0=0%, 1.0=100%. This requires Dynmap to be installed.")
        @RangeDecimal(min=0, max=1)
        public double dynmapBorderOpacity = 0.9;
        @Comment("The opacity of the fill color for claims. 0.0=0%, 1.0=100%. This requires Dynmap to be installed.")
        @RangeDecimal(min=0, max=1)
        public double dynmapFillOpacity = 0.75;
    }

    public void load() {
        if(!configFile.exists())
            createDefaultConfigFile();
        else {
            try {
                CommentedFileConfig cfg = CommentedFileConfig.of(configFile);
                cfg.load();
                Map<Field, List<Field>> toAdd = new HashMap<>();
                for(Field f: getClass().getFields()) {
                    if(Modifier.isStatic(f.getModifiers()) || Modifier.isFinal(f.getModifiers()))
                        continue;
                    Object obj = cfg.get(f.getName());
                    com.electronwill.nightconfig.core.Config subcfg;
                    if(obj instanceof com.electronwill.nightconfig.core.Config) {
                        subcfg = (com.electronwill.nightconfig.core.Config) obj;
                        for (Field f1 : f.getType().getFields()) {
                            if (Modifier.isStatic(f1.getModifiers()) || Modifier.isFinal(f1.getModifiers()))
                                continue;
                            Object value = subcfg.get(f1.getName());
                            if(value != null) {
                                //Ensure that values are in an acceptable range
                                RangeDecimal decRange = f1.getAnnotation(RangeDecimal.class);
                                RangeNumber numRange = f1.getAnnotation(RangeNumber.class);
                                Object prevVal = value;
                                if(numRange != null)
                                    value = Math.max(numRange.min(), Math.min(numRange.max(), Long.parseLong(String.valueOf(value))));
                                if(decRange != null)
                                    value = Math.max(decRange.min(), Math.min(decRange.max(), Double.parseDouble(String.valueOf(value))));
                                if((numRange != null || decRange != null) && Double.parseDouble(String.valueOf(value)) - Double.parseDouble(String.valueOf(prevVal)) != 0)
                                    Clans.getLogger().warn("Adjusted value of {} from {} to {} due to Range constraint.", f1.getName(), prevVal, value);
                                //Apply appropriate conversions to avoid ClassCastExceptions
                                if(f1.getType().equals(String.class))
                                    value = String.valueOf(value);
                                else if(f1.getType().equals(double.class) || f1.getType().equals(Double.class))
                                    value = Double.parseDouble(String.valueOf(value));
                                else if(f1.getType().equals(float.class) || f1.getType().equals(Float.class))
                                    value = Float.parseFloat(String.valueOf(value));
                                else if(f1.getType().equals(long.class) || f1.getType().equals(Long.class))
                                    value = Long.parseLong(String.valueOf(value));
                                else if(f1.getType().equals(int.class) || f1.getType().equals(Integer.class))
                                    value = Integer.parseInt(String.valueOf(value));
                                else if(f1.getType().equals(short.class) || f1.getType().equals(Short.class))
                                    value = Short.parseShort(String.valueOf(value));
                                else if(f1.getType().equals(byte.class) || f1.getType().equals(Byte.class))
                                    value = Byte.parseByte(String.valueOf(value));
                                //Put the value in the field
                                f1.set(f.get(this), value);
                            } else {
                                if(!toAdd.containsKey(f))
                                    toAdd.put(f, new ArrayList<>());
                                toAdd.get(f).add(f1);
                            }
                        }
                    } else {
                        toAdd.put(f, null);
                    }
                }
                //Now add the missing fields from the default config, if any, then save
                if(!toAdd.isEmpty()) {
                    for(Field f: toAdd.keySet()) {
                        CommentedConfig subcfg = cfg.get(f.getName());
                        if(subcfg == null)
                            subcfg = cfg.createSubConfig();
                        for(Field f1: toAdd.get(f) != null ? toAdd.get(f) : Lists.newArrayList(f.getType().getFields())) {
                            if(Modifier.isStatic(f1.getModifiers()) || Modifier.isFinal(f1.getModifiers()))
                                continue;
                            Comment comment = f1.getAnnotation(Comment.class);
                            if(comment != null)
                                subcfg.setComment(f1.getName(), comment.value());
                            subcfg.add(f1.getName(), f1.get(f.get(this)));
                        }
                        Comment comment = f.getAnnotation(Comment.class);
                        if(comment != null)
                            cfg.setComment(f.getName(), comment.value());
                        cfg.set(f.getName(), subcfg.unmodifiable());
                    }
                    cfg.save();
                }
            } catch(IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void createDefaultConfigFile() {
        try {
            //noinspection ResultOfMethodCallIgnored
            configFile.getParentFile().mkdirs();
            CommentedFileConfig cfg = CommentedFileConfig.of(configFile);
            for(Field f: getClass().getFields()) {
                if(Modifier.isStatic(f.getModifiers()) || Modifier.isFinal(f.getModifiers()))
                    continue;
                CommentedConfig subcfg = cfg.createSubConfig();
                for(Field f1: f.getType().getFields()) {
                    if(Modifier.isStatic(f1.getModifiers()) || Modifier.isFinal(f1.getModifiers()))
                        continue;
                    Comment comment = f1.getAnnotation(Comment.class);
                    if(comment != null)
                        subcfg.setComment(f1.getName(), comment.value());
                    subcfg.add(f1.getName(), f1.get(f.get(this)));
                }
                Comment comment = f.getAnnotation(Comment.class);
                if(comment != null)
                    cfg.setComment(f.getName(), comment.value());
                cfg.set(f.getName(), subcfg.unmodifiable());
            }
            cfg.save();
        } catch(IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
