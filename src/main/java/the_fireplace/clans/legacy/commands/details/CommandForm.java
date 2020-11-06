package the_fireplace.clans.legacy.commands.details;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.membership.PlayerClans;
import the_fireplace.clans.clan.metadata.ClanNames;
import the_fireplace.clans.economy.Economy;
import the_fireplace.clans.legacy.ClansModContainer;
import the_fireplace.clans.legacy.commands.ClanSubCommand;
import the_fireplace.clans.legacy.config.Config;
import the_fireplace.clans.legacy.model.EnumRank;
import the_fireplace.clans.legacy.util.TextStyles;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;
import the_fireplace.clans.player.PlayerClanSettings;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandForm extends ClanSubCommand {
	@Override
	public String getName() {
		return "form";
	}

	@Override
	public EnumRank getRequiredClanRank() {
		return ClansModContainer.getConfig().isAllowMultiClanMembership() ? EnumRank.ANY : EnumRank.NOCLAN;
	}

	@Override
	public int getMinArgs() {
		return 1;
	}

	@Override
	public int getMaxArgs() {
		return 1;
	}

	@Override
	public void run(MinecraftServer server, EntityPlayerMP sender, String[] args) {
		if(selectedClan == null || ClansModContainer.getConfig().isAllowMultiClanMembership()) {
			String newClanName = TextStyles.stripFormatting(args[0]);
			if (Config.getInstance().chatCensor.censorClanNames)
				newClanName = ClansModContainer.getChatCensorCompat().getCensoredString(newClanName);
			if (ClansModContainer.getConfig().getMaxNameLength() > 0 && newClanName.length() > ClansModContainer.getConfig().getMaxNameLength())
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.setname.toolong", newClanName, ClansModContainer.getConfig().getMaxNameLength()).setStyle(TextStyles.RED));
			else if (!ClanNames.isClanNameAvailable(newClanName))
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.setname.taken", newClanName).setStyle(TextStyles.RED));
			else {
				if (Economy.deductAmount(ClansModContainer.getConfig().getFormClanCost(), sender.getUniqueID())) {
					Clan c = new Clan(newClanName, sender.getUniqueID());
					if(PlayerClans.countClansPlayerIsIn(sender.getUniqueID()) == 1)
						PlayerClanSettings.setDefaultClan(sender.getUniqueID(), c.getClanMetadata().getClanId());
					sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.form.success").setStyle(TextStyles.GREEN));
				} else
					sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.form.insufficient_funds", Economy.getFormattedCurrency(ClansModContainer.getConfig().getFormClanCost())).setStyle(TextStyles.RED));
			}
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.form.already_in_clan").setStyle(TextStyles.RED));
	}
}
