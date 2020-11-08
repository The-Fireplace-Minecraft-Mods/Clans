package the_fireplace.clans.legacy.commands.teleportation;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.clan.membership.ClanMembers;
import the_fireplace.clans.legacy.ClansModContainer;
import the_fireplace.clans.legacy.cache.RaidingParties;
import the_fireplace.clans.legacy.commands.ClanSubCommand;
import the_fireplace.clans.legacy.model.ChunkPosition;
import the_fireplace.clans.legacy.model.EnumRank;
import the_fireplace.clans.legacy.util.ChunkUtils;
import the_fireplace.clans.legacy.util.EntityUtil;
import the_fireplace.clans.legacy.util.TextStyles;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

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
		UUID chunkOwner = ChunkUtils.getChunkOwner(origin);
        if(chunkOwner == null && ClansModContainer.getConfig().shouldProtectWilderness() && player.getPosition().getY() >= ClansModContainer.getConfig().getMinWildernessY()) {
			BlockPos spawn = player.world.getSpawnPoint();
			player.attemptTeleport(spawn.getX(), spawn.getY(), spawn.getZ());
		} else if(chunkOwner != null && !ClanMembers.get(chunkOwner).isMember(player.getUniqueID()) && (!RaidingParties.hasActiveRaid(chunkOwner) || !RaidingParties.getActiveRaid(chunkOwner).getAttackers().contains(player.getUniqueID()))) {
			Chunk safeChunk = EntityUtil.findSafeChunkFor(player, new ChunkPosition(origin));
			EntityUtil.teleportSafelyToChunk(player, safeChunk);
		} else
			player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "commands.clan.trapped.fail").setStyle(TextStyles.RED));
	}

}
