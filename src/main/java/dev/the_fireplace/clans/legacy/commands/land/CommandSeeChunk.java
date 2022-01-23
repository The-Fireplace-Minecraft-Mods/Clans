package dev.the_fireplace.clans.legacy.commands.land;

import dev.the_fireplace.clans.legacy.cache.PlayerCache;
import dev.the_fireplace.clans.legacy.commands.ClanSubCommand;
import dev.the_fireplace.clans.legacy.model.EnumRank;
import dev.the_fireplace.clans.legacy.util.ChunkUtils;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandSeeChunk extends ClanSubCommand
{
    @Override
    public String getName() {
        return "seechunk";
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
    public void run(MinecraftServer server, ServerPlayerEntity sender, String[] args) throws CommandException {
        World w = sender.getEntityWorld();
        Chunk c = w.getChunk(sender.getPosition());
        if (args.length == 1) {
            PlayerCache.setIsShowingChunkBorders(sender.getUniqueID(), parseBool(args[0]));
        }
        ChunkUtils.showChunkBounds(c, sender);
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "on", "off");
        }
        return Collections.emptyList();
    }
}
