package the_fireplace.clans.commands.teleportation;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.Clans;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.forge.ClansForge;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.Objects;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandSetHome extends ClanSubCommand {
	@Override
	public EnumRank getRequiredClanRank() {
		return EnumRank.LEADER;
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
		return TranslationUtil.getRawTranslationString(sender, "commands.clan.sethome.usage");
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		Chunk c = sender.getEntityWorld().getChunk(sender.getPosition());
		if(c.hasCapability(ClansForge.CLAIMED_LAND, null) && selectedClan.getClanId().equals(Objects.requireNonNull(c.getCapability(ClansForge.CLAIMED_LAND, null)).getClan())) {
			for(Map.Entry<Clan, BlockPos> pos: ClanCache.getClanHomes().entrySet())
				if(pos.getValue() != null && pos.getKey() != selectedClan && pos.getValue().getDistance(sender.getPosition().getX(), sender.getPosition().getY(), sender.getPosition().getZ()) < Clans.getConfig().getMinClanHomeDist()) {
					sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.sethome.proximity", Clans.getConfig().getMinClanHomeDist()).setStyle(TextStyles.RED));
					return;
				}
			selectedClan.setHome(sender.getPosition(), sender.dimension);
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.sethome.success").setStyle(TextStyles.GREEN));
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.sethome.territory").setStyle(TextStyles.RED));
	}
}
