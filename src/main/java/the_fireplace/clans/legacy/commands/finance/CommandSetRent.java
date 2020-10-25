package the_fireplace.clans.legacy.commands.finance;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.ClansModContainer;
import the_fireplace.clans.legacy.commands.ClanSubCommand;
import the_fireplace.clans.legacy.model.EnumRank;
import the_fireplace.clans.legacy.util.FormulaParser;
import the_fireplace.clans.legacy.util.TextStyles;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandSetRent extends ClanSubCommand {
	@Override
	public String getName() {
		return "setrent";
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
		if(ClansModContainer.getConfig().getChargeRentDays() <= 0)
			throw new CommandException(TranslationUtil.getRawTranslationString(sender, "commands.clan.setrent.disabled"));
		if(!selectedClan.isServer()) {
			double newRent = parseDouble(args[0]);
			if (newRent >= 0) {
				double maxRent = FormulaParser.eval(ClansModContainer.getConfig().getMaxRentFormula(), selectedClan, 0);
				if (maxRent <= 0 || newRent <= maxRent) {
					selectedClan.setRent(newRent);
					sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.setrent.success", selectedClan.getName(), selectedClan.getRent()).setStyle(TextStyles.GREEN));
				} else
					sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.setrent.overmax", selectedClan.getName(), ClansModContainer.getPaymentHandler().getFormattedCurrency(maxRent)).setStyle(TextStyles.RED));
			} else
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.setrent.negative").setStyle(TextStyles.RED));
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.not_on_server", "setrent", selectedClan.getName()).setStyle(TextStyles.RED));
	}
}
