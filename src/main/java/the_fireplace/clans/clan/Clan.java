package the_fireplace.clans.clan;

import com.google.common.collect.Maps;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;

public class Clan implements Serializable {
	private static final long serialVersionUID = 0x2ACA77AC;

	private String clanName, clanBanner;
	private String tagline = "This is a new clan.";
	private HashMap<UUID, EnumRank> members;
	private UUID clanId;
	private float homeX, homeY, homeZ;
	private boolean hasHome = false;
	private int homeDimension;
	private int claimCount = 0;

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
		ClanCache.purgePlayerCache(leader);
	}

	public HashMap<UUID, EnumRank> getMembers() {
		return members;
	}

	public UUID getClanId() {
		return clanId;
	}

	public String getClanName() {
		return clanName;
	}

	public void setClanName(String clanName) {
		ClanCache.removeName(this.clanName);
		ClanCache.addName(clanName);
		this.clanName = clanName;
		ClanDatabase.save();
	}

	public String getClanBanner() {
		return clanBanner;
	}

	public void setClanBanner(String clanBanner) {
		ClanCache.removeBanner(this.clanBanner);
		ClanCache.addBanner(clanBanner);
		this.clanBanner = clanBanner;
		ClanDatabase.save();
	}

	public void setHome(BlockPos pos, int dimension) {
		this.homeX = pos.getX();
		this.homeY = pos.getY();
		this.homeZ = pos.getZ();
		this.hasHome = true;
		this.homeDimension = dimension;
		ClanDatabase.save();
	}

	public boolean hasHome() {
		return hasHome;
	}

	public void unsetHome() {
		hasHome = false;
		homeX = homeY = homeZ = 0;
		homeDimension = 0;
	}

	public BlockPos getHome() {
		return new BlockPos(homeX, homeY, homeZ);
	}

	public int getHomeDim() {
		return homeDimension;
	}

	public int getClaimCount() {
		return claimCount;
	}

	public void addClaimCount() {
		claimCount++;
	}

	public void subClaimCount() {
		claimCount--;
	}

	public String getTagline() {
		return tagline;
	}

	public void setTagline(String tagline) {
		this.tagline = tagline;
	}

	public int getMemberCount(){
		return members.size();
	}

	public void addMember(UUID player) {
		this.members.put(player, EnumRank.MEMBER);
		ClanCache.purgePlayerCache(player);
		ClanDatabase.save();
	}

	public boolean removeMember(UUID player) {
		boolean removed = this.members.remove(player) != null;
		if(removed) {
			ClanCache.purgePlayerCache(player);
			ClanDatabase.save();
		}
		return removed;
	}

	public boolean demoteMember(UUID player) {
		if(!members.containsKey(player))
			return false;
		else {
			if(members.get(player) == EnumRank.ADMIN){
				members.put(player, EnumRank.MEMBER);
				ClanCache.updateRank(player, EnumRank.MEMBER);
				ClanDatabase.save();
				return true;
			} else return false;
		}
	}

	public boolean promoteMember(UUID player) {
		if(!members.containsKey(player))
			return false;
		else {
			if(members.get(player) == EnumRank.ADMIN) {
				//TODO: Perhaps a config option to restrict clans to one leader, disabled by default
				/*UUID leader = null;
				for(UUID member: members.keySet())
					if(members.get(member) == EnumRank.LEADER) {
						leader = member;
						break;
					}
				if(leader != null) {
					members.put(leader, EnumRank.ADMIN);
					ClanCache.updateRank(leader, EnumRank.ADMIN);
				}*/
				members.put(player, EnumRank.LEADER);
				ClanCache.updateRank(player, EnumRank.LEADER);
				ClanDatabase.save();
				return true;
			} else if(members.get(player) == EnumRank.MEMBER) {
				members.put(player, EnumRank.ADMIN);
				ClanCache.updateRank(player, EnumRank.ADMIN);
				ClanDatabase.save();
				return true;
			} return false;
		}
	}
}
