package the_fireplace.clans.commands.details;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.util.MinecraftColors;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandSetName extends ClanSubCommand {
	@Override
	public EnumRank getRequiredClanRank() {
		return EnumRank.LEADER;
	}

	@Override
	public int getMinArgs() {
		return 1;
	}

	@Override
	public int getMaxArgs() {
		return 1;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/clan setname <newname>";
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		String newName = args[0];
		if(Clans.cfg.maxNameLength > 0 && newName.length() > Clans.cfg.maxNameLength)
			sender.sendMessage(new TextComponentString(MinecraftColors.RED + "The clan name you have specified is too long. This server's maximum name length is: "+Clans.cfg.maxNameLength));
		else if(!ClanCache.clanNameTaken(newName)) {
			selectedClan.setClanName(newName);
			sender.sendMessage(new TextComponentString(MinecraftColors.GREEN + "Clan name set!"));
		} else
			sender.sendMessage(new TextComponentString(MinecraftColors.RED + "The clan name you have specified is already taken."));
	}
}
