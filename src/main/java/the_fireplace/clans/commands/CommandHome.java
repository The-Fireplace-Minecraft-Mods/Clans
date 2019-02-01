package the_fireplace.clans.commands;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import the_fireplace.clans.MinecraftColors;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.ClanDatabase;
import the_fireplace.clans.clan.EnumRank;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandHome extends ClanSubCommand {
	@Override
	public EnumRank getRequiredClanRank() {
		return EnumRank.MEMBER;
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
		return "/clan home";
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		Clan playerClan = ClanCache.getPlayerClan(sender.getUniqueID());
		BlockPos home = playerClan.getHome();
		int playerDim = sender.dimension;
		if(!playerClan.hasHome())
			sender.sendMessage(new TextComponentString(MinecraftColors.RED+"Error: Your clan does not have a set home. The clan leader should use /clan sethome to set one."));
		else if(playerDim == playerClan.getHomeDim() || sender.changeDimension(playerClan.getHomeDim()) != null) {
			if (!sender.attemptTeleport(home.getX(), home.getY(), home.getZ())) {
				sender.sendMessage(new TextComponentString(MinecraftColors.RED + "Error teleporting to clan home. Ensure that it is not blocked."));
				if (playerDim != sender.dimension && sender.changeDimension(playerDim) == null)
					sender.sendMessage(new TextComponentString(MinecraftColors.RED + "Error teleporting you back to the dimension you were in."));
			}
		} else {
			sender.sendMessage(new TextComponentString(MinecraftColors.RED + "Error teleporting to clan home dimension."));
		}
	}
}
