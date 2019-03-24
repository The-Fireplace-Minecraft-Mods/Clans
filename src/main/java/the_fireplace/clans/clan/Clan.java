package the_fireplace.clans.clan;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import the_fireplace.clans.Clans;

import javax.annotation.Nullable;
import java.io.Serializable;
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
		this.clanId = UUID.randomUUID();
		Clans.getPaymentHandler().ensureAccountExists(clanId);
		Clans.getPaymentHandler().addAmount(Clans.cfg.formClanBankAmount, clanId);
		ClanCache.purgePlayerCache(leader);
	}

	public UUID getClanId() {
		return clanId;
	}

	boolean isOpclan() {
		if(clanId.equals(UUID.fromString("00000000-0000-0000-0000-000000000000")) || clanName.equals("Server") && description.equals("Server Operator Clan"))
			isOpclan = true;
		return isOpclan;
	}

	public JsonObject toJsonObject() {
		JsonObject ret = new JsonObject();
		ret.addProperty("clanName", clanName);
		ret.add("clanBanner", null);
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
		ret.addProperty("isOpclan", isOpclan());
		ret.addProperty("rent", rent);
		ret.addProperty("wins", wins);
		ret.addProperty("losses", losses);
		ret.addProperty("shield", shield);
		ret.addProperty("rentTimestamp", rentTimeStamp);
		ret.addProperty("upkeepTimestamp", upkeepTimeStamp);

		return ret;
	}
}
