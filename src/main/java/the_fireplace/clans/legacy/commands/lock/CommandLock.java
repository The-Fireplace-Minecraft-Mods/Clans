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
import the_fireplace.clans.legacy.ClansModContainer;
import the_fireplace.clans.legacy.commands.ClanSubCommand;
import the_fireplace.clans.legacy.model.EnumLockType;
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

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandLock extends ClanSubCommand {
	@Override
	public String getName() {
		return "lock";
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
	public void run(MinecraftServer server, EntityPlayerMP sender, String[] args) throws CommandException {
		EnumLockType mode = parseLockType(args.length == 0 ? null : args[0]);
		RayTraceResult lookRay = EntityUtil.getLookRayTrace(sender, 4);
		if(lookRay == null || lookRay.typeOfHit != RayTraceResult.Type.BLOCK) {
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.lock.not_block").setStyle(TextStyles.RED));
			return;
		}
		BlockPos targetBlockPos = lookRay.getBlockPos();
		if(!selectedClan.getClanMetadata().getClanId().equals(ChunkUtils.getChunkOwner(sender.world.getChunk(targetBlockPos)))) {
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.lock.wrong_owner", selectedClan.getClanMetadata().getClanName()).setStyle(TextStyles.RED));
			return;
		}
        if(!ClanPermissions.get().hasPerm("lock." + mode.toString().toLowerCase(), sender.getUniqueID())) {
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.lock.permission", selectedClan.getClanMetadata().getClanName(), mode.toString().toLowerCase()).setStyle(TextStyles.RED));
			return;
		}
        if(ClanLocks.get().isLocked(targetBlockPos) && !ClanLocks.get().isLockOwner(targetBlockPos, sender.getUniqueID())) {
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.lock.locked", Objects.requireNonNull(server.getPlayerProfileCache().getProfileByUUID(Objects.requireNonNull(ClanLocks.get().getLockOwner(targetBlockPos)))).getName()).setStyle(TextStyles.RED));
			return;
		}
		IBlockState state = sender.getEntityWorld().getBlockState(targetBlockPos);
		if(ClansModContainer.getConfig().getLockableBlocks().contains(state.getBlock().getRegistryName().toString())) {
            ClanLocks.get().addLock(targetBlockPos, mode, sender.getUniqueID());
            for(BlockPos pos: MultiblockUtil.getLockingConnectedPositions(sender.world, targetBlockPos, state))
                ClanLocks.get().addLock(pos, mode, sender.getUniqueID());
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.lock.success").setStyle(TextStyles.GREEN));
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.lock.failed").setStyle(TextStyles.RED));
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		if(args.length == 1)
			return getListOfStringsMatchingLastWord(args, "private", "clan", "open");
		return Collections.emptyList();
	}
}
