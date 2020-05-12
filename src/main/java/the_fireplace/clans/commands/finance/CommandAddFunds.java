package the_fireplace.clans.commands.finance;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.ClansHelper;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandAddFunds extends ClanSubCommand {
	@Override
	public String getName() {
		return "addfunds";
	}

	@Override
	public EnumRank getRequiredClanRank() {
		return EnumRank.MEMBER;
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
		if(!selectedClan.isServer()) {
			double amount;
			try {
				amount = parseDouble(args[0]);
				if(amount < 0)
					amount = 0;
			} catch(NumberFormatException|CommandException e) {
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.addfunds.format").setStyle(TextStyles.RED));
				return;
			}
			if(ClansHelper.getPaymentHandler().deductAmount(amount, sender.getUniqueID())) {
				if(ClansHelper.getPaymentHandler().addAmount(amount, selectedClan.getId())) {
					sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.addfunds.success", ClansHelper.getPaymentHandler().getFormattedCurrency(amount), selectedClan.getName()).setStyle(TextStyles.GREEN));
					selectedClan.messageAllOnline(sender, TextStyles.GREEN, "commands.clan.addfunds.added", sender.getDisplayNameString(), ClansHelper.getPaymentHandler().getFormattedCurrency(amount), selectedClan.getName());
				} else {
					ClansHelper.getPaymentHandler().addAmount(amount, sender.getUniqueID());
					sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "clans.error.no_clan_econ_acct").setStyle(TextStyles.RED));
				}
			} else
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.common.insufficient_funds").setStyle(TextStyles.RED));
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.not_on_server", "addfunds", selectedClan.getName()).setStyle(TextStyles.RED));
	}
}
