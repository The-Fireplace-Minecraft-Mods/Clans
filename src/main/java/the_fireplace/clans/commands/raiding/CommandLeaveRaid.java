package the_fireplace.clans.commands.raiding;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import the_fireplace.clans.commands.RaidSubCommand;
import the_fireplace.clans.raid.Raid;
import the_fireplace.clans.raid.RaidingParties;
import the_fireplace.clans.util.MinecraftColors;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandLeaveRaid extends RaidSubCommand {
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
		return "/raid leave";
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		if(!RaidingParties.getRaidingPlayers().contains(sender)) {
			Raid raid = RaidingParties.getRaid(sender);
			if (raid != null) {
				raid.removeMember(sender);
				sender.sendMessage(new TextComponentString(MinecraftColors.GREEN + "You successfully left the raid!"));
			} else//Internal error because we should not reach this point
				sender.sendMessage(new TextComponentString(MinecraftColors.RED + "Internal error: You are not in a raid!"));
		} else
			sender.sendMessage(new TextComponentString(MinecraftColors.RED + "You are not in a raid!"));
	}
}
