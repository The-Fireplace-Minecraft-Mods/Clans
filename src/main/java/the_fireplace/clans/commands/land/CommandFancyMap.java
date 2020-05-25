package the_fireplace.clans.commands.land;

import com.google.common.collect.Maps;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.Clans;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.data.ClaimData;
import the_fireplace.clans.model.ChunkPositionWithData;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandFancyMap extends ClanSubCommand {
	@Override
	public String getName() {
		return "fancymap";
	}

	private static final char[] mapchars = {'#', '&', '@', '*', '+', '<', '>', '~', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '2', '3', '4', '5', '6', '7', '8', '9', 'w', 'm'};
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

	@SuppressWarnings("Duplicates")
	@Override
	public void run(MinecraftServer server, EntityPlayerMP sender, String[] args) {

		World w = sender.getEntityWorld();
		Chunk center = w.getChunk(sender.getPosition());

		Map<UUID, Character> symbolMap = Maps.newHashMap();
		sender.sendMessage(new TextComponentString("=====================================================").setStyle(TextStyles.GREEN));
		new Thread(() -> {
			for(int z=center.z-26; z <= center.z + 26; z++) {
				StringBuilder row = new StringBuilder();
				for (int x = center.x - 26; x <= center.x + 26; x++) {
					String wildernessColor = center.z == z && center.x == x ? "\u00A79" : Clans.getConfig().isProtectWilderness() ? "\u00A7e" : "\u00A72";
					ChunkPositionWithData pos = ClaimData.getChunkPositionData(x, z, sender.getServerWorld().provider.getDimension());
					Clan clan = ClaimData.getChunkClan(pos);
					if(pos == null || clan == null)
						row.append(wildernessColor).append('-');
					else if(pos.isBorderland())
						row.append('\u00A7').append(Integer.toHexString(clan.getTextColor().getColorIndex())).append('-');
					else {
						if (!symbolMap.containsKey(clan.getId()))
							symbolMap.put(clan.getId(), mapchars[symbolMap.size() % mapchars.length]);
						row.append(center.z == z && center.x == x ? "\u00A79": '\u00A7'+Integer.toHexString(clan.getTextColor().getColorIndex())).append(symbolMap.get(clan.getId()));
					}
				}
				sender.sendMessage(new TextComponentString(row.toString()));
			}
			sender.sendMessage(new TextComponentString("=====================================================").setStyle(TextStyles.GREEN));
			for(Map.Entry<UUID, Character> symbol: symbolMap.entrySet()) {
				Clan c = ClanCache.getClanById(symbol.getKey());
				sender.sendMessage(new TextComponentString(symbol.getValue() + ": " +(c != null ? c.getName() : TranslationUtil.getStringTranslation(sender.getUniqueID(), "clans.wilderness"))).setStyle(new Style().setColor(c != null ? c.getTextColor() : TextFormatting.YELLOW)));
			}
		}).start();
	}
}
