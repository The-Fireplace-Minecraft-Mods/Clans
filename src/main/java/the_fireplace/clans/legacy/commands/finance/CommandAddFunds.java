package the_fireplace.clans.legacy.commands.finance;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.clan.admin.AdminControlledClanSettings;
import the_fireplace.clans.clan.membership.ClanMemberMessager;
import the_fireplace.clans.economy.Economy;
import the_fireplace.clans.legacy.commands.ClanSubCommand;
import the_fireplace.clans.legacy.model.EnumRank;
import the_fireplace.clans.legacy.util.TextStyles;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;

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
        if(!AdminControlledClanSettings.get().isServerOwned()) {
			double amount;
			try {
				amount = parseDouble(args[0]);
				if(amount < 0)
					amount = 0;
			} catch(NumberFormatException|CommandException e) {
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.addfunds.format").setStyle(TextStyles.RED));
				return;
			}
			if(Economy.deductAmount(amount, sender.getUniqueID())) {
				if(Economy.addAmount(amount, selectedClan.getClanMetadata().getClanId())) {
					sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.addfunds.success", Economy.getFormattedCurrency(amount), selectedClan.getClanMetadata().getClanName()).setStyle(TextStyles.GREEN));
                    ClanMemberMessager.get().messageAllOnline(sender, TextStyles.GREEN, "commands.clan.addfunds.added", sender.getDisplayNameString(), Economy.getFormattedCurrency(amount), selectedClan.getClanMetadata().getClanName());
                } else {
					Economy.addAmount(amount, sender.getUniqueID());
					sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "clans.error.no_clan_econ_acct").setStyle(TextStyles.RED));
				}
			} else
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.common.insufficient_funds").setStyle(TextStyles.RED));
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.not_on_server", "addfunds", selectedClan.getClanMetadata().getClanName()).setStyle(TextStyles.RED));
	}
}
