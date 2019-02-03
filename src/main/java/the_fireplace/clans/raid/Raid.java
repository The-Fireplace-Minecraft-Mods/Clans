package the_fireplace.clans.raid;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayerMP;
import the_fireplace.clans.clan.Clan;

import java.util.ArrayList;

public class Raid {
	private String raidName;
	private ArrayList<EntityPlayerMP> members;
	private Clan target;

	public Raid(String raidName, EntityPlayerMP starter, Clan targetClan){
		this.raidName = raidName;
		members = Lists.newArrayList();
		addMember(starter);
		this.target = targetClan;
		RaidingParties.addRaid(raidName, this);
	}

	public ArrayList<EntityPlayerMP> getMembers() {
		return members;
	}

	public String getRaidName() {
		return raidName;
	}

	public int getMemberCount(){
		return members.size();
	}

	public void addMember(EntityPlayerMP player) {
		this.members.add(player);
		RaidingParties.addRaider(player, this);
	}

	public boolean removeMember(EntityPlayerMP player) {
		boolean rm = this.members.remove(player);
		if(rm) {
			RaidingParties.removeRaider(player);
			if(this.members.isEmpty())
				RaidingParties.removeRaid(this);
		}
		return rm;
	}

	public Clan getTarget() {
		return target;
	}
}
