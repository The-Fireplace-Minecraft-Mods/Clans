package the_fireplace.clans.commands.lock;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.Clans;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.model.EnumLockType;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.util.ChunkUtils;
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
		EnumLockType mode;
		if(args.length == 0)
			mode = EnumLockType.PRIVATE;
		else
			switch(args[0].toLowerCase()) {
				case "private":
				case "p":
					mode = EnumLockType.PRIVATE;
					break;
				case "clan":
				case "c":
					mode = EnumLockType.CLAN;
					break;
				case "open":
				case "public":
				case "o":
					mode = EnumLockType.OPEN;
					break;
				default:
					throw new WrongUsageException(getUsage(sender));
			}
		Chunk c = sender.world.getChunk(sender.getPosition());
		for(int y=0; y <= 255; y++)
			for(int x=c.getPos().getXStart(); x <= c.getPos().getXEnd(); x++)
				for(int z=c.getPos().getZStart(); z <= c.getPos().getZEnd(); z++) {
					BlockPos targetBlockPos = new BlockPos(x, y, z);
					if (!selectedClan.getClanId().equals(ChunkUtils.getChunkOwner(sender.world.getChunk(targetBlockPos))))
						continue;
					if (selectedClan.isLocked(targetBlockPos) && !selectedClan.isLockOwner(targetBlockPos, sender.getUniqueID()))
						continue;
					if (Clans.getConfig().getLockableBlocks().contains(sender.getEntityWorld().getBlockState(targetBlockPos).getBlock().getRegistryName().toString()))
						selectedClan.addLock(targetBlockPos, mode, sender.getUniqueID());
				}
		sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.lockchunk.success").setStyle(TextStyles.GREEN));
	}
}
