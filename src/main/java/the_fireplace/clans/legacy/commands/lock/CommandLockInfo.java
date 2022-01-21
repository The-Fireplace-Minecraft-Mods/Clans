package the_fireplace.clans.legacy.commands.lock;

import com.mojang.authlib.GameProfile;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import the_fireplace.clans.clan.accesscontrol.ClanLocks;
import the_fireplace.clans.legacy.commands.ClanSubCommand;
import the_fireplace.clans.legacy.model.EnumRank;
import the_fireplace.clans.legacy.util.ChunkUtils;
import the_fireplace.clans.legacy.util.EntityUtil;
import the_fireplace.clans.legacy.util.TextStyles;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandLockInfo extends ClanSubCommand
{
    @Override
    public String getName() {
        return "lockinfo";
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
    public void run(MinecraftServer server, EntityPlayerMP sender, String[] args) throws CommandException {
        RayTraceResult lookRay = EntityUtil.getLookRayTrace(sender, 4);
        if (lookRay == null || lookRay.typeOfHit != RayTraceResult.Type.BLOCK) {
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.lockinfo.not_block").setStyle(TextStyles.RED));
            return;
        }
        BlockPos targetBlockPos = lookRay.getBlockPos();
        UUID targetChunkClan = ChunkUtils.getChunkOwner(sender.world.getChunk(targetBlockPos));

        if (targetChunkClan != null && ClanLocks.get(targetChunkClan).isLocked(targetBlockPos)) {
            GameProfile prof = server.getPlayerProfileCache().getProfileByUUID(Objects.requireNonNull(ClanLocks.get(targetChunkClan).getLockOwner(targetBlockPos)));
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.lockinfo.locked_by", prof != null ? prof.getName() : "unknown")
                .appendText(" ").appendSibling(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.lockinfo.type", Objects.requireNonNull(ClanLocks.get(targetChunkClan).getLockType(targetBlockPos)).name())).setStyle(TextStyles.GREEN));
            Map<UUID, Boolean> overrides = ClanLocks.get(targetChunkClan).getLockOverrides(targetBlockPos);
            if (!overrides.isEmpty()) {
                sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.lockinfo.overrides"));
                for (Map.Entry<UUID, Boolean> entry : overrides.entrySet()) {
                    GameProfile p2 = server.getPlayerProfileCache().getProfileByUUID(entry.getKey());
                    sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), entry.getValue() ? "commands.clan.lockinfo.allowed" : "commands.clan.lockinfo.denied", p2 != null ? p2.getName() : "unknown"));
                }
            }
        } else {
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.common.not_locked").setStyle(TextStyles.GREEN));
        }
    }
}
