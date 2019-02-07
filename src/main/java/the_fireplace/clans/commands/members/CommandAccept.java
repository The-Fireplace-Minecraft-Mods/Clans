package the_fireplace.clans.commands.members;

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

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandAccept extends ClanSubCommand {
	@Override
	public EnumRank getRequiredClanRank() {
		return EnumRank.NOCLAN;
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
		return "/clan accept";
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		Clan acceptClan = ClanCache.getInvite(sender.getUniqueID());
		if(acceptClan != null){
			acceptClan.addMember(sender.getUniqueID());
			sender.sendMessage(new TextComponentTranslation(MinecraftColors.GREEN + "You joined %s.", acceptClan.getClanName()));
		} else
			sender.sendMessage(new TextComponentString(MinecraftColors.RED + "You don't have any pending invites."));
	}
}
