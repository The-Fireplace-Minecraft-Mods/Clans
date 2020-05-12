package the_fireplace.clans.commands.lock;

import com.google.common.collect.Lists;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.ClansHelper;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.util.ChunkUtils;
import the_fireplace.clans.util.MultiblockUtil;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandUnlockChunk extends ClanSubCommand {
	@Override
	public String getName() {
		return "unlockchunk";
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
		UUID allowedUnlockPlayer = UUID.fromString("00000000-0000-0000-0000-000000000000");
		boolean all = false;
		if(args.length == 0)
			allowedUnlockPlayer = sender.getUniqueID();
		else if(selectedClan.hasPerm("lockadmin", sender.getUniqueID())) {
			if (args[0].equalsIgnoreCase("all") || args[0].equals("*"))
				all = true;
			else
				allowedUnlockPlayer = parsePlayerName(server, args[0]).getId();
		} else {
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.unlockchunk.failed").setStyle(TextStyles.RED));
			return;
		}

		Chunk c = sender.world.getChunk(sender.getPosition());
		if (!selectedClan.getId().equals(ChunkUtils.getChunkOwner(c))) {
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.common.not_claimed_by", selectedClan.getName()).setStyle(TextStyles.RED));
			return;
		}
		for(int y=0; y <= 255; y++)
			for(int x=c.getPos().getXStart(); x <= c.getPos().getXEnd(); x++)
				for(int z=c.getPos().getZStart(); z <= c.getPos().getZEnd(); z++) {
					BlockPos targetBlockPos = new BlockPos(x, y, z);
					if (!all && selectedClan.isLocked(targetBlockPos) && !allowedUnlockPlayer.equals(selectedClan.getLockOwner(targetBlockPos)))
						continue;
					IBlockState state = sender.getEntityWorld().getBlockState(targetBlockPos);
					if (ClansHelper.getConfig().getLockableBlocks().contains(state.getBlock().getRegistryName().toString())) {
						selectedClan.delLock(targetBlockPos);
						for(BlockPos pos: MultiblockUtil.getLockingConnectedPositions(sender.world, targetBlockPos, state))
							selectedClan.delLock(pos);
					}
				}
		sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.unlockchunk.success").setStyle(TextStyles.GREEN));
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		if(args.length == 1){
			List<String> completions = Lists.newArrayList("all");
			if(sender instanceof EntityPlayerMP) {
				Clan chunkClan = ClanCache.getClanById(ChunkUtils.getChunkOwner(sender.getEntityWorld().getChunk(sender.getPosition())));
				if(chunkClan != null)
					for (UUID member : chunkClan.getMembers().keySet())
						completions.add(Objects.requireNonNull(server.getPlayerProfileCache().getProfileByUUID(member)).getName());
			}
			return getListOfStringsMatchingLastWord(args, completions);
		}
		return Collections.emptyList();
	}
}
