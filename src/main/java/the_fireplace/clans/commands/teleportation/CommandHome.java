package the_fireplace.clans.commands.teleportation;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import the_fireplace.clans.Clans;
import the_fireplace.clans.cache.PlayerCache;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.data.PlayerData;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.model.OrderedPair;
import the_fireplace.clans.util.EntityUtil;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandHome extends ClanSubCommand {
	@Override
	public String getName() {
		return "home";
	}

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
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) throws CommandException {
		if(Clans.getConfig().getClanHomeWarmupTime() <= -1)
			throw new CommandException(TranslationUtil.getRawTranslationString(sender, "commands.clan.home.disabled"));
		BlockPos home = selectedClan.getHome();
		int playerDim = sender.dimension;

		int cooldown = PlayerData.getCooldown(sender.getUniqueID());
		if(cooldown <= 0) {
			if (!selectedClan.hasHome() || home == null)
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.home.nohome", selectedClan.getName()).setStyle(TextStyles.RED));
			else {
				if(Clans.getConfig().getClanHomeWarmupTime() > 0) {
					sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.home.warmup", selectedClan.getName(), Clans.getConfig().getClanHomeWarmupTime()).setStyle(TextStyles.GREEN));
					PlayerCache.setClanHomeCheckX(sender.getUniqueID(), (float)sender.posX);
					PlayerCache.setClanHomeCheckY(sender.getUniqueID(), (float)sender.posY);
					PlayerCache.setClanHomeCheckZ(sender.getUniqueID(), (float)sender.posZ);
					PlayerCache.clanHomeWarmups.put(sender, new OrderedPair<>(Clans.getConfig().getClanHomeWarmupTime(), selectedClan.getId()));
				} else
					teleportHome(sender, selectedClan, home, playerDim, false);
			}
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.home.cooldown", cooldown).setStyle(TextStyles.RED));
	}

	public static void teleportHome(EntityPlayer player, Clan playerClan, BlockPos home, int playerDim, boolean noCooldown) {
		home = EntityUtil.getSafeLocation(Objects.requireNonNull(player.getServer()).getWorld(playerClan.getHomeDim()), home, 5);
		if (playerDim == playerClan.getHomeDim()) {
			completeTeleportHome(player, home, playerDim, noCooldown);
		} else {
			player.setPortal(player.getPosition());
			if (player.changeDimension(playerClan.getHomeDim()) != null) {
				completeTeleportHome(player, home, playerDim, noCooldown);
			} else {
				player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "commands.clan.home.dim_error").setStyle(TextStyles.RED));
			}
		}
	}

	private static void completeTeleportHome(EntityPlayer player, @Nullable BlockPos home, int playerDim, boolean noCooldown) {
		if (home == null || !player.attemptTeleport(home.getX(), home.getY(), home.getZ())) {
			player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "commands.clan.home.blocked").setStyle(TextStyles.RED));
			if (playerDim != player.dimension && player.changeDimension(playerDim) == null)
				player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "commands.clan.home.return_dim").setStyle(TextStyles.RED));
		} else if(!noCooldown)
			PlayerData.setCooldown(player.getUniqueID(), Clans.getConfig().getClanHomeCooldownTime());
	}
}
