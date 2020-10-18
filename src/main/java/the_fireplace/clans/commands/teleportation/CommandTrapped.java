package the_fireplace.clans.commands.teleportation;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.Clans;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.cache.RaidingParties;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.model.ChunkPosition;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.util.ChunkUtils;
import the_fireplace.clans.util.EntityUtil;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandTrapped extends ClanSubCommand {
	@Override
	public String getName() {
		return "trapped";
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
	public void run(MinecraftServer server, EntityPlayerMP player, String[] args) {
		Chunk origin = player.world.getChunk(player.getPosition());
		Clan chunkOwner = ClanCache.getClanById(ChunkUtils.getChunkOwner(origin));
		if(chunkOwner == null && Clans.getConfig().shouldProtectWilderness() && player.getPosition().getY() >= Clans.getConfig().getMinWildernessY()) {
			BlockPos spawn = player.world.getSpawnPoint();
			player.attemptTeleport(spawn.getX(), spawn.getY(), spawn.getZ());
		} else if(chunkOwner != null && !chunkOwner.getMembers().containsKey(player.getUniqueID()) && (!RaidingParties.hasActiveRaid(chunkOwner) || !RaidingParties.getActiveRaid(chunkOwner).getAttackers().contains(player.getUniqueID()))) {
			Chunk safeChunk = EntityUtil.findSafeChunkFor(player, new ChunkPosition(origin));
			EntityUtil.teleportSafelyToChunk(player, safeChunk);
		} else
			player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "commands.clan.trapped.fail").setStyle(TextStyles.RED));
	}

}
