package the_fireplace.clans.legacy.commands.lock;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import the_fireplace.clans.clan.accesscontrol.ClanLocks;
import the_fireplace.clans.clan.accesscontrol.ClanPermissions;
import the_fireplace.clans.legacy.commands.ClanSubCommand;
import the_fireplace.clans.legacy.model.EnumRank;
import the_fireplace.clans.legacy.util.ChunkUtils;
import the_fireplace.clans.legacy.util.EntityUtil;
import the_fireplace.clans.legacy.util.MultiblockUtil;
import the_fireplace.clans.legacy.util.TextStyles;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandDenyAccess extends ClanSubCommand
{
    @Override
    public String getName() {
        return "denyaccess";
    }

    @Override
    public EnumRank getRequiredClanRank() {
        return EnumRank.ADMIN;
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public int getMaxArgs() {
        return 1;
    }

    @Override
    public void run(MinecraftServer server, EntityPlayerMP sender, String[] args) throws CommandException {
        RayTraceResult lookRay = EntityUtil.getLookRayTrace(sender, 4);
        if (lookRay == null || lookRay.typeOfHit != RayTraceResult.Type.BLOCK) {
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.denyaccess.not_block").setStyle(TextStyles.RED));
            return;
        }
        BlockPos targetBlockPos = lookRay.getBlockPos();
        if (!selectedClan.equals(ChunkUtils.getChunkOwner(sender.world.getChunk(targetBlockPos)))) {
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.lock.wrong_owner", selectedClanName).setStyle(TextStyles.RED));
            return;
        }
        if (ClanLocks.get(selectedClan).isLocked(targetBlockPos) && !ClanLocks.get(selectedClan).isLockOwner(targetBlockPos, sender.getUniqueID()) && !ClanPermissions.get(selectedClan).hasPerm("lockadmin", sender.getUniqueID())) {
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.denyaccess.locked", Objects.requireNonNull(server.getPlayerProfileCache().getProfileByUUID(Objects.requireNonNull(ClanLocks.get(selectedClan).getLockOwner(targetBlockPos)))).getName()).setStyle(TextStyles.RED));
            return;
        }
        UUID targetPlayerId = parsePlayerName(server, args[0]).getId();
        IBlockState state = sender.getEntityWorld().getBlockState(targetBlockPos);
        if (ClanLocks.get(selectedClan).isLocked(targetBlockPos)) {
            ClanLocks.get(selectedClan).addLockOverride(targetBlockPos, targetPlayerId, false);
            for (BlockPos pos : MultiblockUtil.getLockingConnectedPositions(sender.world, targetBlockPos, state)) {
                ClanLocks.get(selectedClan).addLockOverride(pos, targetPlayerId, false);
            }
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.denyaccess.success", args[0]).setStyle(TextStyles.GREEN));
        } else {
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.common.not_locked").setStyle(TextStyles.RED));
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, server.getPlayerList().getOnlinePlayerNames()) : Collections.emptyList();
    }
}
