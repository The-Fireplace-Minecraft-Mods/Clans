package the_fireplace.clans.clan;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import the_fireplace.clans.Clans;
import the_fireplace.clans.util.TextStyles;

import javax.annotation.Nullable;
import java.util.*;

public class NewClan {
    private String clanName, clanBanner;
    private String description = "This is a new clan.";
    private HashMap<UUID, EnumRank> members;
    private UUID clanId;
    private float homeX, homeY, homeZ;
    private boolean hasHome = false;
    private int homeDimension;
    private int claimCount = 0;
    private boolean isOpclan = false;
    private long rent = 0;
    private int wins = 0;
    private int losses = 0;
    private long shield = Clans.cfg.initialShield * 60;
    private long rentTimestamp = System.currentTimeMillis(), upkeepTimestamp = System.currentTimeMillis();
    private int color = new Random().nextInt(0xffffff);
    private int textColor = TextStyles.getNearestTextColor(color).getColorIndex();

    public NewClan(String clanName, UUID leader){
        this(clanName, leader, null);
    }

    public NewClan(String clanName, UUID leader, @Nullable String banner){
        this.clanName = clanName;
        this.members = Maps.newHashMap();
        this.members.put(leader, EnumRank.LEADER);
        if(banner != null)
            this.clanBanner = banner;
        do{
            this.clanId = UUID.randomUUID();
        } while(!NewClanDatabase.addClan(this.clanId, this));
        Clans.getPaymentHandler().ensureAccountExists(clanId);

        // Ensure that the starting balance of the account is 0,
        //  to prevent "free money" from the creation of a new bank account
        if (Clans.getPaymentHandler().getBalance(clanId) > 0) {
            Clans.getPaymentHandler().deductAmount(Clans.getPaymentHandler().getBalance(clanId),clanId);
        }

        Clans.getPaymentHandler().addAmount(Clans.cfg.formClanBankAmount, clanId);
        ClanCache.purgePlayerCache(leader);
    }

    /**
     * Generate OpClan
     */
    NewClan(){
        this.clanName = "Server";
        this.description = "Server Operator Clan";
        this.members = Maps.newHashMap();
        this.clanId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        if(!NewClanDatabase.addClan(this.clanId, this))
            Clans.LOGGER.error("Unable to add opclan to the clan database!");
        this.isOpclan = true;
    }

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
        ret.addProperty("claimCount", claimCount);
        ret.addProperty("isOpclan", isOpclan);
        ret.addProperty("rent", rent);
        ret.addProperty("wins", wins);
        ret.addProperty("losses", losses);
        ret.addProperty("shield", shield);
        ret.addProperty("rentTimestamp", rentTimestamp);
        ret.addProperty("upkeepTimestamp", upkeepTimestamp);
        ret.addProperty("color", color);

        return ret;
    }

    public NewClan(JsonObject obj){
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
        this.claimCount = obj.get("claimCount").getAsInt();
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
    }

    public HashMap<UUID, EnumRank> getMembers() {
        return members;
    }

    public ArrayList<UUID> getLeaders() {
        ArrayList<UUID> leaders = Lists.newArrayList();
        for(Map.Entry<UUID, EnumRank> member: members.entrySet())
            if(member.getValue().equals(EnumRank.LEADER))
                leaders.add(member.getKey());
        return leaders;
    }

    public void payLeaders(long totalAmount) {
        ArrayList<UUID> leaders = getLeaders();
        long remainder = totalAmount % leaders.size();
        totalAmount /= leaders.size();
        for(UUID leader: leaders) {
            Clans.getPaymentHandler().addAmount(totalAmount, leader);
            if(remainder-- > 0)
                Clans.getPaymentHandler().addAmount(1, leader);
        }
    }

    public HashMap<EntityPlayerMP, EnumRank> getOnlineMembers() {
        HashMap<EntityPlayerMP, EnumRank> online = Maps.newHashMap();
        if(isOpclan)
            return online;
        for(Map.Entry<UUID, EnumRank> member: getMembers().entrySet())
            online.put(ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByUUID(member.getKey()), member.getValue());
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
        NewClanDatabase.markChanged();
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
        NewClanDatabase.markChanged();
    }

    public void setHome(BlockPos pos, int dimension) {
        if(isOpclan)
            return;
        this.homeX = pos.getX();
        this.homeY = pos.getY();
        this.homeZ = pos.getZ();
        this.hasHome = true;
        this.homeDimension = dimension;
        NewClanDatabase.markChanged();
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
        NewClanDatabase.markChanged();
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
        return claimCount;
    }

    public int getMaxClaimCount() {
        return getMemberCount() * Clans.cfg.maxClanPlayerClaims;
    }

    public void addClaimCount() {
        claimCount++;
        NewClanDatabase.markChanged();
    }

    public void subClaimCount() {
        claimCount--;
        NewClanDatabase.markChanged();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        NewClanDatabase.markChanged();
    }

    public int getMemberCount(){
        return members.size();
    }

    public void addMember(UUID player) {
        if(isOpclan)
            return;
        this.members.put(player, EnumRank.MEMBER);
        ClanCache.purgePlayerCache(player);
        NewClanDatabase.markChanged();
    }

    public boolean removeMember(UUID player) {
        if(isOpclan)
            return false;
        if(members.get(player).equals(EnumRank.LEADER) && getLeaders().size() == 1)
            return false;
        boolean removed = this.members.remove(player) != null;
        if(removed) {
            ClanCache.purgePlayerCache(player);
            NewClanDatabase.markChanged();
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
                NewClanDatabase.markChanged();
                return true;
            } else if(members.get(player) == EnumRank.LEADER){
                members.put(player, EnumRank.ADMIN);
                NewClanDatabase.markChanged();
                return true;
            } else return false;
        }
    }

    public boolean promoteMember(UUID player) {
        if(isOpclan || !members.containsKey(player))
            return false;
        else {
            if(members.get(player) == EnumRank.ADMIN) {
                if(!Clans.cfg.multipleClanLeaders) {
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
                NewClanDatabase.markChanged();
                return true;
            } else if(members.get(player) == EnumRank.MEMBER) {
                members.put(player, EnumRank.ADMIN);
                NewClanDatabase.markChanged();
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
    }

    public long getNextRentTimestamp() {
        return rentTimestamp;
    }

    public void updateNextRentTimeStamp() {
        this.rentTimestamp = System.currentTimeMillis() + Clans.cfg.chargeRentDays * 1000L * 60L * 60L * 24L;
    }

    public long getNextUpkeepTimestamp() {
        return upkeepTimestamp;
    }

    public void updateNextUpkeepTimeStamp() {
        this.upkeepTimestamp = System.currentTimeMillis() + Clans.cfg.clanUpkeepDays * 1000L * 60L * 60L * 24L;
    }

    /**
     * Add minutes to the clan's shield
     * @param shield
     * number of minutes of shield
     */
    public void addShield(long shield) {
        this.shield += shield;
    }

    public void setShield(long shield) {
        this.shield = shield;
    }

    /**
     * This should be called once a minute
     */
    public void decrementShield() {
        if(shield > 0)
            shield--;
    }

    public boolean isShielded() {
        return shield > 0;
    }

    public long getShield() {
        return shield;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    public void addWin() {
        wins++;
    }

    public void addLoss() {
        losses++;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
        this.textColor = TextStyles.getNearestTextColor(color).getColorIndex();
    }

    public TextFormatting getTextColor() {
        return TextFormatting.fromColorIndex(textColor);
    }
}