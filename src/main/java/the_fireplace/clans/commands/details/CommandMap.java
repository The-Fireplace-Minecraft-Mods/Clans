package the_fireplace.clans.commands.details;

import com.google.common.collect.Maps;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.util.ChunkUtils;
import the_fireplace.clans.util.MinecraftColors;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandMap extends ClanSubCommand {
	private static final char[] mapchars = {'%', '&', '@', '*', '+', '<', '>', '~', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '2', '3', '4', '5', '6', '7', '8', '9', 'w', 'm'};
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
		return "/clan map";
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		assert server != null;
		World w = sender.getEntityWorld();
		Chunk center = w.getChunk(sender.getPosition());

		Map<UUID, Character> symbolMap = Maps.newHashMap();
		sender.sendMessage(new TextComponentString(MinecraftColors.GREEN + "====================================================="));
		for(int z=center.z-5; z <= center.x + 5; z++) {
			StringBuilder row = new StringBuilder();
			for (int x = center.x - 26; x <= center.x + 26; x++) {
				Chunk c = w.getChunk(x, z);
				UUID chunkOwner = ChunkUtils.getChunkOwner(c);
				if(chunkOwner == null)
					row.append('#');
				else {
					if(ClanCache.getClan(chunkOwner) == null) {
						ChunkUtils.setChunkOwner(c, null);
						row.append('#');
					} else {
						if (!symbolMap.containsKey(chunkOwner))
							symbolMap.put(chunkOwner, mapchars[symbolMap.size() % mapchars.length]);
						row.append(symbolMap.get(chunkOwner));
					}
				}
			}
			sender.sendMessage(new TextComponentString(MinecraftColors.GREEN + row));
		}
		sender.sendMessage(new TextComponentString(MinecraftColors.GREEN + "====================================================="));
		for(Map.Entry<UUID, Character> symbol: symbolMap.entrySet()) {
			Clan c = ClanCache.getClan(symbol.getKey());
			sender.sendMessage(new TextComponentString(MinecraftColors.GREEN + symbol.getValue() + ": " +(c != null ? c.getClanName() : "Wilderness")));
		}
	}
}
