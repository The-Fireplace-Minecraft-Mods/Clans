package the_fireplace.clans.commands.members;

import com.google.common.collect.Lists;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import the_fireplace.clans.util.MinecraftColors;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.ClanSubCommand;

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
		Clan playerClan = ClanCache.getPlayerClan(sender.getUniqueID());
		assert playerClan != null;
		EnumRank senderRank = playerClan.getMembers().get(sender.getUniqueID());
		if(senderRank == EnumRank.LEADER) {
			if(playerClan.getMembers().size() == 1){
				sender.sendMessage(new TextComponentTranslation(MinecraftColors.RED + "You are the last member of your clan. To disband it, use /clan disband."));
				return;
			}
			List<UUID> leaders = Lists.newArrayList();
			for(UUID member: playerClan.getMembers().keySet())
				if(playerClan.getMembers().get(member).equals(EnumRank.LEADER))
					leaders.add(member);
			if(leaders.size() <= 1)
				sender.sendMessage(new TextComponentString(MinecraftColors.RED + "You cannot leave the clan without a leader. Promote someone else to be a leader before leaving."));
			else {
				if(playerClan.removeMember(sender.getUniqueID())) {
					sender.sendMessage(new TextComponentString(MinecraftColors.GREEN + "You have left the clan."));
				} else //Internal error because this should be unreachable
					sender.sendMessage(new TextComponentTranslation(MinecraftColors.RED + "Internal Error: You were unable to be removed from your clan."));
			}
		} else {
			if(playerClan.removeMember(sender.getUniqueID())) {
				sender.sendMessage(new TextComponentString(MinecraftColors.GREEN + "You have left the clan."));
			} else //Internal error because this should be unreachable
				sender.sendMessage(new TextComponentTranslation(MinecraftColors.RED + "Internal Error: You were unable to be removed from your clan."));
		}
	}
}
