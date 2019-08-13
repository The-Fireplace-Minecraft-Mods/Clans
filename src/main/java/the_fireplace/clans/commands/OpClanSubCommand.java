package the_fireplace.clans.commands;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.util.PermissionManager;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class OpClanSubCommand extends ClanSubCommand {
	@Override
	public final String getName() {
		return "opclan";
	}

	@Override
	public final boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return sender.canUseCommand(server.getOpPermissionLevel(), this.getName()) || PermissionManager.permissionManagementExists() && PermissionManager.hasPermission(sender, PermissionManager.OPCLAN_COMMAND_PREFIX + getUsage(server).split(" ")[1]);
	}

	@Override
	public final EnumRank getRequiredClanRank(){
		return EnumRank.LEADER;
	}

	@Override
	protected boolean allowConsoleUsage() {
		return true;
	}
}
