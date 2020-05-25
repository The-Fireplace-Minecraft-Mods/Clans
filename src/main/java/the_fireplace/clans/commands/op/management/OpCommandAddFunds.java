package the_fireplace.clans.commands.op.management;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import the_fireplace.clans.Clans;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.commands.OpClanSubCommand;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OpCommandAddFunds extends OpClanSubCommand {
	@Override
	public String getName() {
		return "addfunds";
	}

	@Override
	public int getMinArgs() {
		return 2;
	}

	@Override
	public int getMaxArgs() {
		return 2;
	}

	@Override
	protected void runFromAnywhere(MinecraftServer server, ICommandSender sender, String[] args) {
		String clan = args[0];
		Clan c = ClanCache.getClanByName(clan);
		if(c != null) {
			if(!c.isServer()) {
				double amount;
				try {
					amount = parseDouble(args[1]);
					if (amount < 0)
						amount = 0;
				} catch (NumberFormatException | NumberInvalidException e) {
					sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.addfunds.format").setStyle(TextStyles.RED));
					return;
				}
				if (Clans.getPaymentHandler().addAmount(amount, c.getId()))
					sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.addfunds.success", Clans.getPaymentHandler().getFormattedCurrency(amount), c.getName()).setStyle(TextStyles.GREEN));
				else
					sender.sendMessage(TranslationUtil.getTranslation(sender, "clans.error.no_clan_econ_acct").setStyle(TextStyles.RED));
			} else
				sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.not_on_server", "addfunds", clan).setStyle(TextStyles.RED));
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.notfound", clan).setStyle(TextStyles.RED));
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		return args.length == 1 ? getListOfStringsMatchingLastWord(args, ClanCache.getClanNames().keySet()) : Collections.emptyList();
	}
}
