package dev.the_fireplace.clans.legacy.commands.land;

import dev.the_fireplace.clans.legacy.commands.ClanSubCommand;
import dev.the_fireplace.clans.legacy.logic.ClaimMapToChat;
import dev.the_fireplace.clans.legacy.model.EnumRank;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandMap extends ClanSubCommand
{
    @Override
    public String getName() {
        return "map";
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
        return 0;
    }

    @Override
    public void run(MinecraftServer server, ServerPlayerEntity sender, String[] args) {
        World w = sender.getEntityWorld();
        Chunk senderChunk = w.getChunk(sender.getPosition());

        ClaimMapToChat.createAllianceMap(sender, senderChunk.getPos(), sender.dimension).prepareAndSend();
    }
}
