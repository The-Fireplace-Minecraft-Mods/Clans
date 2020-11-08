package the_fireplace.clans.legacy.commands.raiding;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.clan.membership.ClanMembers;
import the_fireplace.clans.clan.metadata.ClanNames;
import the_fireplace.clans.economy.Economy;
import the_fireplace.clans.legacy.ClansModContainer;
import the_fireplace.clans.legacy.cache.RaidingParties;
import the_fireplace.clans.legacy.commands.RaidSubCommand;
import the_fireplace.clans.legacy.model.Raid;
import the_fireplace.clans.legacy.util.FormulaParser;
import the_fireplace.clans.legacy.util.TextStyles;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;

import javax.annotation.ParametersAreNonnullByDefault;

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
	public void run(MinecraftServer server, EntityPlayerMP sender, String[] args) {
		if(RaidingParties.getRaidingPlayers().contains(sender.getUniqueID())) {
			Raid raid = RaidingParties.getRaid(sender);
			if (raid != null) {

                long clanDefenderCount = ClanMembers.get(raid.getTarget()).getRaidDefenderCount();
				if(clanDefenderCount >= raid.getAttackerCount() - ClansModContainer.getConfig().getMaxRaidersOffset()) {
					if(!RaidingParties.hasActiveRaid(raid.getTarget())) {
						if(!RaidingParties.isPreparingRaid(raid.getTarget())) {
							double raidCost = FormulaParser.eval(ClansModContainer.getConfig().getStartRaidCostFormula(), raid.getTarget(), raid, 0);
							raid.setCost(raidCost);
							if (Economy.deductAmount(raidCost, sender.getUniqueID())) {
								RaidingParties.initRaid(raid.getTarget());
                                sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.start.success", ClanNames.get(raid.getTarget()).getName()).setStyle(TextStyles.GREEN));
							} else
                                sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.start.insufficient_funds", ClanNames.get(raid.getTarget()).getName(), Economy.getFormattedCurrency(raidCost)).setStyle(TextStyles.RED));
						} else
							sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.start.raiding").setStyle(TextStyles.RED));
					} else //This should not be possible
						sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.start.error", Math.round(100f*(ClansModContainer.getConfig().getDefenseShield() *60f*60f+raid.getRemainingSeconds())/60f/60f)/100f).setStyle(TextStyles.RED));
				} else
					sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.invite.limit", raid.getAttackerCount(), clanDefenderCount + ClansModContainer.getConfig().getMaxRaidersOffset()).setStyle(TextStyles.RED));
			} else {
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.common.notinparty").setStyle(TextStyles.RED));
				ClansModContainer.getMinecraftHelper().getLogger().error("Player was in getRaidingPlayers but getRaid was null!");
			}
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.common.notinparty").setStyle(TextStyles.RED));
	}
}
