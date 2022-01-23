package dev.the_fireplace.clans.legacy.commands.lock;

import dev.the_fireplace.clans.legacy.ClansModContainer;
import dev.the_fireplace.clans.legacy.clan.accesscontrol.ClanLocks;
import dev.the_fireplace.clans.legacy.clan.accesscontrol.ClanPermissions;
import dev.the_fireplace.clans.legacy.commands.ClanSubCommand;
import dev.the_fireplace.clans.legacy.model.EnumLockType;
import dev.the_fireplace.clans.legacy.model.EnumRank;
import dev.the_fireplace.clans.legacy.util.ChunkUtils;
import dev.the_fireplace.clans.legacy.util.MultiblockUtil;
import dev.the_fireplace.clans.legacy.util.TextStyles;
import dev.the_fireplace.clans.legacy.util.translation.TranslationUtil;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandLockChunk extends ClanSubCommand
{
    @Override
    public String getName() {
        return "lockchunk";
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
        EnumLockType mode = parseLockType(args.length == 0 ? null : args[0]);

        Chunk c = sender.world.getChunk(sender.getPosition());
        if (!selectedClan.equals(ChunkUtils.getChunkOwner(c))) {
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.common.not_claimed_by", selectedClanName).setStyle(TextStyles.RED));
            return;
        }
        if (!ClanPermissions.get(selectedClan).hasPerm("lock." + mode.toString().toLowerCase(), sender.getUniqueID())) {
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.lock.permission", selectedClanName, mode.toString().toLowerCase()).setStyle(TextStyles.RED));
            return;
        }
        for (int y = 0; y <= 255; y++) {
            for (int x = c.getPos().getXStart(); x <= c.getPos().getXEnd(); x++) {
                for (int z = c.getPos().getZStart(); z <= c.getPos().getZEnd(); z++) {
                    BlockPos targetBlockPos = new BlockPos(x, y, z);
                    if (ClanLocks.get(selectedClan).isLocked(targetBlockPos) && !ClanLocks.get(selectedClan).isLockOwner(targetBlockPos, sender.getUniqueID())) {
                        continue;
                    }
                    IBlockState state = sender.getEntityWorld().getBlockState(targetBlockPos);
                    if (ClansModContainer.getConfig().getLockableBlocks().contains(state.getBlock().getRegistryName().toString())) {
                        ClanLocks.get(selectedClan).addLock(targetBlockPos, mode, sender.getUniqueID());
                        for (BlockPos pos : MultiblockUtil.getLockingConnectedPositions(sender.world, targetBlockPos, state)) {
                            ClanLocks.get(selectedClan).addLock(pos, mode, sender.getUniqueID());
                        }
                    }
                }
            }
        }
        sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.lockchunk.success").setStyle(TextStyles.GREEN));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "private", "clan", "open");
        }
        return Collections.emptyList();
    }
}
