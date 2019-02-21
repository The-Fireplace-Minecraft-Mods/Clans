package the_fireplace.clans.commands.details;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.event.Timer;
import the_fireplace.clans.util.MinecraftColors;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

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
		assert playerClan != null;
		BlockPos home = playerClan.getHome();
		int playerDim = sender.dimension;

		//noinspection ConstantConditions
		if(sender.hasCapability(Clans.CLAN_HOME, null)) {
			//noinspection ConstantConditions
			int cooldown = sender.getCapability(Clans.CLAN_HOME, null).getCooldown();
			if(cooldown <= 0) {
				if (!playerClan.hasHome())
					sender.sendMessage(new TextComponentString(MinecraftColors.RED + "Error: Your clan does not have a set home. The clan leader should use /clan sethome to set one."));
				else {
					if(Clans.cfg.clanHomeWarmupTime > 0)
						Timer.clanHomeWarmups.put(sender, Clans.cfg.clanHomeWarmupTime);
					else
						teleportHome(sender, playerClan, home, playerDim);
				}
			} else
				sender.sendMessage(new TextComponentString(MinecraftColors.RED + "You cannot use this command until your cooldown runs out in "+cooldown+" seconds."));
		} else
			sender.sendMessage(new TextComponentString(MinecraftColors.RED + "Internal error: Player does not have a cooldown timer."));
	}

	public static void teleportHome(EntityPlayerMP player, Clan playerClan, BlockPos home, int playerDim) {
		if (playerDim == playerClan.getHomeDim() || player.changeDimension(playerClan.getHomeDim()) != null) {
			if (!player.attemptTeleport(home.getX(), home.getY(), home.getZ())) {
				player.sendMessage(new TextComponentString(MinecraftColors.RED + "Error teleporting to clan home. Ensure that it is not blocked."));
				if (playerDim != player.dimension && player.changeDimension(playerDim) == null)
					player.sendMessage(new TextComponentString(MinecraftColors.RED + "Error teleporting you back to the dimension you were in."));
			} else
				//noinspection ConstantConditions
				player.getCapability(Clans.CLAN_HOME, null).setCooldown(Clans.cfg.clanHomeCooldownTime);
		} else
			player.sendMessage(new TextComponentString(MinecraftColors.RED + "Error teleporting to clan home dimension."));
	}
}
