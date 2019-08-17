package the_fireplace.clans.commands.finance;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.Clans;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandAddFunds extends ClanSubCommand {
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
	public String getUsage(ICommandSender sender) {
		return TranslationUtil.getRawTranslationString(sender, "commands.clan.addfunds.usage");
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		if(!selectedClan.isLimitless()) {
			long amount;
			try {
				amount = Long.parseLong(args[0]);
				if(amount < 0)
					amount = 0;
			} catch(NumberFormatException e) {
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.addfunds.format").setStyle(TextStyles.RED));
				return;
			}
			if(Clans.getPaymentHandler().deductAmount(amount, sender.getUniqueID())) {
				if(Clans.getPaymentHandler().addAmount(amount, selectedClan.getClanId())) {
					sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.addfunds.success", amount, Clans.getPaymentHandler().getCurrencyName(amount), selectedClan.getClanName()).setStyle(TextStyles.GREEN));
					selectedClan.messageAllOnline(sender, TextStyles.GREEN, "commands.clan.addfunds.added", sender.getDisplayNameString(), amount, Clans.getPaymentHandler().getCurrencyName(amount), selectedClan.getClanName());
				} else {
					Clans.getPaymentHandler().addAmount(amount, sender.getUniqueID());
					sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "clans.error.no_clan_econ_acct").setStyle(TextStyles.RED));
				}
			} else
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.common.insufficient_funds").setStyle(TextStyles.RED));
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.not_on_limitless", "addfunds", selectedClan.getClanName()).setStyle(TextStyles.RED));
	}
}
