package the_fireplace.clans.commands.raiding;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.RaidSubCommand;
import the_fireplace.clans.raid.Raid;
import the_fireplace.clans.raid.RaidingParties;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandStartRaid extends RaidSubCommand {
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
		return TranslationUtil.getRawTranslationString(sender, "commands.raid.start.usage");
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		if(RaidingParties.getRaidingPlayers().contains(sender.getUniqueID())) {
			Raid raid = RaidingParties.getRaid(sender);
			if (raid != null) {
				assert server != null;
				HashMap<EntityPlayerMP, EnumRank> clanPlayers = raid.getTarget().getOnlineMembers();
				if(clanPlayers.size() >= raid.getAttackerCount() - Clans.cfg.maxRaidersOffset) {
					if(!RaidingParties.hasActiveRaid(raid.getTarget())) {
						if(!RaidingParties.isPreparingRaid(raid.getTarget())) {
							long raidCost = Clans.cfg.startRaidCost;
							if (Clans.cfg.startRaidMultiplier)
								raidCost *= raid.getTarget().getClaimCount();
							raid.setCost(raidCost);
							if (Clans.getPaymentHandler().deductAmount(raidCost, sender.getUniqueID())) {
								RaidingParties.initRaid(raid.getTarget());
								sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.start.success", raid.getTarget().getClanName()).setStyle(TextStyles.GREEN));
							} else
								sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.start.insufficient_funds", raid.getTarget().getClanName(), raidCost, Clans.getPaymentHandler().getCurrencyName(raidCost)).setStyle(TextStyles.RED));
						} else
							sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.start.raiding").setStyle(TextStyles.RED));
					} else //This should not be possible
						sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.start.error", Math.round(100f*(Clans.cfg.defenseShield*60f*60f+raid.getRemainingSeconds())/60f/60f)/100f).setStyle(TextStyles.RED));
				} else
					sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.invite.limit", raid.getAttackerCount(), clanPlayers.size() + Clans.cfg.maxRaidersOffset).setStyle(TextStyles.RED));
			} else {
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.common.notinparty").setStyle(TextStyles.RED));
				Clans.LOGGER.error("Player was in getRaidingPlayers but getRaid was null!");
			}
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.common.notinparty").setStyle(TextStyles.RED));
	}
}
