package the_fireplace.clans.clan;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.command.CommandException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import the_fireplace.clans.Clans;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Clan implements Serializable {
	private static final long serialVersionUID = 0x1254367;

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

	private long rentTimeStamp = System.currentTimeMillis(), upkeepTimeStamp = System.currentTimeMillis();

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
		Clans.getPaymentHandler().addAmount(Clans.cfg.formClanBankAmount, clanId);
		ClanCache.purgePlayerCache(leader);
	}

	/**
	 * Generate OpClan
	 */
	Clan(){
		this.clanName = "Server";
		this.description = "Server Operator Clan";
		this.members = Maps.newHashMap();
		this.clanId = UUID.fromString("00000000-0000-0000-0000-000000000000");
		while(!ClanDatabase.addClan(this.clanId, this))
			this.clanId = UUID.randomUUID();
		this.isOpclan = true;
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
		for(Map.Entry<UUID, EnumRank> member: getMembers().entrySet()) {
			EntityPlayerMP memberMP;
			try {
				memberMP = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByUUID(member.getKey());
			} catch(CommandException e) {
				continue;
			}
			online.put(memberMP, member.getValue());
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
		ClanDatabase.save();
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
		ClanDatabase.save();
	}

	public void setHome(BlockPos pos, int dimension) {
		if(isOpclan)
			return;
		this.homeX = pos.getX();
		this.homeY = pos.getY();
		this.homeZ = pos.getZ();
		this.hasHome = true;
		this.homeDimension = dimension;
		ClanDatabase.save();
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
		//No need to save here because subClaimCount is always called after this.
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
		ClanDatabase.save();
	}

	public void subClaimCount() {
		claimCount--;
		ClanDatabase.save();
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
		ClanDatabase.save();
	}

	public int getMemberCount(){
		return members.size();
	}

	public void addMember(UUID player) {
		if(isOpclan)
			return;
		this.members.put(player, EnumRank.MEMBER);
		ClanCache.purgePlayerCache(player);
		ClanDatabase.save();
	}

	public boolean removeMember(UUID player) {
		if(isOpclan)
			return false;
		if(members.get(player).equals(EnumRank.LEADER) && getLeaders().size() == 1)
			return false;
		boolean removed = this.members.remove(player) != null;
		if(removed) {
			ClanCache.purgePlayerCache(player);
			ClanDatabase.save();
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
				ClanDatabase.save();
				return true;
			} else if(members.get(player) == EnumRank.LEADER){
				members.put(player, EnumRank.ADMIN);
				ClanDatabase.save();
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
				ClanDatabase.save();
				return true;
			} else if(members.get(player) == EnumRank.MEMBER) {
				members.put(player, EnumRank.ADMIN);
				ClanDatabase.save();
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

	public long getRentTimeStamp() {
		return rentTimeStamp;
	}

	public void updateRentTimeStamp() {
		this.rentTimeStamp = System.currentTimeMillis();
	}

	public long getUpkeepTimeStamp() {
		return upkeepTimeStamp;
	}

	public void updateUpkeepTimeStamp() {
		this.upkeepTimeStamp = System.currentTimeMillis();
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
}
