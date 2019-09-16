package the_fireplace.clans.commands.lock;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.Clans;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.model.EnumLockType;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.util.ChunkUtils;
import the_fireplace.clans.util.MultiblockUtil;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandLockChunk extends ClanSubCommand {
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
	public void run(MinecraftServer server, EntityPlayerMP sender, String[] args) throws CommandException {
		EnumLockType mode = parseLockType(args.length == 0 ? null : args[0]);

		Chunk c = sender.world.getChunk(sender.getPosition());
		for(int y=0; y <= 255; y++)
			for(int x=c.getPos().getXStart(); x <= c.getPos().getXEnd(); x++)
				for(int z=c.getPos().getZStart(); z <= c.getPos().getZEnd(); z++) {
					BlockPos targetBlockPos = new BlockPos(x, y, z);
					if (!selectedClan.getClanId().equals(ChunkUtils.getChunkOwner(sender.world.getChunk(targetBlockPos))))
						continue;
					if (selectedClan.isLocked(targetBlockPos) && !selectedClan.isLockOwner(targetBlockPos, sender.getUniqueID()))
						continue;
					IBlockState state = sender.getEntityWorld().getBlockState(targetBlockPos);
					if (Clans.getConfig().getLockableBlocks().contains(state.getBlock().getRegistryName().toString())) {
						selectedClan.addLock(targetBlockPos, mode, sender.getUniqueID());
						for(BlockPos pos: MultiblockUtil.getLockingConnectedPositions(sender.world, targetBlockPos, state))
							selectedClan.addLock(pos, mode, sender.getUniqueID());
					}
				}
		sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.lockchunk.success").setStyle(TextStyles.GREEN));
	}
}
