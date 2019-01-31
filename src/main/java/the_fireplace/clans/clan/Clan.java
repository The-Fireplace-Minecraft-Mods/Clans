package the_fireplace.clans.clan;

import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;

public class Clan implements Serializable {
	private String clanName, clanBanner;
	private HashMap<UUID, EnumRank> members;
	private UUID clanId;

	public Clan(String clanName, UUID leader){
		this(clanName, leader, null);
	}

	public Clan(String clanName, UUID leader, @Nullable String banner){
		this.clanName = clanName;
		this.members = Maps.newHashMap();
		this.members.put(leader, EnumRank.LEADER);
		if(banner != null)
			this.clanBanner = banner;
		this.clanId = UUID.randomUUID();
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
		this.clanName = clanName;
	}

	public String getClanBanner() {
		return clanBanner;
	}

	public void setClanBanner(String clanBanner) {
		this.clanBanner = clanBanner;
	}
}
