package dev.the_fireplace.clans.legacy.commands.land;

import dev.the_fireplace.clans.legacy.commands.ClanSubCommand;
import dev.the_fireplace.clans.legacy.logic.ClaimManagement;
import dev.the_fireplace.clans.legacy.model.EnumRank;
import dev.the_fireplace.clans.legacy.util.PermissionManager;
import dev.the_fireplace.clans.multithreading.ConcurrentExecutionManager;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandClaim extends ClanSubCommand
{
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
    public void run(MinecraftServer server, ServerPlayerEntity sender, String[] args) throws CommandException {
        if (args.length == 0) {
            ClaimManagement.checkAndAttemptClaim(sender, selectedClan);
        } else if (hasClaimRadiusPermission(sender)) {
            int radius = parseInt(args[0]);
            ConcurrentExecutionManager.runKillable(() -> {
                boolean canClaimRadius = ClaimManagement.checkCanClaimRadius(sender, selectedClan, radius, "square");
                if (canClaimRadius) {
                    ClaimManagement.claimRadius(sender, selectedClan, radius);
                }
            });
        } else {
            throw new CommandException("commands.generic.permission");
        }
    }

    private static boolean hasClaimRadiusPermission(ServerPlayerEntity sender) {
        return PermissionManager.hasPermission(sender, PermissionManager.CLAN_COMMAND_PREFIX + "claim.radius", true);
    }
}
