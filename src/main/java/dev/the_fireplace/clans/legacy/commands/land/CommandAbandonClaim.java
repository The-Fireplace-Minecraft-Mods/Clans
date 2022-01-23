package dev.the_fireplace.clans.legacy.commands.land;

import dev.the_fireplace.clans.legacy.commands.ClanSubCommand;
import dev.the_fireplace.clans.legacy.logic.ClaimManagement;
import dev.the_fireplace.clans.legacy.model.EnumRank;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandAbandonClaim extends ClanSubCommand
{
    @Override
    public String getName() {
        return "abandonclaim";
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
        return 0;
    }

    @Override
    public void run(MinecraftServer server, ServerPlayerEntity sender, String[] args) {
        ClaimManagement.checkAndAttemptAbandon(sender, selectedClan);
    }
}
