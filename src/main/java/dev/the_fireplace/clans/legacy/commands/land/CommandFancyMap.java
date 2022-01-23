package dev.the_fireplace.clans.legacy.commands.land;

import dev.the_fireplace.clans.legacy.commands.ClanSubCommand;
import dev.the_fireplace.clans.legacy.logic.ClaimMapToChat;
import dev.the_fireplace.clans.legacy.model.EnumRank;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandFancyMap extends ClanSubCommand
{
    @Override
    public String getName() {
        return "fancymap";
    }

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
        return 1;
    }

    @Override
    public void run(MinecraftServer server, ServerPlayerEntity sender, String[] args) {
        if (args.length == 0) {
            ClaimMapToChat.sendSingleFancyMap(sender);
        } else {
            ClaimMapToChat.sendAllFancyMaps(sender);
        }
    }
}
