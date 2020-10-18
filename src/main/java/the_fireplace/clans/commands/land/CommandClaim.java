package the_fireplace.clans.commands.land;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.logic.ClanManagementLogic;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.util.PermissionManager;

import javax.annotation.ParametersAreNonnullByDefault;

import static the_fireplace.clans.util.PermissionManager.CLAN_COMMAND_PREFIX;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandClaim extends ClanSubCommand {
	@Override
	public String getName() {
		return "claim";
	}

	@Override
	public EnumRank getRequiredClanRank() {
		return EnumRank.ADMIN;
	}

	@Override
	public int getMinArgs() {
		return 0;
	}

	@Override
	public int getMaxArgs() {
		return 1;
	}

	@Override
	public void run(MinecraftServer server, EntityPlayerMP sender, String[] args) throws CommandException {
		if(args.length == 0)
			ClanManagementLogic.checkAndAttemptClaim(sender, selectedClan, false);
		else if(hasClaimRadiusPermission(sender) && ClanManagementLogic.checkCanClaimRadius(sender, selectedClan, parseInt(args[0]), "square"))
			ClanManagementLogic.claimRadius(sender, selectedClan, parseInt(args[0]), "square");
		else if(!hasClaimRadiusPermission(sender))
			throw new CommandException("commands.generic.permission");
	}

	private static boolean hasClaimRadiusPermission(EntityPlayerMP sender) {
		return !PermissionManager.permissionManagementExists() || PermissionManager.hasPermission(sender, CLAN_COMMAND_PREFIX + "claim.radius");
	}
}
