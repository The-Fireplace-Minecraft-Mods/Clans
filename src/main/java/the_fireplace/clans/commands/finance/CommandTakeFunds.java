package the_fireplace.clans.commands.finance;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
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
public class CommandTakeFunds extends ClanSubCommand {
	@Override
	public String getName() {
		return "takefunds";
	}

	@Override
	public EnumRank getRequiredClanRank() {
		return EnumRank.LEADER;
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
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) throws CommandException {
		if(!Clans.getConfig().isLeaderWithdrawFunds())
			throw new CommandException(TranslationUtil.getRawTranslationString(sender.getUniqueID(), "commands.clan.takefunds.disabled"));
		if(!selectedClan.isLimitless()) {
			long amount = Long.parseLong(args[0]);
			if(Clans.getPaymentHandler().deductAmount(amount, selectedClan.getClanId())) {
				if(Clans.getPaymentHandler().addAmount(amount, sender.getUniqueID())) {
					sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.takefunds.success", amount, Clans.getPaymentHandler().getCurrencyName(amount), selectedClan.getClanName()).setStyle(TextStyles.GREEN));
					selectedClan.messageAllOnline(sender, TextStyles.GREEN, "commands.clan.takefunds.taken", sender.getDisplayNameString(), amount, Clans.getPaymentHandler().getCurrencyName(amount), selectedClan.getClanName());
				} else {
					Clans.getPaymentHandler().addAmount(amount, selectedClan.getClanId());
					sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "clans.error.no_player_econ_acct").setStyle(TextStyles.RED));
				}
			} else
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.common.insufficient_clan_funds", selectedClan.getClanName()).setStyle(TextStyles.RED));
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.not_on_limitless", "takefunds", selectedClan.getClanName()).setStyle(TextStyles.RED));
	}
}
