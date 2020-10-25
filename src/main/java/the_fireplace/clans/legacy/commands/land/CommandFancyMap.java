package the_fireplace.clans.legacy.commands.land;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.legacy.commands.ClanSubCommand;
import the_fireplace.clans.legacy.logic.ClaimMapToChat;
import the_fireplace.clans.legacy.model.EnumRank;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandFancyMap extends ClanSubCommand {
	@Override
	public String getName() {
		return "fancymap";
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
	public void run(MinecraftServer server, EntityPlayerMP sender, String[] args) {
		World w = sender.getEntityWorld();
		Chunk center = w.getChunk(sender.getPosition());

		ClaimMapToChat.createFancyMap(sender, center.getPos(), sender.dimension).prepareAndSend();
	}
}
