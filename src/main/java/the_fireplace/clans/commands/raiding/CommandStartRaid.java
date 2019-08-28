package the_fireplace.clans.commands.raiding;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.Clans;
import the_fireplace.clans.cache.RaidingParties;
import the_fireplace.clans.commands.RaidSubCommand;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.model.Raid;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.Set;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandStartRaid extends RaidSubCommand {
	@Override
	public String getName() {
		return "start";
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
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		if(RaidingParties.getRaidingPlayers().contains(sender.getUniqueID())) {
			Raid raid = RaidingParties.getRaid(sender);
			if (raid != null) {
				assert server != null;
				Set<Map.Entry<EntityPlayerMP, EnumRank>> clanPlayers = raid.getTarget().getOnlineSurvivalMembers();
				if(clanPlayers.size() >= raid.getAttackerCount() - Clans.getConfig().getMaxRaidersOffset()) {
					if(!RaidingParties.hasActiveRaid(raid.getTarget())) {
						if(!RaidingParties.isPreparingRaid(raid.getTarget())) {
							long raidCost = Clans.getConfig().getStartRaidCost();
							if (Clans.getConfig().isStartRaidMultiplier())
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
						sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.start.error", Math.round(100f*(Clans.getConfig().getDefenseShield() *60f*60f+raid.getRemainingSeconds())/60f/60f)/100f).setStyle(TextStyles.RED));
				} else
					sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.invite.limit", raid.getAttackerCount(), clanPlayers.size() + Clans.getConfig().getMaxRaidersOffset()).setStyle(TextStyles.RED));
			} else {
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.common.notinparty").setStyle(TextStyles.RED));
				Clans.getMinecraftHelper().getLogger().error("Player was in getRaidingPlayers but getRaid was null!");
			}
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.common.notinparty").setStyle(TextStyles.RED));
	}
}
