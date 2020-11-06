package the_fireplace.clans.legacy.commands.finance;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.clan.admin.AdminControlledClanSettings;
import the_fireplace.clans.clan.membership.ClanMemberMessager;
import the_fireplace.clans.economy.Economy;
import the_fireplace.clans.legacy.ClansModContainer;
import the_fireplace.clans.legacy.commands.ClanSubCommand;
import the_fireplace.clans.legacy.model.EnumRank;
import the_fireplace.clans.legacy.util.TextStyles;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;

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
	public void run(MinecraftServer server, EntityPlayerMP sender, String[] args) throws CommandException {
		if(!ClansModContainer.getConfig().isLeaderWithdrawFunds())
			throw new CommandException(TranslationUtil.getRawTranslationString(sender.getUniqueID(), "commands.clan.takefunds.disabled"));
        if(!AdminControlledClanSettings.get().isServerOwned()) {
			double amount = parseDouble(args[0]);
			if(Economy.deductAmount(amount, selectedClan.getClanMetadata().getClanId())) {
				if(Economy.addAmount(amount, sender.getUniqueID())) {
					sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.takefunds.success", Economy.getFormattedCurrency(amount), selectedClan.getClanMetadata().getClanName()).setStyle(TextStyles.GREEN));
                    ClanMemberMessager.get().messageAllOnline(sender, TextStyles.GREEN, "commands.clan.takefunds.taken", sender.getDisplayNameString(), Economy.getFormattedCurrency(amount), selectedClan.getClanMetadata().getClanName());
                } else {
					Economy.addAmount(amount, selectedClan.getClanMetadata().getClanId());
					sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "clans.error.no_player_econ_acct").setStyle(TextStyles.RED));
				}
			} else
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.common.insufficient_clan_funds", selectedClan.getClanMetadata().getClanName()).setStyle(TextStyles.RED));
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.not_on_server", "takefunds", selectedClan.getClanMetadata().getClanName()).setStyle(TextStyles.RED));
	}
}
