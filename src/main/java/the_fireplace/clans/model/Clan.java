package the_fireplace.clans.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import the_fireplace.clans.data.ClaimDataManager;
import the_fireplace.clans.data.ClanDatabase;
import the_fireplace.clans.data.PlayerDataManager;
import the_fireplace.clans.util.ClansEventManager;
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

public class Clan {
    private boolean isChanged = false;
    private File clanDataFile;

    private String clanName, clanBanner;
    private String description = TranslationUtil.getStringTranslation("clan.default_description");
    private HashMap<UUID, EnumRank> members;
    private UUID clanId;
    private float homeX, homeY, homeZ;
    private boolean hasHome = false;
    private int homeDimension;
    private boolean isOpclan = false;
    private long rent = 0;
    private int wins = 0;
    private int losses = 0;
    private long shield = Clans.getConfig().getInitialShield() * 60;
    private long rentTimestamp = System.currentTimeMillis() + Clans.getConfig().getChargeRentDays() * 1000L * 60L * 60L * 24L, upkeepTimestamp = System.currentTimeMillis() + Clans.getConfig().getClanUpkeepDays() * 1000L * 60L * 60L * 24L;
    private int color = new Random().nextInt(0xffffff);
    private int textColor = TextStyles.getNearestTextColor(color).getColorIndex();

    private HashMap<String, Object> addonData = Maps.newHashMap();

    public Clan(String clanName, UUID leader){
        this(clanName, leader, null);
    }

    public Clan(String clanName, UUID leader, @Nullable String banner){
        this.clanName = clanName;
        this.members = Maps.newHashMap();
        this.members.put(leader, EnumRank.LEADER);
        if(banner != null)
            this.clanBanner = banner;
        do{
            this.clanId = UUID.randomUUID();
        } while(!ClanDatabase.addClan(this.clanId, this));
        clanDataFile = new File(ClanDatabase.clanDataLocation, clanId.toString()+".json");
        Clans.getPaymentHandler().ensureAccountExists(clanId);
    
        // Ensure that the starting balance of the account is 0, to prevent "free money" from the creation of a new bank account
        if (Clans.getPaymentHandler().getBalance(clanId) > 0)
            Clans.getPaymentHandler().deductAmount(Clans.getPaymentHandler().getBalance(clanId),clanId);
        
        Clans.getPaymentHandler().addAmount(Clans.getConfig().getFormClanBankAmount(), clanId);
        ClanCache.addPlayerClan(leader, this);
        if(!Clans.getConfig().isAllowMultiClanMembership())
            ClanCache.removeInvite(leader);
        ClansEventManager.fireEvent(new ClanFormedEvent(leader, this));
        isChanged = true;
    }

    /**
     * For internal use only. Generates OpClan.
     */
    public Clan(){
        this.clanName = TranslationUtil.getStringTranslation("clan.default_opclan_name");
        this.description = TranslationUtil.getStringTranslation("clan.default_opclan_description");
        this.members = Maps.newHashMap();
        this.clanId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        clanDataFile = new File(ClanDatabase.clanDataLocation, clanId.toString()+".json");
        if(!ClanDatabase.addClan(this.clanId, this))
            Clans.getMinecraftHelper().getLogger().error("Unable to add opclan to the clan database!");
        this.isOpclan = true;
        ClansEventManager.fireEvent(new ClanFormedEvent(null, this));
        isChanged = true;
    }

    //region JsonObject conversions
    public JsonObject toJsonObject() {
        JsonObject ret = new JsonObject();
        ret.addProperty("clanName", clanName);
        ret.addProperty("clanBanner", clanBanner);
        ret.addProperty("clanDescription", description);
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
        ret.addProperty("isOpclan", isOpclan);
        ret.addProperty("rent", rent);
        ret.addProperty("wins", wins);
        ret.addProperty("losses", losses);
        ret.addProperty("shield", shield);
        ret.addProperty("rentTimestamp", rentTimestamp);
        ret.addProperty("upkeepTimestamp", upkeepTimestamp);
        ret.addProperty("color", color);

        JsonHelper.attachAddonData(ret, this.addonData);

        return ret;
    }

    public Clan(JsonObject obj) {
        this.clanName = obj.get("clanName").getAsString();
        if(obj.has("clanBanner") && !(obj.get("clanBanner") instanceof JsonNull))
            this.clanBanner = obj.get("clanBanner").getAsString();
        this.description = obj.get("clanDescription").getAsString();
        HashMap<UUID, EnumRank> newMembers = Maps.newHashMap();
        for(JsonElement entry: obj.get("members").getAsJsonArray())
            newMembers.put(UUID.fromString(entry.getAsJsonObject().get("key").getAsString()), EnumRank.valueOf(entry.getAsJsonObject().get("value").getAsString()));
        this.members = newMembers;
        this.clanId = UUID.fromString(obj.get("clanId").getAsString());
        this.isOpclan = obj.get("isOpclan").getAsBoolean();
        this.homeX = obj.get("homeX").getAsFloat();
        this.homeY = obj.get("homeY").getAsFloat();
        this.homeZ = obj.get("homeZ").getAsFloat();
        this.hasHome = obj.get("hasHome").getAsBoolean();
        this.homeDimension = obj.get("homeDimension").getAsInt();
        this.rent = obj.get("rent").getAsLong();
        this.wins = obj.get("wins").getAsInt();
        this.losses = obj.get("losses").getAsInt();
        this.shield = obj.get("shield").getAsLong();
        this.rentTimestamp = obj.get("rentTimestamp").getAsLong();
        this.upkeepTimestamp = obj.get("upkeepTimestamp").getAsLong();
        if(obj.has("color")) {
            this.color = obj.get("color").getAsInt();
            this.textColor = TextStyles.getNearestTextColor(color).getColorIndex();
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

    public HashMap<UUID, EnumRank> getMembers() {
        return Maps.newHashMap(members);
    }

    public ArrayList<UUID> getLeaders() {
        ArrayList<UUID> leaders = Lists.newArrayList();
        for(Map.Entry<UUID, EnumRank> member: members.entrySet())
            if(member.getValue().equals(EnumRank.LEADER))
                leaders.add(member.getKey());
        return leaders;
    }

    public long payLeaders(long totalAmount) {
        ArrayList<UUID> leaders = getLeaders();
        if(leaders.isEmpty())
            return totalAmount;
        long remainder = totalAmount % leaders.size();
        totalAmount /= leaders.size();
        for(UUID leader: leaders) {
            Clans.getPaymentHandler().addAmount(totalAmount, leader);
            if(remainder-- > 0)
                Clans.getPaymentHandler().addAmount(1, leader);
        }
        return 0;
    }

    public HashMap<EntityPlayerMP, EnumRank> getOnlineMembers() {
        HashMap<EntityPlayerMP, EnumRank> online = Maps.newHashMap();
        if(isOpclan)
            return online;
        for(Map.Entry<UUID, EnumRank> member: getMembers().entrySet()) {
            EntityPlayerMP player = Clans.getMinecraftHelper().getServer().getPlayerList().getPlayerByUUID(member.getKey());
            //noinspection ConstantConditions
            if(player != null)
                online.put(player, member.getValue());
        }
        return online;
    }

    public UUID getClanId() {
        return clanId;
    }

    public String getClanName() {
        return clanName;
    }

    public void setClanName(String clanName) {
        ClanCache.removeName(this.clanName);
        this.clanName = clanName;
        ClanCache.addName(this);
        markChanged();
    }

    public String getClanBanner() {
        return clanBanner;
    }

    public void setClanBanner(String clanBanner) {
        if(isOpclan)
            return;
        ClanCache.removeBanner(this.clanBanner);
        ClanCache.addBanner(clanBanner);
        this.clanBanner = clanBanner;
        markChanged();
    }

    public void setHome(BlockPos pos, int dimension) {
        if(isOpclan)
            return;
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

    public int getClaimCount() {
        return ClaimDataManager.getClaimedChunks(getClanId()).size();
    }

    public int getMaxClaimCount() {
        return Clans.getConfig().isMultiplyMaxClaimsByPlayers() ? getMemberCount() * Clans.getConfig().getMaxClaims() : Clans.getConfig().getMaxClaims();
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
        if(isOpclan)
            return;
        this.members.put(player, EnumRank.MEMBER);
        ClanCache.addPlayerClan(player, this);
        if(!Clans.getConfig().isAllowMultiClanMembership() || equals(ClanCache.getInvite(player)))
            ClanCache.removeInvite(player);
        markChanged();
    }

    public boolean removeMember(UUID player) {
        if(isOpclan)
            return false;
        if(members.get(player).equals(EnumRank.LEADER) && getLeaders().size() == 1)
            return false;
        boolean removed = this.members.remove(player) != null;
        if(removed) {
            ClanCache.removePlayerClan(player, this);
            markChanged();
        }
        return removed;
    }

    public boolean demoteMember(UUID player) {
        if(isOpclan || !members.containsKey(player))
            return false;
        else {
            if(members.get(player).equals(EnumRank.LEADER) && getLeaders().size() == 1)
                return false;
            if(members.get(player) == EnumRank.ADMIN){
                members.put(player, EnumRank.MEMBER);
                markChanged();
                return true;
            } else if(members.get(player) == EnumRank.LEADER){
                members.put(player, EnumRank.ADMIN);
                markChanged();
                return true;
            } else
                return false;
        }
    }

    public boolean promoteMember(UUID player) {
        if(isOpclan || !members.containsKey(player))
            return false;
        else {
            if(members.get(player) == EnumRank.ADMIN) {
                if(!Clans.getConfig().isMultipleClanLeaders()) {
                    UUID leader = null;
                    for(UUID member: members.keySet())
                        if(members.get(member) == EnumRank.LEADER) {
                            leader = member;
                            break;
                        }
                    if(leader != null) {
                        members.put(leader, EnumRank.ADMIN);
                    }
                }
                members.put(player, EnumRank.LEADER);
                markChanged();
                return true;
            } else if(members.get(player) == EnumRank.MEMBER) {
                members.put(player, EnumRank.ADMIN);
                markChanged();
                return true;
            } return false;
        }
    }

    public boolean isOpclan(){
        return isOpclan;
    }

    public long getRent() {
        return rent;
    }

    public void setRent(long rent) {
        this.rent = rent;
        markChanged();
    }

    public long getNextRentTimestamp() {
        return rentTimestamp;
    }

    public void updateNextRentTimeStamp() {
        this.rentTimestamp = System.currentTimeMillis() + Clans.getConfig().getChargeRentDays() * 1000L * 60L * 60L * 24L;
        markChanged();
    }

    public long getNextUpkeepTimestamp() {
        return upkeepTimestamp;
    }

    public void updateNextUpkeepTimeStamp() {
        this.upkeepTimestamp = System.currentTimeMillis() + Clans.getConfig().getClanUpkeepDays() * 1000L * 60L * 60L * 24L;
        markChanged();
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

    public void addWin() {
        wins++;
        markChanged();
    }

    public void addLoss() {
        losses++;
        markChanged();
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
        this.textColor = TextStyles.getNearestTextColor(color).getColorIndex();
        markChanged();
    }

    public TextFormatting getTextColor() {
        return TextFormatting.fromColorIndex(textColor);
    }

    public void refundClaim() {
        Clans.getPaymentHandler().addAmount(getClaimCount() <= Clans.getConfig().getReducedCostClaimCount() ? Clans.getConfig().getReducedChunkClaimCost() : Clans.getConfig().getClaimChunkCost(), getClanId());
    }

    public boolean payForClaim() {
        return Clans.getPaymentHandler().deductAmount(getClaimCount() < Clans.getConfig().getReducedCostClaimCount() ? Clans.getConfig().getReducedChunkClaimCost() : Clans.getConfig().getClaimChunkCost(), getClanId());
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
            RaidingParties.removeRaid(RaidingParties.getRaid(this));
        ClanDatabase.removeClan(getClanId());

        long distFunds = Clans.getPaymentHandler().getBalance(this.getClanId());
        long rem;
        distFunds += Clans.getConfig().getClaimChunkCost() * this.getClaimCount();
        if (Clans.getConfig().isLeaderRecieveDisbandFunds()) {
            distFunds = this.payLeaders(distFunds);
            rem = distFunds % this.getMemberCount();
            distFunds /= this.getMemberCount();
        } else {
            rem = this.payLeaders(distFunds % this.getMemberCount());
            distFunds /= this.getMemberCount();
        }
        for (UUID member : this.getMembers().keySet()) {
            Clans.getPaymentHandler().ensureAccountExists(member);
            if (!Clans.getPaymentHandler().addAmount(distFunds + (rem-- > 0 ? 1 : 0), member))
                rem += this.payLeaders(distFunds);
            PlayerDataManager.updateDefaultClan(member, getClanId());
            EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(member);
            //noinspection ConstantConditions
            if (player != null) {
                if (!(sender instanceof EntityPlayerMP) || !player.getUniqueID().equals(((EntityPlayerMP)sender).getUniqueID()))
                    player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), disbandMessageTranslationKey, translationArgs).setStyle(TextStyles.YELLOW));
            }
        }
        Clans.getPaymentHandler().deductAmount(Clans.getPaymentHandler().getBalance(this.getClanId()), this.getClanId());
    }
    //endregion

    //region messageAllOnline
    public void messageAllOnline(Style textStyle, String translationKey, Object... args) {
        messageAllOnline(EnumRank.ANY, textStyle, translationKey, args);
    }

    public void messageAllOnline(EntityPlayerMP excluded, Style textStyle, String translationKey, Object... args) {
        messageAllOnline(EnumRank.ANY, excluded, textStyle, translationKey, args);
    }

    public void messageAllOnline(EnumRank minRank, Style textStyle, String translationKey, Object... args) {
        messageAllOnline(minRank, null, textStyle, translationKey, args);
    }

    public void messageAllOnline(EnumRank minRank, @Nullable EntityPlayerMP excluded, Style textStyle, String translationKey, Object... args) {
        HashMap<EntityPlayerMP, EnumRank> online = getOnlineMembers();
        for(EntityPlayerMP member : online.keySet())
            if(online.get(member).greaterOrEquals(minRank) && (excluded == null || !member.getUniqueID().equals(excluded.getUniqueID())))
                member.sendMessage(TranslationUtil.getTranslation(member.getUniqueID(), translationKey, args).setStyle(textStyle));
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
