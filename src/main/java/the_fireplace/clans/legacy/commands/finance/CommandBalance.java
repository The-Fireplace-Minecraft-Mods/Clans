package the_fireplace.clans.legacy.commands.finance;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.ClansModContainer;
import the_fireplace.clans.legacy.commands.ClanSubCommand;
import the_fireplace.clans.legacy.model.EnumRank;
import the_fireplace.clans.legacy.util.TextStyles;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandBalance extends ClanSubCommand {
	@Override
	public String getName() {
		return "balance";
	}

	@Override
	public EnumRank getRequiredClanRank() {
		return EnumRank.MEMBER;
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
		if(!selectedClan.isServer()) {
			double balance = ClansModContainer.getPaymentHandler().getBalance(selectedClan.getId());
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.balance.balance", selectedClan.getName(), ClansModContainer.getPaymentHandler().getFormattedCurrency(balance)).setStyle(TextStyles.GREEN));
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.not_on_server", "balance", selectedClan.getName()).setStyle(TextStyles.RED));
	}
}
