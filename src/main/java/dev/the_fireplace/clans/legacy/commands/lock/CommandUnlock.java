package dev.the_fireplace.clans.legacy.commands.lock;

import dev.the_fireplace.clans.legacy.clan.accesscontrol.ClanLocks;
import dev.the_fireplace.clans.legacy.clan.accesscontrol.ClanPermissions;
import dev.the_fireplace.clans.legacy.commands.ClanSubCommand;
import dev.the_fireplace.clans.legacy.model.EnumRank;
import dev.the_fireplace.clans.legacy.util.ChunkUtils;
import dev.the_fireplace.clans.legacy.util.EntityUtil;
import dev.the_fireplace.clans.legacy.util.MultiblockUtil;
import dev.the_fireplace.clans.legacy.util.TextStyles;
import dev.the_fireplace.clans.legacy.util.translation.TranslationUtil;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandUnlock extends ClanSubCommand
{
    @Override
    public String getName() {
        return "unlock";
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
        RayTraceResult lookRay = EntityUtil.getLookRayTrace(sender, 4);
        if (lookRay == null || lookRay.typeOfHit != RayTraceResult.Type.BLOCK) {
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.unlock.not_block").setStyle(TextStyles.RED));
            return;
        }
        BlockPos targetBlockPos = lookRay.getBlockPos();
        if (!selectedClan.equals(ChunkUtils.getChunkOwner(sender.world.getChunk(targetBlockPos)))) {
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.lock.wrong_owner", selectedClanName).setStyle(TextStyles.RED));
            return;
        }
        if (ClanLocks.get(selectedClan).isLocked(targetBlockPos) && !ClanLocks.get(selectedClan).isLockOwner(targetBlockPos, sender.getUniqueID()) && !ClanPermissions.get(selectedClan).hasPerm("lockadmin", sender.getUniqueID())) {
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.unlock.locked", Objects.requireNonNull(server.getPlayerProfileCache().getProfileByUUID(Objects.requireNonNull(ClanLocks.get(selectedClan).getLockOwner(targetBlockPos)))).getName()).setStyle(TextStyles.RED));
            return;
        }
        IBlockState state = sender.getEntityWorld().getBlockState(targetBlockPos);
        if (ClanLocks.get(selectedClan).isLocked(targetBlockPos)) {
            ClanLocks.get(selectedClan).delLock(targetBlockPos);
            for (BlockPos pos : MultiblockUtil.getLockingConnectedPositions(sender.world, targetBlockPos, state)) {
                ClanLocks.get(selectedClan).delLock(pos);
            }
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.unlock.success").setStyle(TextStyles.GREEN));
        } else {
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.common.not_locked").setStyle(TextStyles.RED));
        }
    }
}
