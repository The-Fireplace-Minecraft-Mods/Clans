package dev.the_fireplace.clans.legacy.commands.raiding;

import dev.the_fireplace.clans.legacy.ClansModContainer;
import dev.the_fireplace.clans.legacy.cache.RaidingParties;
import dev.the_fireplace.clans.legacy.clan.accesscontrol.ClanLocks;
import dev.the_fireplace.clans.legacy.clan.metadata.ClanNames;
import dev.the_fireplace.clans.legacy.commands.RaidSubCommand;
import dev.the_fireplace.clans.legacy.logic.LandProtectionLogic;
import dev.the_fireplace.clans.legacy.model.Raid;
import dev.the_fireplace.clans.legacy.util.ChunkUtils;
import dev.the_fireplace.clans.legacy.util.EntityUtil;
import dev.the_fireplace.clans.legacy.util.TextStyles;
import dev.the_fireplace.clans.legacy.util.translation.TranslationUtil;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandThru extends RaidSubCommand
{
    @Override
    public String getName() {
        return "thru";
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
        Raid r = RaidingParties.getRaid(sender);
        if (r == null || !r.isActive()) {
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.common.not_raiding").setStyle(TextStyles.RED));
            return;
        }
        RayTraceResult lookRay = EntityUtil.getLookRayTrace(sender, 4);
        if (lookRay == null || lookRay.typeOfHit != RayTraceResult.Type.BLOCK) {
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.thru.not_block").setStyle(TextStyles.RED));
            return;
        }
        BlockPos targetBlockPos = lookRay.getBlockPos();
        UUID targetPosClan = ChunkUtils.getChunkOwner(sender.world.getChunk(targetBlockPos));
        if (!r.getTarget().equals(targetPosClan)) {
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.thru.wrong_pos_owner", ClanNames.get(r.getTarget()).getName()).setStyle(TextStyles.RED));
            return;
        }

        if (ClanLocks.get(targetPosClan).isLocked(targetBlockPos) || !ClansModContainer.getConfig().isEnableStealing() && LandProtectionLogic.isContainer(sender.world, targetBlockPos, null, null)) {
            for (int step = 2; step < 9; step++) {
                BlockPos telePos = EntityUtil.getSafeLocation(sender.world, targetBlockPos.offset(lookRay.sideHit.getOpposite(), step));
                if (telePos != null) {
                    sender.attemptTeleport(telePos.getX(), telePos.getY(), telePos.getZ());
                    return;
                }
            }
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.thru.failed").setStyle(TextStyles.RED));
        } else {
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.common.not_locked").setStyle(TextStyles.RED));
        }
    }
}
