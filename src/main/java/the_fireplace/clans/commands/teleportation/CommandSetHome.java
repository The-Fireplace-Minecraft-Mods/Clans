package the_fireplace.clans.commands.teleportation;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.clan.NewClan;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.util.TextStyles;

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
		return "/clan sethome";
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		Chunk c = sender.getEntityWorld().getChunk(sender.getPosition());
		if(c.hasCapability(Clans.CLAIMED_LAND, null) && selectedClan.getClanId().equals(Objects.requireNonNull(c.getCapability(Clans.CLAIMED_LAND, null)).getClan())) {
			for(Map.Entry<NewClan, BlockPos> pos: ClanCache.getClanHomes().entrySet())
				if(pos.getValue() != null && pos.getKey() != selectedClan && pos.getValue().getDistance(sender.getPosition().getX(), sender.getPosition().getY(), sender.getPosition().getZ()) < Clans.cfg.minClanHomeDist) {
					sender.sendMessage(new TextComponentString("You are too close to another clan's home! You must be at least "+Clans.cfg.minClanHomeDist+" blocks away from other clans' homes to set your clan home. Use /clan fancymap to see where nearby clans are.").setStyle(TextStyles.RED));
					return;
				}
			selectedClan.setHome(sender.getPosition(), sender.dimension);
			sender.sendMessage(new TextComponentString("Clan home set!").setStyle(TextStyles.GREEN));
		} else
			sender.sendMessage(new TextComponentString("Clan home can only be set in clan territory!").setStyle(TextStyles.RED));
	}

	@Override
	protected void runFromAnywhere(MinecraftServer server, ICommandSender sender, String[] args) {

	}
}
