package the_fireplace.clans.legacy.commands.op.management;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.admin.AdminControlledClanSettings;
import the_fireplace.clans.clan.metadata.ClanNames;
import the_fireplace.clans.economy.Economy;
import the_fireplace.clans.legacy.commands.OpClanSubCommand;
import the_fireplace.clans.legacy.util.TextStyles;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;

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
		Clan c = ClanNames.getClanByName(clan);
		if(c != null) {
            if(!AdminControlledClanSettings.get().isServerOwned()) {
				double amount;
				try {
					amount = parseDouble(args[1]);
					if (amount < 0)
						amount = 0;
				} catch (NumberFormatException | NumberInvalidException e) {
					sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.addfunds.format").setStyle(TextStyles.RED));
					return;
				}
				if (Economy.addAmount(amount, c.getClanMetadata().getClanId()))
					sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.addfunds.success", Economy.getFormattedCurrency(amount), c.getClanMetadata().getClanName()).setStyle(TextStyles.GREEN));
				else
					sender.sendMessage(TranslationUtil.getTranslation(sender, "clans.error.no_clan_econ_acct").setStyle(TextStyles.RED));
			} else
				sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.not_on_server", "addfunds", clan).setStyle(TextStyles.RED));
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.notfound", clan).setStyle(TextStyles.RED));
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		return args.length == 1 ? getListOfStringsMatchingLastWord(args, ClanNames.getClanNames()) : Collections.emptyList();
	}
}
