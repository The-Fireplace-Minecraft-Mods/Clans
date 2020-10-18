package the_fireplace.clans.model;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.*;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import the_fireplace.clans.Clans;
import the_fireplace.clans.api.event.ClanFormedEvent;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.cache.RaidingParties;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.commands.CommandClan;
import the_fireplace.clans.data.ClaimData;
import the_fireplace.clans.data.ClanDatabase;
import the_fireplace.clans.data.PlayerData;
import the_fireplace.clans.util.ClansEventManager;
import the_fireplace.clans.util.FormulaParser;
import the_fireplace.clans.util.JsonHelper;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class Clan {
    private boolean isChanged = false;
    private final File clanDataFile;

    private String clanName, clanBanner;
    private String description = TranslationUtil.getStringTranslation("clan.default_description");
    private final Map<UUID, EnumRank> members;
    private UUID clanId;
    private float homeX, homeY, homeZ;
    private boolean hasHome = false;
    private int homeDimension;
    private double rent;
    private int wins = 0, losses = 0;
    private long shield = Clans.getConfig().getInitialShield() * 60;
    private long rentTimestamp = System.currentTimeMillis() + Clans.getConfig().getChargeRentDays() * 1000L * 60L * 60L * 24L, upkeepTimestamp = System.currentTimeMillis() + Clans.getConfig().getClanUpkeepDays() * 1000L * 60L * 60L * 24L;
    private int color;
    private int textColor;
    private double multiplier = 1.0;
    private long cachedClaimCount = -1;

    public static final Map<String, EnumRank> DEFAULT_PERMISSIONS = Maps.newHashMap();
    public static final Map<String, Integer> DEFAULT_OPTIONS = Maps.newHashMap();

    static {
        for(Map.Entry<String, ClanSubCommand> entry: CommandClan.commands.entrySet())
            if(entry.getValue().getRequiredClanRank().greaterOrEquals(EnumRank.ADMIN) && !entry.getValue().getRequiredClanRank().equals(EnumRank.ANY))
                DEFAULT_PERMISSIONS.put(entry.getKey(), entry.getValue().getRequiredClanRank());
        DEFAULT_PERMISSIONS.put("access", EnumRank.MEMBER);
        DEFAULT_PERMISSIONS.put("interact", EnumRank.MEMBER);
        DEFAULT_PERMISSIONS.put("build", EnumRank.MEMBER);
        DEFAULT_PERMISSIONS.put("harmmob", EnumRank.MEMBER);
        DEFAULT_PERMISSIONS.put("harmanimal", EnumRank.MEMBER);
        DEFAULT_PERMISSIONS.put("lockadmin", EnumRank.LEADER);
        DEFAULT_PERMISSIONS.put("lock.private", EnumRank.MEMBER);
        DEFAULT_PERMISSIONS.put("lock.clan", EnumRank.MEMBER);
        DEFAULT_PERMISSIONS.put("lock.open", EnumRank.MEMBER);
        
        //Config option overrides
        DEFAULT_OPTIONS.put("maxclaims", -1);
        DEFAULT_OPTIONS.put("mobspawning", -1);
        DEFAULT_OPTIONS.put("claimcost", -1);
        //Custom properties
        DEFAULT_OPTIONS.put("mobdamage", 1);
        DEFAULT_OPTIONS.put("upkeepexemption", 0);
        DEFAULT_OPTIONS.put("dynmapvisible", 1);
        DEFAULT_OPTIONS.put("server", 0);
        DEFAULT_OPTIONS.put("pvp", -1);
    }

    private final Map<String, Integer> options = Maps.newHashMap();
    private final Map<String, EnumRank> permissions = Maps.newHashMap();
    private final Map<String, Map<UUID, Boolean>> permissionOverrides = Maps.newHashMap();
    
    private final Map<BlockPos, OrderedPair<EnumLockType, UUID>> locks = Maps.newHashMap();
    private final Map<BlockPos, Map<UUID, Boolean>> lockOverrides = Maps.newHashMap();

    private Map<String, Object> addonData = Maps.newHashMap();

    public Clan(String clanName, UUID leader) {
        this.clanName = TextStyles.stripFormatting(clanName);
        this.members = Maps.newHashMap();
        this.members.put(leader, EnumRank.LEADER);
        for(Map.Entry<String, EnumRank> perm: DEFAULT_PERMISSIONS.entrySet()) {
            permissions.put(perm.getKey(), perm.getValue());
            permissionOverrides.put(perm.getKey(), Maps.newHashMap());
        }
        for(Map.Entry<String, Integer> opt: DEFAULT_OPTIONS.entrySet())
            options.put(opt.getKey(), opt.getValue());
        do{
            this.clanId = UUID.randomUUID();
        } while(!ClanDatabase.addClan(this.clanId, this));
        clanDataFile = new File(ClanDatabase.clanDataLocation, clanId.toString()+".json");

        // Ensure that the starting balance of the account is 0, to prevent "free money" from the creation of a new bank account
        if (Clans.getPaymentHandler().getBalance(clanId) > 0)
            Clans.getPaymentHandler().deductAmount(Clans.getPaymentHandler().getBalance(clanId),clanId);

        Clans.getPaymentHandler().addAmount(Clans.getConfig().getFormClanBankAmount(), clanId);
        ClanCache.addPlayerClan(leader, this);
        if(!Clans.getConfig().isAllowMultiClanMembership())
            for(Clan clan: ClanDatabase.getClans())
                PlayerData.removeInvite(leader, clan.getId());
        rent = Math.min(FormulaParser.eval(Clans.getConfig().getMaxRentFormula(), this, 0)/ Clans.getConfig().getChargeRentDays(), FormulaParser.eval(Clans.getConfig().getClanUpkeepCostFormula(), this, 0)/ Clans.getConfig().getClanUpkeepDays())* Clans.getConfig().getChargeRentDays();
        color = new Random().nextInt(0xffffff);
        textColor = TextStyles.getNearestTextColor(color).getColorIndex();
        ClansEventManager.fireEvent(new ClanFormedEvent(leader, this));
        isChanged = true;
    }

    //region JsonObject conversions
    @SuppressWarnings("DuplicatedCode")
    public JsonObject toJsonObject() {
        JsonObject ret = new JsonObject();
        ret.addProperty("clanName", TextStyles.stripFormatting(clanName));
        ret.addProperty("clanBanner", clanBanner);
        ret.addProperty("clanDescription", TextStyles.stripFormatting(description));
        JsonArray members = new JsonArray();
        for(Map.Entry<UUID, EnumRank> entry : this.members.entrySet()) {
            JsonObject newEntry = new JsonObject();
            newEntry.addProperty("key", entry.getKey().toString());
            newEntry.addProperty("value", entry.getValue().toString());
            members.add(newEntry);
        }
        ret.add("members", members);
        ret.addProperty("clanId", clanId.toString());
        ret.addProperty("homeX", homeX);
        ret.addProperty("homeY", homeY);
        ret.addProperty("homeZ", homeZ);
        ret.addProperty("hasHome", hasHome);
        ret.addProperty("homeDimension", homeDimension);
        ret.addProperty("rent", rent);
        ret.addProperty("wins", wins);
        ret.addProperty("losses", losses);
        ret.addProperty("shield", shield);
        ret.addProperty("rentTimestamp", rentTimestamp);
        ret.addProperty("upkeepTimestamp", upkeepTimestamp);
        ret.addProperty("color", color);
        ret.addProperty("multiplier", multiplier);

        JsonArray permissions = new JsonArray();
        for(Map.Entry<String, EnumRank> entry: this.permissions.entrySet()) {
            JsonObject perm = new JsonObject();
            perm.addProperty("name", entry.getKey());
            perm.addProperty("value", entry.getValue().name());
            JsonArray overrides = new JsonArray();
            if(permissionOverrides.containsKey(entry.getKey()))
                for(Map.Entry<UUID, Boolean> override: permissionOverrides.get(entry.getKey()).entrySet()) {
                    JsonObject or = new JsonObject();
                    or.addProperty("player", override.getKey().toString());
                    or.addProperty("allowed", override.getValue());
                    overrides.add(or);
                }
            perm.add("overrides", overrides);
            permissions.add(perm);
        }
        ret.add("permissions", permissions);

        JsonArray locks = new JsonArray();
        for(Map.Entry<BlockPos, OrderedPair<EnumLockType, UUID>> entry: this.locks.entrySet()) {
            JsonObject lock = new JsonObject();
            lock.add("position", JsonHelper.toJsonObject(entry.getKey()));
            lock.addProperty("type", entry.getValue().getValue1().ordinal());
            lock.addProperty("owner", entry.getValue().getValue2().toString());
            JsonArray overrides = new JsonArray();
            if(lockOverrides.containsKey(entry.getKey()))
                for(Map.Entry<UUID, Boolean> override: lockOverrides.get(entry.getKey()).entrySet()) {
                    JsonObject or = new JsonObject();
                    or.addProperty("player", override.getKey().toString());
                    or.addProperty("allowed", override.getValue());
                    overrides.add(or);
                }
            lock.add("overrides", overrides);
            locks.add(lock);
        }
        ret.add("locks", locks);

        JsonArray options = new JsonArray();
        for(Map.Entry<String, Integer> entry: this.options.entrySet()) {
            JsonObject opt = new JsonObject();
            opt.addProperty("name", entry.getKey());
            opt.addProperty("value", entry.getValue());
            options.add(opt);
        }
        ret.add("options", options);

        JsonHelper.attachAddonData(ret, this.addonData);

        return ret;
    }

    public Clan(JsonObject obj) {
        this.clanName = TextStyles.stripFormatting(obj.get("clanName").getAsString());
        if(obj.has("clanBanner") && !(obj.get("clanBanner") instanceof JsonNull))
            this.clanBanner = obj.get("clanBanner").getAsString();
        this.description = TextStyles.stripFormatting(obj.get("clanDescription").getAsString());
        HashMap<UUID, EnumRank> newMembers = Maps.newHashMap();
        for(JsonElement entry: obj.get("members").getAsJsonArray())
            newMembers.put(UUID.fromString(entry.getAsJsonObject().get("key").getAsString()), EnumRank.valueOf(entry.getAsJsonObject().get("value").getAsString()));
        this.members = newMembers;
        this.clanId = UUID.fromString(obj.get("clanId").getAsString());
        this.homeX = obj.get("homeX").getAsFloat();
        this.homeY = obj.get("homeY").getAsFloat();
        this.homeZ = obj.get("homeZ").getAsFloat();
        this.hasHome = obj.get("hasHome").getAsBoolean();
        this.homeDimension = obj.get("homeDimension").getAsInt();
        this.rent = obj.get("rent").getAsDouble();
        this.wins = obj.get("wins").getAsInt();
        this.losses = obj.get("losses").getAsInt();
        this.shield = obj.get("shield").getAsLong();
        this.rentTimestamp = obj.get("rentTimestamp").getAsLong();
        this.upkeepTimestamp = obj.get("upkeepTimestamp").getAsLong();
        this.multiplier = obj.has("multiplier") ? obj.get("multiplier").getAsDouble() : 1;
        if(obj.has("color")) {
            this.color = obj.get("color").getAsInt();
            this.textColor = TextStyles.getNearestTextColor(color).getColorIndex();
        }
        for(Map.Entry<String, EnumRank> perm: DEFAULT_PERMISSIONS.entrySet()) {
            permissions.put(perm.getKey(), perm.getValue());
            permissionOverrides.put(perm.getKey(), Maps.newHashMap());
        }
        if(obj.has("permissions")) {
            for(JsonElement e: obj.getAsJsonArray("permissions")) {
                JsonObject perm = e.getAsJsonObject();
                if(!DEFAULT_PERMISSIONS.containsKey(perm.get("name").getAsString()))
                    continue;
                permissions.put(perm.get("name").getAsString(), EnumRank.valueOf(perm.get("value").getAsString()));
                permissionOverrides.put(perm.get("name").getAsString(), Maps.newHashMap());
                for(JsonElement o: perm.getAsJsonArray("overrides"))
                    permissionOverrides.get(perm.get("name").getAsString()).put(UUID.fromString(o.getAsJsonObject().get("player").getAsString()), o.getAsJsonObject().get("allowed").getAsBoolean());
            }
        }
        if(obj.has("locks")) {
            for(JsonElement e: obj.getAsJsonArray("locks")) {
                JsonObject lock = e.getAsJsonObject();
                BlockPos pos = JsonHelper.fromJsonObject(lock.get("position").getAsJsonObject());
                locks.put(pos, new OrderedPair<>(EnumLockType.values()[lock.get("type").getAsInt()], UUID.fromString(lock.get("owner").getAsString())));
                lockOverrides.put(pos, Maps.newHashMap());
                for(JsonElement o: lock.getAsJsonArray("overrides"))
                    lockOverrides.get(pos).put(UUID.fromString(o.getAsJsonObject().get("player").getAsString()), o.getAsJsonObject().get("allowed").getAsBoolean());
            }
        }
        for(Map.Entry<String, Integer> opt: DEFAULT_OPTIONS.entrySet())
            options.put(opt.getKey(), opt.getValue());
        if(obj.has("options")) {
            for(JsonElement e: obj.getAsJsonArray("options")) {
                JsonObject perm = e.getAsJsonObject();
                if(!DEFAULT_OPTIONS.containsKey(perm.get("name").getAsString()))
                    continue;
                options.put(perm.get("name").getAsString(), perm.get("value").getAsInt());
            }
        }

        addonData = JsonHelper.getAddonData(obj);
        clanDataFile = new File(ClanDatabase.clanDataLocation, clanId.toString()+".json");
    }
    //endregion

    //region save/load
    @Nullable
    public static Clan load(File file) {
        JsonParser jsonParser = new JsonParser();
        try {
            Object obj = jsonParser.parse(new FileReader(file));
            if(obj instanceof JsonObject) {
                return new Clan((JsonObject)obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void save() {
        if(!isChanged)
            return;

        try {
            FileWriter file = new FileWriter(clanDataFile);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(toJsonObject());
            file.write(json);
            file.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
        isChanged = false;
    }

    public File getClanDataFile() {
        return clanDataFile;
    }

    public void markChanged() {
        isChanged = true;
    }
    //endregion

    public Map<UUID, EnumRank> getMembers() {
        return Collections.unmodifiableMap(members);
    }

    public Collection<UUID> getLeaders() {
        Set<UUID> leaders = members.entrySet().stream()
            .filter(entry -> entry.getValue() == EnumRank.LEADER)
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
        return Collections.unmodifiableCollection(leaders);
    }

    public long getLeaderCount() {
        return members.entrySet().stream().filter(entry -> entry.getValue() == EnumRank.LEADER).count();
    }

    public double payLeaders(double totalAmount) {
        Collection<UUID> leaders = getLeaders();
        if(leaders.isEmpty())
            return totalAmount;
        totalAmount /= leaders.size();
        for(UUID leader: leaders)
            Clans.getPaymentHandler().addAmount(totalAmount, leader);
        return 0;
    }

    public Map<EntityPlayerMP, EnumRank> getOnlineMembers() {
        HashMap<EntityPlayerMP, EnumRank> online = Maps.newHashMap();
        for(Map.Entry<UUID, EnumRank> member: getMembers().entrySet()) {
            EntityPlayerMP player = Clans.getMinecraftHelper().getServer().getPlayerList().getPlayerByUUID(member.getKey());
            //noinspection ConstantConditions
            if(player != null)
                online.put(player, member.getValue());
        }
        return Collections.unmodifiableMap(online);
    }

    public Set<Map.Entry<EntityPlayerMP, EnumRank>> getOnlineSurvivalMembers() {
        return Collections.unmodifiableSet(getOnlineMembers().entrySet().stream().filter(e -> !e.getKey().isCreative() && !e.getKey().isSpectator()).collect(Collectors.toSet()));
    }

    public UUID getId() {
        return clanId;
    }

    public String getName() {
        return clanName;
    }

    public void setName(String clanName) {
        ClanCache.removeName(this.clanName);
        this.clanName = clanName;
        ClanCache.addName(this);
        markChanged();
    }

    public String getBanner() {
        return clanBanner;
    }

    public void setBanner(String clanBanner) {
        ClanCache.removeBanner(this.clanBanner);
        ClanCache.addBanner(clanBanner);
        this.clanBanner = clanBanner;
        markChanged();
    }

    public void setHome(BlockPos pos, int dimension) {
        this.homeX = pos.getX();
        this.homeY = pos.getY();
        this.homeZ = pos.getZ();
        this.hasHome = true;
        this.homeDimension = dimension;
        markChanged();
        ClanCache.setClanHome(this, pos);
    }

    public boolean hasHome() {
        return hasHome;
    }

    public void unsetHome() {
        hasHome = false;
        homeX = homeY = homeZ = 0;
        homeDimension = 0;
        ClanCache.clearClanHome(this);
        markChanged();
    }

    @Nullable
    public BlockPos getHome() {
        if(!hasHome)
            return null;
        return new BlockPos(homeX, homeY, homeZ);
    }

    public int getHomeDim() {
        return homeDimension;
    }

    public long getClaimCount() {
        cacheClaimCountIfNeeded();
        return cachedClaimCount;
    }

    public void incrementCachedClaimCount() {
        cacheClaimCountIfNeeded();
        cachedClaimCount++;
    }

    public void decrementCachedClaimCount() {
        cacheClaimCountIfNeeded();
        cachedClaimCount--;
    }

    private void cacheClaimCountIfNeeded() {
        if(cachedClaimCount < 0)
            cacheClaimCount();
    }

    /**
     * WARNING: Potentially costly
     */
    public void cacheClaimCount() {
        cachedClaimCount = ClaimData.getClaimedChunkCount(getId());
    }

    public long getMaxClaimCount() {
        if(options.get("maxclaims") > -1)
            return options.get("maxclaims");
        return Clans.getConfig().isMultiplyMaxClaimsByPlayers()
            ? getMemberCount() * Clans.getConfig().getMaxClaims()
            : Clans.getConfig().getMaxClaims();
    }

    public void setOption(String option, int value) {
        options.put(option, value);
    }

    public boolean isServer(){
        return options.get("server") == 1;
    }

    public void setServer(boolean server){
        this.options.put("server", server ? 1 : 0);
        markChanged();
    }

    /**
     * Get if PVP is allowed on this clan's land
     * @return
     * null for default PVP behavior (protect clan members and their entities, server clans' default is to disallow PVP)
     * FALSE for no PVP at all
     * TRUE for all PVP allowed
     */
    @Nullable
    public Boolean pvpAllowed() {
        return options.get("pvp") < 0 || options.get("pvp") > 1
            //the PVP option is outside the Boolean range, so we want null for default behavior unless it is a server clan
            ? (isServer() ? Boolean.FALSE : null)
            //Otherwise, the pvp option has been set to 0 or 1 which means we should return the appropriate Boolean
            : (options.get("pvp") == 1 ? Boolean.TRUE : Boolean.FALSE);
    }

    @Nullable
    public Boolean getMobSpawnOverride() {
        return options.get("mobspawning") < 0 ? null : options.get("mobspawning") == 1;
    }

    public boolean isMobDamageAllowed() {
        return options.get("mobdamage") == 1;
    }

    public double getClaimCost() {
        return getClaimCost(getClaimCount());
    }

    /**
     * Gets the cost of one claim when the clan has a certain number of claims
     */
    public double getClaimCost(long currentClaimCount) {
        int costOption = options.get("claimcost");
        if(costOption >= 0)
            return costOption;
        return currentClaimCount < Clans.getConfig().getReducedCostClaimCount()
                ? Clans.getConfig().getReducedChunkClaimCost()
                : FormulaParser.eval(Clans.getConfig().getClaimChunkCostFormula(), this, 0);
    }

    public boolean hasCustomClaimCost() {
        return options.get("claimcost") >= 0;
    }

    public boolean isUpkeepExempt() {
        return options.get("upkeepexemption") == 1;
    }

    public boolean isVisibleOnDynmap() {
        return options.get("dynmapvisible") == 1;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        markChanged();
    }

    public int getMemberCount(){
        return members.size();
    }

    public void addMember(UUID player) {
        this.members.put(player, EnumRank.MEMBER);
        ClanCache.addPlayerClan(player, this);
        PlayerData.removeInvite(player, getId());
        if(!Clans.getConfig().isAllowMultiClanMembership() && !isServer())
            for(Clan clan: ClanDatabase.getClans())
                PlayerData.removeInvite(player, clan.getId());
        Clans.getDynmapCompat().refreshTooltip(this);
        markChanged();
    }

    public void addMember(UUID player, EnumRank rank) {
        boolean prevHadMember = members.containsKey(player);
        this.members.put(player, rank);
        if(!prevHadMember) {
            ClanCache.addPlayerClan(player, this);
            PlayerData.removeInvite(player, getId());
            if(!Clans.getConfig().isAllowMultiClanMembership() && !isServer())
                for(Clan clan: ClanDatabase.getClans())
                    PlayerData.removeInvite(player, clan.getId());
            Clans.getDynmapCompat().refreshTooltip(this);
        }
        markChanged();
    }

    public boolean removeMember(UUID player) {
        if(isEssentialMember(player))
            return false;
        boolean removed = this.members.remove(player) != null;
        if(removed) {
            ClanCache.removePlayerClan(player, this);
            Clans.getDynmapCompat().refreshTooltip(this);
            removeLockData(player);
            markChanged();
        }
        return removed;
    }

    public boolean isEssentialMember(UUID player) {
        return members.get(player).equals(EnumRank.LEADER)
            && getLeaders().size() == 1
            && !isServer();
    }

    public boolean demoteMember(UUID player) {
        if (canMemberBeDemoted(player)) {
            EnumRank playerRank = members.get(player);
            members.put(player, EnumRank.getNextLowerRankInClan(playerRank));
            markChanged();
            return true;
        }
        return false;
    }

    public boolean canMemberBeDemoted(UUID player) {
        return members.containsKey(player)
            && EnumRank.isAboveMemberRank(members.get(player))
            && !isEssentialMember(player);
    }

    public boolean promoteMember(UUID player) {
        if (members.containsKey(player)) {
            if(members.get(player) == EnumRank.ADMIN) {
                demotePreviousLeadersIfNeeded();
                members.put(player, EnumRank.LEADER);
                markChanged();
                return true;
            } else if(members.get(player) == EnumRank.MEMBER) {
                members.put(player, EnumRank.ADMIN);
                markChanged();
                return true;
            }
        }
        return false;
    }

    private void demotePreviousLeadersIfNeeded() {
        if(!Clans.getConfig().allowsMultipleClanLeaders()) {
            getLeaders().forEach(leader -> members.put(leader, EnumRank.ADMIN));
        }
    }

    public double getRent() {
        return rent;
    }

    public void setRent(double rent) {
        this.rent = rent;
        markChanged();
    }

    public long getNextRentTimestamp() {
        return rentTimestamp;
    }

    public void updateNextRentTimestamp() {
        this.rentTimestamp = System.currentTimeMillis() + Clans.getConfig().getChargeRentDays() * 1000L * 60L * 60L * 24L;
        markChanged();
    }

    public long getNextUpkeepTimestamp() {
        return upkeepTimestamp;
    }

    public void updateNextUpkeepTimestamp() {
        this.upkeepTimestamp = System.currentTimeMillis() + Clans.getConfig().getClanUpkeepDays() * 1000L * 60L * 60L * 24L;
        markChanged();
    }

    public boolean isUnraidable() {
        return isServer() || Boolean.FALSE.equals(pvpAllowed());
    }

    //region shield
    /**
     * Add minutes to the clan's shield
     * @param shield
     * number of minutes of shield
     */
    public void addShield(long shield) {
        this.shield += shield;
        markChanged();
    }

    public void setShield(long shield) {
        this.shield = shield;
        markChanged();
    }

    /**
     * This should be called once a minute
     */
    public void decrementShield() {
        if(shield > 0)
            shield--;
        markChanged();
    }

    public boolean isShielded() {
        return shield > 0;
    }

    /**
     * Gets the amount of shield remaining on the clan, in minutes.
     */
    public long getShield() {
        return shield;
    }
    //endregion

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    public void addWin(Raid raid) {
        wins++;
        if(Clans.getConfig().isIncreasingRewards()) {
            if(raid.getPartyWlr() >= Clans.getConfig().getWLRThreshold())
                decreaseMultiplier();
        }
        markChanged();
    }

    public void addLoss() {
        losses++;
        if(Clans.getConfig().isIncreasingRewards())
            increaseMultiplier();
        markChanged();
    }

    private void decreaseMultiplier() {
        setMultiplier(Clans.getConfig().getDecreasedMultiplierFormula());
    }

    private void increaseMultiplier() {
        setMultiplier(Clans.getConfig().getIncreasedMultiplierFormula());
    }

    private void setMultiplier(String formula) {
        double eval = FormulaParser.eval(formula, this, 1.0);
        if(eval > 0.99)
            multiplier = eval;
    }

    public double getDisbandCost() {
        return FormulaParser.eval(Clans.getConfig().getDisbandFeeFormula(), this, 1.0);
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
        this.textColor = TextStyles.getNearestTextColor(color).getColorIndex();
        markChanged();
    }

    public double getRaidRewardMultiplier() {
        return multiplier;
    }

    public TextFormatting getTextColor() {
        return Objects.requireNonNull(TextFormatting.fromColorIndex(textColor));
    }

    public void refundClaim() {
        Clans.getPaymentHandler().addAmount(getClaimCost(), getId());
    }

    public boolean payForClaim() {
        return Clans.getPaymentHandler().deductAmount(getClaimCost(), getId());
    }

    public void setPerm(String permission, EnumRank rank) {
        permissions.put(permission, rank);
        markChanged();
    }

    public void addPermissionOverride(String permission, UUID playerId, boolean value) {
        permissionOverrides.get(permission).put(playerId, value);
        markChanged();
    }

    public void addLock(BlockPos pos, EnumLockType type, UUID owner) {
        locks.put(pos, new OrderedPair<>(type, owner));
        markChanged();
    }

    public void delLock(BlockPos pos) {
        if(locks.remove(pos) != null)
            markChanged();
    }

    public void addLockOverride(BlockPos pos, UUID playerId, boolean value) {
        if(!lockOverrides.containsKey(pos))
            lockOverrides.put(pos, Maps.newHashMap());
        lockOverrides.get(pos).put(playerId, value);
        markChanged();
    }

    public boolean hasLockAccess(BlockPos pos, @Nullable UUID playerId, String defaultToPermission) {
        if(playerId == null)
            return false;
        if(hasPerm("lockadmin", playerId))
            return true;
        if(lockOverrides.containsKey(pos) && lockOverrides.get(pos).containsKey(playerId))
            return lockOverrides.get(pos).get(playerId);
        else if(locks.containsKey(pos)) {
            switch(locks.get(pos).getValue1()) {
                case OPEN:
                    return true;
                case CLAN:
                default:
                    return members.containsKey(playerId);
                case PRIVATE:
                    return locks.get(pos).getValue2().equals(playerId);
            }
        } else
            return hasPerm(defaultToPermission, playerId);
    }

    public boolean isLocked(BlockPos pos) {
        return locks.containsKey(pos);
    }

    public boolean isLockOwner(BlockPos pos, UUID checkOwner) {
        return locks.getOrDefault(pos, new OrderedPair<>(null, UUID.fromString("00000000-0000-0000-0000-000000000000"))).getValue2().equals(checkOwner);
    }

    @Nullable
    public UUID getLockOwner(BlockPos pos) {
        return locks.getOrDefault(pos, new OrderedPair<>(null, null)).getValue2();
    }

    @Nullable
    public EnumLockType getLockType(BlockPos pos) {
        return locks.getOrDefault(pos, new OrderedPair<>(null, null)).getValue1();
    }

    public Map<UUID, Boolean> getLockOverrides(BlockPos pos) {
        return Collections.unmodifiableMap(lockOverrides.getOrDefault(pos, Maps.newHashMap()));
    }

    public void removeLockData(UUID player) {
        for(Map.Entry<BlockPos, OrderedPair<EnumLockType, UUID>> entry: Sets.newHashSet(locks.entrySet()))
            if(entry.getValue().getValue2().equals(player))
                locks.remove(entry.getKey());
        for(Map.Entry<BlockPos, Map<UUID, Boolean>> entry: Sets.newHashSet(lockOverrides.entrySet()))
            if(entry.getValue().containsKey(player))
                lockOverrides.get(entry.getKey()).remove(player);
    }

    public boolean hasPerm(String permission, @Nullable UUID playerId) {
        if(playerId == null)
            return false;
        if(permissionOverrides.get(permission).containsKey(playerId))
            return permissionOverrides.get(permission).get(playerId);
        else
            return permissions.get(permission).equals(EnumRank.ANY) || (members.containsKey(playerId) && members.get(playerId).greaterOrEquals(permissions.get(permission)));
    }

    public Map<String, EnumRank> getPermissions() {
        return Collections.unmodifiableMap(permissions);
    }

    public Map<String, Map<UUID, Boolean>> getPermissionOverrides() {
        return Collections.unmodifiableMap(permissionOverrides);
    }

    /**
     * Sets addon data for this clan
     * @param key
     * The key you are giving this data. It should be unique
     * @param value
     * The data itself. This should be a primitive, string, a list or map containg only lists/maps/primitives/strings, or a JsonElement. If not, your data may not save/load properly. All lists will be loaded as ArrayLists. All maps will be loaded as HashMaps.
     */
    public void setCustomData(String key, Object value) {
        if(!value.getClass().isPrimitive() && !value.getClass().isAssignableFrom(BigDecimal.class) && !value.getClass().isAssignableFrom(List.class) && !value.getClass().isAssignableFrom(Map.class) && !value.getClass().isAssignableFrom(JsonElement.class))
            Clans.getMinecraftHelper().getLogger().warn("Custom data may not be properly saved and loaded, as it is not assignable from any supported json deserialization. Key: {}, Value: {}", key, value);
        addonData.put(key, value);
        markChanged();
    }

    @Nullable
    public Object getCustomData(String key) {
        return addonData.get(key);
    }

    //region disband
    /**
     * Disbands a clan and unregisters cache for it where needed.
     * @param server
     * The Minecraft Server instance
     * @param sender
     * The player that initiated this disband, if any. Used to determine which clan member, if any, should be exempt from the disband message
     * @param disbandMessageTranslationKey
     * The translation key of the message to go out to all online clan members when it gets disbanded
     * @param translationArgs
     * The arguments to pass in to the translation
     */
    public void disband(MinecraftServer server, @Nullable ICommandSender sender, String disbandMessageTranslationKey, Object... translationArgs) {
        if(RaidingParties.hasActiveRaid(this))
            RaidingParties.getActiveRaid(this).raiderVictory();
        if(RaidingParties.isPreparingRaid(this))
            RaidingParties.removeRaid(RaidingParties.getInactiveRaid(this));
        Clans.getDynmapCompat().clearAllClanMarkers(this);
        ClanDatabase.removeClan(getId());
        if(isServer())
            return;

        double distFunds = Clans.getPaymentHandler().getBalance(this.getId());
        distFunds += FormulaParser.eval(Clans.getConfig().getClaimChunkCostFormula(), this, 0) * this.getClaimCount();
        if (Clans.getConfig().isLeaderRecieveDisbandFunds())
            distFunds = this.payLeaders(distFunds);
        distFunds /= this.getMemberCount();
        for (UUID member : this.getMembers().keySet()) {
            Clans.getPaymentHandler().addAmount(distFunds, member);
            PlayerData.updateDefaultClan(member, getId());
            EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(member);
            //noinspection ConstantConditions
            if (player != null) {
                if (!(sender instanceof EntityPlayerMP) || !player.getUniqueID().equals(((EntityPlayerMP)sender).getUniqueID()))
                    player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), disbandMessageTranslationKey, translationArgs).setStyle(TextStyles.YELLOW));
            }
        }
        Clans.getPaymentHandler().deductAmount(Clans.getPaymentHandler().getBalance(this.getId()), this.getId());
    }
    //endregion

    //region messageAllOnline
    public void messageAllOnline(Style textStyle, String translationKey, Object... args) {
        messageAllOnline(false, textStyle, translationKey, args);
    }

    public void messageAllOnline(@Nullable EntityPlayerMP excluded, Style textStyle, String translationKey, Object... args) {
        messageAllOnline(false, excluded, textStyle, translationKey, args);
    }

    public void messageAllOnline(EnumRank minRank, Style textStyle, String translationKey, Object... args) {
        messageAllOnline(false, minRank, textStyle, translationKey, args);
    }

    public void messageAllOnline(boolean actionBar, Style textStyle, String translationKey, Object... args) {
        messageAllOnline(actionBar, EnumRank.ANY, textStyle, translationKey, args);
    }

    public void messageAllOnline(boolean actionBar, @Nullable EntityPlayerMP excluded, Style textStyle, String translationKey, Object... args) {
        messageAllOnline(actionBar, EnumRank.ANY, excluded, textStyle, translationKey, args);
    }

    public void messageAllOnline(boolean actionBar, EnumRank minRank, Style textStyle, String translationKey, Object... args) {
        messageAllOnline(actionBar, minRank, null, textStyle, translationKey, args);
    }

    public void messageAllOnline(EnumRank minRank, @Nullable EntityPlayerMP excluded, Style textStyle, String translationKey, Object... args) {
        messageAllOnline(false, minRank, excluded, textStyle, translationKey, args);
    }

    public void messageAllOnline(boolean actionBar, EnumRank minRank, @Nullable EntityPlayerMP excluded, Style textStyle, String translationKey, Object... args) {
        Map<EntityPlayerMP, EnumRank> online = getOnlineMembers();
        for(EntityPlayerMP member : online.keySet())
            if(online.get(member).greaterOrEquals(minRank) && (excluded == null || !member.getUniqueID().equals(excluded.getUniqueID())))
                member.sendStatusMessage(TranslationUtil.getTranslation(member.getUniqueID(), translationKey, args).setStyle(textStyle), actionBar && member.isEntityAlive());
    }
    //endregion

    //region Equality
    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null)
            return false;
        else if (obj == this)
            return true;
        else
            return obj instanceof Clan && equals((Clan) obj);
    }

    public boolean equals(@Nullable Clan clan) {
        if (clan == null)
            return false;
        return clanId.equals(clan.clanId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clanId);
    }
    //endregion
}
