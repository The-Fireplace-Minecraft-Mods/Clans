package the_fireplace.clans.commands.teleportation;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.event.Timer;
import the_fireplace.clans.util.CapHelper;
import the_fireplace.clans.util.Pair;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.TranslationUtil;

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
		return TranslationUtil.getRawTranslationString(sender, "commands.clan.home.usage");
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		BlockPos home = selectedClan.getHome();
		int playerDim = sender.dimension;

		int cooldown = CapHelper.getPlayerClanCapability(sender).getCooldown();
		if(cooldown <= 0) {
			if (!selectedClan.hasHome() || home == null)
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.home.nohome", selectedClan.getClanName()).setStyle(TextStyles.RED));
			else {
				if(Clans.cfg.clanHomeWarmupTime > 0)
					Timer.clanHomeWarmups.put(sender, new Pair<>(Clans.cfg.clanHomeWarmupTime, ClanCache.getPlayerClans(sender.getUniqueID()).indexOf(selectedClan)));
				else
					teleportHome(sender, selectedClan, home, playerDim);
			}
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.home.cooldown", cooldown).setStyle(TextStyles.RED));
	}

	public static void teleportHome(EntityPlayerMP player, Clan playerClan, BlockPos home, int playerDim) {
		home = getSafeExitLocation(Objects.requireNonNull(player.getServer()).getWorld(playerClan.getHomeDim()), home, 5);
		if (playerDim == playerClan.getHomeDim()) {
			completeTeleportHome(player, home, playerDim);
		} else {
			player.setPortal(player.getPosition());
			if (player.changeDimension(playerClan.getHomeDim()) != null) {
				completeTeleportHome(player, home, playerDim);
			} else {
				player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "commands.clan.home.dim_error").setStyle(TextStyles.RED));
			}
		}
	}

	private static void completeTeleportHome(EntityPlayerMP player, BlockPos home, int playerDim) {
		if (!player.attemptTeleport(home.getX(), home.getY(), home.getZ())) {
			player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "commands.clan.home.blocked").setStyle(TextStyles.RED));
			if (playerDim != player.dimension && player.changeDimension(playerDim) == null)
				player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "commands.clan.home.return_dim").setStyle(TextStyles.RED));
		} else
			CapHelper.getPlayerClanCapability(player).setCooldown(Clans.cfg.clanHomeCooldownTime);
	}

	private static BlockPos getSafeExitLocation(World worldIn, BlockPos pos, int tries) {
		int i = pos.getX();
		int j = pos.getY();
		int k = pos.getZ();

		for (int l = 0; l <= 1; ++l) {
			int i1 = i - Integer.compare(pos.getX(), 0) * l - 1;
			int j1 = k - Integer.compare(pos.getZ(), 0) * l - 1;
			int k1 = i1 + 2;
			int l1 = j1 + 2;

			for (int i2 = i1; i2 <= k1; ++i2) {
				for (int j2 = j1; j2 <= l1; ++j2) {
					BlockPos blockpos = new BlockPos(i2, j, j2);

					if (hasRoomForPlayer(worldIn, blockpos) || --tries <= 0)
						return blockpos;
				}
			}
		}

		return pos;
	}

	private static boolean hasRoomForPlayer(World worldIn, BlockPos pos) {
		return worldIn.getBlockState(pos.down()).isTopSolid() && !worldIn.getBlockState(pos).getMaterial().isSolid() && !worldIn.getBlockState(pos.up()).getMaterial().isSolid();
	}
}
