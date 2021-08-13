package the_fireplace.clans.legacy.commands.land;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.legacy.commands.ClanSubCommand;
import the_fireplace.clans.legacy.logic.ClaimManagement;
import the_fireplace.clans.legacy.model.EnumRank;
import the_fireplace.clans.legacy.util.PermissionManager;
import the_fireplace.clans.multithreading.ConcurrentExecutionManager;

import javax.annotation.ParametersAreNonnullByDefault;

import static the_fireplace.clans.legacy.util.PermissionManager.CLAN_COMMAND_PREFIX;

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
			ClaimManagement.checkAndAttemptClaim(sender, selectedClan);
		else if(hasClaimRadiusPermission(sender)) {
			int radius = parseInt(args[0]);
			ConcurrentExecutionManager.runKillable(() -> {
				boolean canClaimRadius = ClaimManagement.checkCanClaimRadius(sender, selectedClan, radius, "square");
				if (canClaimRadius) {
					ClaimManagement.claimRadius(sender, selectedClan, radius);
				}
			});
		} else
			throw new CommandException("commands.generic.permission");
	}

	private static boolean hasClaimRadiusPermission(EntityPlayerMP sender) {
		return PermissionManager.hasPermission(sender, CLAN_COMMAND_PREFIX + "claim.radius", true);
	}
}
