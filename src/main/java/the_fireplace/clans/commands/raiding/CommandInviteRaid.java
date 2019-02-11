package the_fireplace.clans.commands.raiding;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.RaidSubCommand;
import the_fireplace.clans.raid.Raid;
import the_fireplace.clans.raid.RaidingParties;
import the_fireplace.clans.util.MinecraftColors;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandInviteRaid extends RaidSubCommand {
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
		return "/raid invite <player>";
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) throws CommandException {
		if(!RaidingParties.getRaidingPlayers().contains(sender)) {
			Raid raid = RaidingParties.getRaid(sender);
			if (raid != null) {
				assert server != null;
				EntityPlayerMP targetPlayer = getPlayer(server, sender, args[0]);
				HashMap<EntityPlayerMP, EnumRank> clanPlayers = raid.getTarget().getOnlineMembers(server, sender);
				if(clanPlayers.size() > raid.getMemberCount() - Clans.cfg.maxRaidersOffset) {
					if(!clanPlayers.containsKey(targetPlayer)) {
						targetPlayer.sendMessage(new TextComponentString(MinecraftColors.GREEN + "You have been invited to a raid against " + raid.getTarget().getClanName() + "! To join, type /raid join " + raid.getTarget().getClanName()));
						sender.sendMessage(new TextComponentString(MinecraftColors.GREEN + "You successfully invited " + targetPlayer.getName() + " to the raid!"));
					} else
						sender.sendMessage(new TextComponentString(MinecraftColors.RED + "You cannot invite someone to raid their own clan!"));
				} else
					sender.sendMessage(new TextComponentString(MinecraftColors.RED + "Your raid cannot hold any more people!"));
			} else//Internal error because we should not reach this point
				sender.sendMessage(new TextComponentString(MinecraftColors.RED + "Internal error: You are not in a raid!"));
		} else
			sender.sendMessage(new TextComponentString(MinecraftColors.RED + "You are not in a raid!"));
	}
}
