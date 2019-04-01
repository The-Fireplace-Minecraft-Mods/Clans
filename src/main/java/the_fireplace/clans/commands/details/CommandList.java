package the_fireplace.clans.commands.details;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.clan.NewClan;
import the_fireplace.clans.clan.NewClanDatabase;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.util.TextStyles;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandList extends ClanSubCommand {
	@Override
	public EnumRank getRequiredClanRank() {
		return EnumRank.ANY;
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
		return "/clan list";
	}

	@Override
	protected void runFromAnywhere(MinecraftServer server, ICommandSender sender, String[] args) {
		sender.sendMessage(new TextComponentString("Clans on this server:").setStyle(TextStyles.GREEN));
		if(!NewClanDatabase.getClans().isEmpty()) {
			for (NewClan clan : NewClanDatabase.getClans())
				sender.sendMessage(new TextComponentString(clan.getClanName() + " - " + clan.getDescription()).setStyle(TextStyles.GREEN));
		} else
			sender.sendMessage(new TextComponentString("There are no clans on this server.").setStyle(TextStyles.YELLOW));
	}

	@Override
	protected boolean allowConsoleUsage() {
		return true;
	}
}
