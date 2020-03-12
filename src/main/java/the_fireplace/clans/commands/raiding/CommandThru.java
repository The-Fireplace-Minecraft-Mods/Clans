package the_fireplace.clans.commands.raiding;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import the_fireplace.clans.ClansHelper;
import the_fireplace.clans.cache.RaidingParties;
import the_fireplace.clans.commands.RaidSubCommand;
import the_fireplace.clans.logic.LandProtectionEventLogic;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.model.Raid;
import the_fireplace.clans.util.ChunkUtils;
import the_fireplace.clans.util.EntityUtil;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandThru extends RaidSubCommand {
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
	public void run(MinecraftServer server, EntityPlayerMP sender, String[] args) {
		Raid r = RaidingParties.getRaid(sender);
		if(r == null || !r.isActive()) {
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.common.not_raiding").setStyle(TextStyles.RED));
			return;
		}
		RayTraceResult lookRay = EntityUtil.getLookRayTrace(sender, 4);
		if(lookRay == null || lookRay.typeOfHit != RayTraceResult.Type.BLOCK) {
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.thru.not_block").setStyle(TextStyles.RED));
			return;
		}
		BlockPos targetBlockPos = lookRay.getBlockPos();
		Clan targetPosClan = ChunkUtils.getChunkOwnerClan(sender.world.getChunk(targetBlockPos));
		if(!r.getTarget().equals(targetPosClan)) {
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.thru.wrong_pos_owner", r.getTarget().getName()).setStyle(TextStyles.RED));
			return;
		}

		if(r.getTarget().isLocked(targetBlockPos) || !ClansHelper.getConfig().isEnableStealing() && LandProtectionEventLogic.isContainer(sender.world, targetBlockPos, null, null)){
			for (int step = 2; step < 9; step++) {
				BlockPos telePos = EntityUtil.getSafeLocation(sender.world, targetBlockPos.offset(lookRay.sideHit.getOpposite(), step), step-1);
				if(telePos != null) {
					sender.attemptTeleport(telePos.getX(), telePos.getY(), telePos.getZ());
					return;
				}
			}
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.thru.failed").setStyle(TextStyles.RED));
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.common.not_locked").setStyle(TextStyles.RED));
	}
}
