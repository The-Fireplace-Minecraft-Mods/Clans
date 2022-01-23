package dev.the_fireplace.clans.legacy.commands.op.land;

import dev.the_fireplace.clans.legacy.commands.OpClanSubCommand;
import dev.the_fireplace.clans.legacy.logic.ClaimManagement;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OpCommandAbandonClaim extends OpClanSubCommand
{
    @Override
    public String getName() {
        return "abandonclaim";
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
    public void run(MinecraftServer server, ServerPlayerEntity sender, String[] args) {
        ClaimManagement.checkAndAttemptAbandon(sender, null);
    }

    @Override
    protected boolean allowConsoleUsage() {
        return false;
    }
}
