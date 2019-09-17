package the_fireplace.clans.commands.lock;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.util.ChunkUtils;
import the_fireplace.clans.util.EntityUtil;
import the_fireplace.clans.util.MultiblockUtil;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandUnlock extends ClanSubCommand {
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
	public void run(MinecraftServer server, EntityPlayerMP sender, String[] args) {
		RayTraceResult lookRay = EntityUtil.getLookRayTrace(sender, 4);
		if(lookRay == null || lookRay.typeOfHit != RayTraceResult.Type.BLOCK) {
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.unlock.not_block").setStyle(TextStyles.RED));
			return;
		}
		BlockPos targetBlockPos = lookRay.getBlockPos();
		if(!selectedClan.getClanId().equals(ChunkUtils.getChunkOwner(sender.world.getChunk(targetBlockPos)))) {
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.lock.wrong_owner", selectedClan.getClanName()).setStyle(TextStyles.RED));
			return;
		}
		if(selectedClan.isLocked(targetBlockPos) && !selectedClan.isLockOwner(targetBlockPos, sender.getUniqueID())) {
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.unlock.locked", Objects.requireNonNull(server.getPlayerProfileCache().getProfileByUUID(Objects.requireNonNull(selectedClan.getLockOwner(targetBlockPos)))).getName()).setStyle(TextStyles.RED));
			return;
		}
		IBlockState state = sender.getEntityWorld().getBlockState(targetBlockPos);
		if(selectedClan.isLocked(targetBlockPos)) {
			selectedClan.delLock(targetBlockPos);
			for(BlockPos pos: MultiblockUtil.getLockingConnectedPositions(sender.world, targetBlockPos, state))
				selectedClan.delLock(pos);
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.unlock.success").setStyle(TextStyles.GREEN));
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.unlock.failed").setStyle(TextStyles.RED));
	}
}
