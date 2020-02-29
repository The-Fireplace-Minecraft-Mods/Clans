package the_fireplace.clans.commands.finance;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.Clans;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;
import the_fireplace.grandeconomy.api.GrandEconomyApi;

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
			long balance = Clans.getPaymentHandler().getBalance(selectedClan.getId());
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.balance.balance", selectedClan.getName(), GrandEconomyApi.toString(balance)).setStyle(TextStyles.GREEN));
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.not_on_server", "balance", selectedClan.getName()).setStyle(TextStyles.RED));
	}
}
