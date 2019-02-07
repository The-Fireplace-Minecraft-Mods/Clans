package the_fireplace.clans.commands.members;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
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
public class CommandInvite extends ClanSubCommand {
	@Override
	public EnumRank getRequiredClanRank() {
		return EnumRank.ADMIN;//TODO Config option to let normal players or only the leader send invite
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
		return "/clan invite <player>";
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) throws CommandException {
		Clan playerClan = ClanCache.getPlayerClan(sender.getUniqueID());
		assert playerClan != null;
		EntityPlayerMP target = getPlayer(server, sender, args[0]);
		if(ClanCache.getPlayerClan(target.getUniqueID()) == null) {
			if(ClanCache.inviteToClan(target.getUniqueID(), playerClan))
				target.sendMessage(new TextComponentTranslation(MinecraftColors.GREEN + "You have been invited to join %1$s. To join %1$s, type /clan accept. To decline, type /clan decline.", playerClan.getClanName()));
			else
				sender.sendMessage(new TextComponentTranslation(MinecraftColors.RED + "The player %s has already been invited to join a clan. They must accept or decline that invitation first.", target.getName()));
		} else
			sender.sendMessage(new TextComponentTranslation(MinecraftColors.RED + "The player %s is already in a clan.", target.getName()));
	}
}
