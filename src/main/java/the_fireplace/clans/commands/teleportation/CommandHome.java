package the_fireplace.clans.commands.teleportation;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.clan.NewClan;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.event.Timer;
import the_fireplace.clans.util.CapHelper;
import the_fireplace.clans.util.Pair;
import the_fireplace.clans.util.TextStyles;

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
		BlockPos home = selectedClan.getHome();
		int playerDim = sender.dimension;

		int cooldown = CapHelper.getPlayerClanCapability(sender).getCooldown();
		if(cooldown <= 0) {
			if (!selectedClan.hasHome() || home == null)
				sender.sendMessage(new TextComponentTranslation("Error: %s does not have a set home. The clan leader should use /clan sethome to set one.", selectedClan.getClanName()).setStyle(TextStyles.RED));
			else {
				if(Clans.cfg.clanHomeWarmupTime > 0)
					Timer.clanHomeWarmups.put(sender, new Pair<>(Clans.cfg.clanHomeWarmupTime, ClanCache.getPlayerClans(sender.getUniqueID()).indexOf(selectedClan)));
				else
					teleportHome(sender, selectedClan, home, playerDim);
			}
		} else
			sender.sendMessage(new TextComponentString("You cannot use this command until your cooldown runs out in "+cooldown+" seconds.").setStyle(TextStyles.RED));
	}

	public static void teleportHome(EntityPlayerMP player, NewClan playerClan, BlockPos home, int playerDim) {
		if (playerDim == playerClan.getHomeDim() || player.changeDimension(playerClan.getHomeDim()) != null) {
			if (!player.attemptTeleport(home.getX(), home.getY(), home.getZ())) {
				player.sendMessage(new TextComponentString("Error teleporting to clan home. Ensure that it is not blocked.").setStyle(TextStyles.RED));
				if (playerDim != player.dimension && player.changeDimension(playerDim) == null)
					player.sendMessage(new TextComponentString("Error teleporting you back to the dimension you were in.").setStyle(TextStyles.RED));
			} else
				CapHelper.getPlayerClanCapability(player).setCooldown(Clans.cfg.clanHomeCooldownTime);
		} else
			player.sendMessage(new TextComponentString("Error teleporting to clan home dimension.").setStyle(TextStyles.RED));
	}
}
