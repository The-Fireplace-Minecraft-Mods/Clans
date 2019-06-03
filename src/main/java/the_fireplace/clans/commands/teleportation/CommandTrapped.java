package the_fireplace.clans.commands.teleportation;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.raid.RaidingParties;
import the_fireplace.clans.util.ChunkUtils;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandTrapped extends ClanSubCommand {
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
	public String getUsage(ICommandSender sender) {
		return TranslationUtil.getRawTranslationString(sender, "commands.clan.trapped.usage");
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP player, String[] args) {
		Chunk origin = player.world.getChunk(player.getPosition());
		Clan chunkOwner = ClanCache.getClanById(ChunkUtils.getChunkOwner(origin));
		if(chunkOwner == null && Clans.cfg.protectWilderness && player.getPosition().getY() >= Clans.cfg.minWildernessY) {
			BlockPos spawn = player.world.getSpawnPoint();
			player.attemptTeleport(spawn.getX(), spawn.getY(), spawn.getZ());
		} else if(chunkOwner != null && !chunkOwner.getMembers().containsKey(player.getUniqueID()) && (!RaidingParties.hasActiveRaid(chunkOwner) || !RaidingParties.getActiveRaid(chunkOwner).getMembers().contains(player.getUniqueID()))) {
			int x = 0, z = 0, tmp, dx = 0, dz = -1;
			while(true) {//Spiral out until a player friendly chunk is found
				Chunk test = player.world.getChunk(origin.x + x, origin.z + z);
				Clan testChunkOwner = ClanCache.getClanById(ChunkUtils.getChunkOwner(test));
				if(testChunkOwner == null || testChunkOwner.getMembers().containsKey(player.getUniqueID())) {
					player.attemptTeleport((test.getPos().getXStart() + test.getPos().getXEnd())/2f, test.getHeight(new BlockPos((test.getPos().getXStart() + test.getPos().getXEnd())/2f, 0, (test.getPos().getZStart() + test.getPos().getZEnd())/2f)), (test.getPos().getZStart() + test.getPos().getZEnd())/2f);
					break;
				}
				if(x == z || (x < 0 && x == -z) || (x > 0 && x == 1-z)) {
					tmp = dx;
					dx = -dz;
					dz = tmp;
				}
				x += dx;
				z += dz;
			}
		} else
			player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "commands.clan.trapped.fail").setStyle(TextStyles.RED));
	}
}
