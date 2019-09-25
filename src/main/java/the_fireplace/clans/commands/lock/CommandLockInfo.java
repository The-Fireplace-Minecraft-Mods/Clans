package the_fireplace.clans.commands.lock;

import com.mojang.authlib.GameProfile;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.util.EntityUtil;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandLockInfo extends ClanSubCommand {
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
		if(lookRay == null || lookRay.typeOfHit != RayTraceResult.Type.BLOCK) {
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.lockinfo.not_block").setStyle(TextStyles.RED));
			return;
		}
		BlockPos targetBlockPos = lookRay.getBlockPos();
		if(selectedClan.isLocked(targetBlockPos)) {
			GameProfile prof = server.getPlayerProfileCache().getProfileByUUID(Objects.requireNonNull(selectedClan.getLockOwner(targetBlockPos)));
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.lockinfo.locked_by", prof != null ? prof.getName() : "unknown").setStyle(TextStyles.GREEN));
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.common.not_locked").setStyle(TextStyles.GREEN));
	}
}
