package the_fireplace.clans.commands.members;

import com.google.common.collect.Lists;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.util.MinecraftColors;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandLeave extends ClanSubCommand {
	@Override
	public EnumRank getRequiredClanRank() {
		return EnumRank.MEMBER;
	}

	@Override
	public int getMinArgs() {
		return 0;
	}

	@Override
	public int getMaxArgs() {
		return 0;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/clan leave";
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		EnumRank senderRank = selectedClan.getMembers().get(sender.getUniqueID());
		if(senderRank == EnumRank.LEADER) {
			if(selectedClan.getMembers().size() == 1){
				sender.sendMessage(new TextComponentTranslation(MinecraftColors.RED + "You are the last member of your clan. To disband it, use /clan disband."));
				return;
			}
			List<UUID> leaders = Lists.newArrayList();
			for(UUID member: selectedClan.getMembers().keySet())
				if(selectedClan.getMembers().get(member).equals(EnumRank.LEADER))
					leaders.add(member);
			if(leaders.size() <= 1) {
				sender.sendMessage(new TextComponentString(MinecraftColors.RED + "You cannot leave the clan without a leader. Promote someone else to be a leader before leaving."));
				return;
			}
		}
		if(selectedClan.removeMember(sender.getUniqueID())) {
			updateDefaultClan(sender, selectedClan);
			sender.sendMessage(new TextComponentString(MinecraftColors.GREEN + "You have left the clan."));
		} else //Internal error because this should be unreachable
			sender.sendMessage(new TextComponentTranslation(MinecraftColors.RED + "Internal Error: You were unable to be removed from your clan."));
	}

	/**
	 * Check if a clan is the player's default clan, and if it is, update the player's default clan to something else.
	 * @param player
	 * The player to check and update (if needed)
	 * @param removeClan
	 * The clan the player is being removed from. Use null to forcibly change the player's default clan, regardless of what it currently is.
	 */
	public static void updateDefaultClan(EntityPlayerMP player, @Nullable Clan removeClan) {
		UUID oldDef = player.getCapability(Clans.CLAN_DATA_CAP, null).getDefaultClan();
		if(removeClan == null || removeClan.getClanId().equals(oldDef))
			if(ClanCache.getPlayerClans(player.getUniqueID()).isEmpty())
				player.getCapability(Clans.CLAN_DATA_CAP, null).setDefaultClan(null);
			else
				player.getCapability(Clans.CLAN_DATA_CAP, null).setDefaultClan(ClanCache.getPlayerClans(player.getUniqueID()).get(0).getClanId());
	}
}
