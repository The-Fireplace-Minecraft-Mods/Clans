package the_fireplace.clans.clan;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;
import the_fireplace.clans.Clans;
import the_fireplace.clans.raid.RaidingParties;
import the_fireplace.clans.util.PlayerClanCapability;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import java.util.*;

public class Clan {
    private String clanName, clanBanner;
    private String description = TranslationUtil.getStringTranslation("clan.default_description");
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
    private long rentTimestamp = System.currentTimeMillis() + Clans.cfg.chargeRentDays * 1000L * 60L * 60L * 24L, upkeepTimestamp = System.currentTimeMillis() + Clans.cfg.clanUpkeepDays * 1000L * 60L * 60L * 24L;
    private int color = new Random().nextInt(0xffffff);
    private int textColor = TextStyles.getNearestTextColor(color).getColorIndex();

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
        Clans.getPaymentHandler().ensureAccountExists(clanId);
    
        // Ensure that the starting balance of the account is 0, to prevent "free money" from the creation of a new bank account
        if (Clans.getPaymentHandler().getBalance(clanId) > 0)
            Clans.getPaymentHandler().deductAmount(Clans.getPaymentHandler().getBalance(clanId),clanId);
        
        Clans.getPaymentHandler().addAmount(Clans.cfg.formClanBankAmount, clanId);
        ClanCache.addPlayerClan(leader, this);
        if(!Clans.cfg.allowMultiClanMembership)
            ClanCache.removeInvite(leader);
    }

    /**
     * For internal use only. Generates OpClan.
     */
    Clan(){
        this.clanName = TranslationUtil.getStringTranslation("clan.default_opclan_name");
        this.description = TranslationUtil.getStringTranslation("clan.default_opclan_description");
        this.members = Maps.newHashMap();
        this.clanId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        if(!ClanDatabase.addClan(this.clanId, this))
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

    public Clan(JsonObject obj){
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
            EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(member.getKey());
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
        ClanDatabase.markChanged();
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
        ClanDatabase.markChanged();
    }

    public void setHome(BlockPos pos, int dimension) {
        if(isOpclan)
            return;
        this.homeX = pos.getX();
        this.homeY = pos.getY();
        this.homeZ = pos.getZ();
        this.hasHome = true;
        this.homeDimension = dimension;
        ClanDatabase.markChanged();
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
        ClanDatabase.markChanged();
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
        ClanDatabase.markChanged();
    }

    public void subClaimCount() {
        claimCount--;
        ClanDatabase.markChanged();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        ClanDatabase.markChanged();
    }

    public int getMemberCount(){
        return members.size();
    }

    public void addMember(UUID player) {
        if(isOpclan)
            return;
        this.members.put(player, EnumRank.MEMBER);
        ClanCache.addPlayerClan(player, this);
        if(!Clans.cfg.allowMultiClanMembership || equals(ClanCache.getInvite(player)))
            ClanCache.removeInvite(player);
        ClanDatabase.markChanged();
    }

    public boolean removeMember(UUID player) {
        if(isOpclan)
            return false;
        if(members.get(player).equals(EnumRank.LEADER) && getLeaders().size() == 1)
            return false;
        boolean removed = this.members.remove(player) != null;
        if(removed) {
            ClanCache.removePlayerClan(player, this);
            ClanDatabase.markChanged();
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
                ClanDatabase.markChanged();
                return true;
            } else if(members.get(player) == EnumRank.LEADER){
                members.put(player, EnumRank.ADMIN);
                ClanDatabase.markChanged();
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
                ClanDatabase.markChanged();
                return true;
            } else if(members.get(player) == EnumRank.MEMBER) {
                members.put(player, EnumRank.ADMIN);
                ClanDatabase.markChanged();
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

    /**
     * Gets the amount of shield remaining on the clan, in minutes.
     */
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
        distFunds += Clans.cfg.claimChunkCost * this.getClaimCount();
        if (Clans.cfg.leaderRecieveDisbandFunds) {
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
            EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(member);
            //noinspection ConstantConditions
            if (player != null) {
                PlayerClanCapability.updateDefaultClan(player, this);
                if (!(sender instanceof EntityPlayerMP) || !player.getUniqueID().equals(((EntityPlayerMP)sender).getUniqueID()))
                    player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), disbandMessageTranslationKey, translationArgs).setStyle(TextStyles.YELLOW));
            }
        }
        Clans.getPaymentHandler().deductAmount(Clans.getPaymentHandler().getBalance(this.getClanId()), this.getClanId());
    }

    /**
     * Compares the object to this object
     * @param obj The object to compare this object too
     * @return Returns true if the object is the same type and the contents match.
     */
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
        return getClanId().equals(clan.getClanId());
    }
}
