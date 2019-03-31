package the_fireplace.clans.commands.teleportation;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockBed;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
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

	@Override
	protected void runFromAnywhere(MinecraftServer server, ICommandSender sender, String[] args) {

	}

	public static void teleportHome(EntityPlayerMP player, NewClan playerClan, BlockPos home, int playerDim) {
		home = getSafeExitLocation(Objects.requireNonNull(player.getServer()).getWorld(playerClan.getHomeDim()), home, 5);
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
