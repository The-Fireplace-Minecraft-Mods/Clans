package the_fireplace.clans.commands.finance;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandTakeFunds extends ClanSubCommand {
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
	public String getUsage(ICommandSender sender) {
		return TranslationUtil.getRawTranslationString(sender, "commands.clan.takefunds.usage");
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) throws CommandException {
		if(!Clans.cfg.leaderWithdrawFunds)
			throw new CommandException(TranslationUtil.getRawTranslationString(sender.getUniqueID(), "commands.clan.takefunds.disabled"));
		long amount = Long.valueOf(args[0]);
		if(Clans.getPaymentHandler().deductAmount(amount, selectedClan.getClanId())) {
			if(Clans.getPaymentHandler().addAmount(amount, sender.getUniqueID()))
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.takefunds.success", amount, Clans.getPaymentHandler().getCurrencyName(amount), selectedClan.getClanName()).setStyle(TextStyles.GREEN));
			else {
				Clans.getPaymentHandler().addAmount(amount, selectedClan.getClanId());
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "clans.error.no_player_econ_acct").setStyle(TextStyles.RED));
			}
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.common.insufficient_clan_funds", selectedClan.getClanName()).setStyle(TextStyles.RED));
	}
}
