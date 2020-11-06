package the_fireplace.clans.legacy.commands.teleportation;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import the_fireplace.clans.clan.home.ClanHomes;
import the_fireplace.clans.legacy.ClansModContainer;
import the_fireplace.clans.legacy.cache.PlayerCache;
import the_fireplace.clans.legacy.commands.ClanSubCommand;
import the_fireplace.clans.legacy.model.EnumRank;
import the_fireplace.clans.legacy.util.EntityUtil;
import the_fireplace.clans.legacy.util.TextStyles;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;
import the_fireplace.clans.player.PlayerHomeCooldown;

import javax.annotation.ParametersAreNonnullByDefault;

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
	public void run(MinecraftServer server, EntityPlayerMP sender, String[] args) throws CommandException {
		if(ClansModContainer.getConfig().getClanHomeWarmupTime() <= -1)
			throw new CommandException(TranslationUtil.getRawTranslationString(sender, "commands.clan.home.disabled"));
        BlockPos home = ClanHomes.get().getHome();
		int playerDim = sender.dimension;

		boolean isCoolingDown = PlayerHomeCooldown.isCoolingDown(sender.getUniqueID());
		if(!isCoolingDown || sender.isCreative()) {
            if (!ClanHomes.get().hasHome() || home == null)
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.home.nohome", selectedClan.getClanMetadata().getClanName()).setStyle(TextStyles.RED));
			else {
				if(ClansModContainer.getConfig().getClanHomeWarmupTime() > 0 && !sender.isCreative()) {
					sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.home.warmup", selectedClan.getClanMetadata().getClanName(), ClansModContainer.getConfig().getClanHomeWarmupTime()).setStyle(TextStyles.GREEN));
					PlayerCache.setClanHomeCheckX(sender.getUniqueID(), (float)sender.posX);
					PlayerCache.setClanHomeCheckY(sender.getUniqueID(), (float)sender.posY);
					PlayerCache.setClanHomeCheckZ(sender.getUniqueID(), (float)sender.posZ);
					PlayerCache.startHomeTeleportWarmup(sender, selectedClan.getClanMetadata().getClanId());
				} else
                    EntityUtil.teleportHome(sender, home, ClanHomes.get().getHomeDim(), playerDim, false);
			}
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.home.cooldown", PlayerHomeCooldown.getCooldown(sender.getUniqueID())).setStyle(TextStyles.RED));
	}
}
