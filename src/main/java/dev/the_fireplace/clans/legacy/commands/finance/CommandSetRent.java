package dev.the_fireplace.clans.legacy.commands.finance;

import dev.the_fireplace.clans.economy.Economy;
import dev.the_fireplace.clans.legacy.ClansModContainer;
import dev.the_fireplace.clans.legacy.clan.admin.AdminControlledClanSettings;
import dev.the_fireplace.clans.legacy.clan.economics.ClanRent;
import dev.the_fireplace.clans.legacy.commands.ClanSubCommand;
import dev.the_fireplace.clans.legacy.model.EnumRank;
import dev.the_fireplace.clans.legacy.util.FormulaParser;
import dev.the_fireplace.clans.legacy.util.TextStyles;
import dev.the_fireplace.clans.legacy.util.translation.TranslationUtil;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandSetRent extends ClanSubCommand
{
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
    public void run(MinecraftServer server, ServerPlayerEntity sender, String[] args) throws CommandException {
        if (ClansModContainer.getConfig().getChargeRentDays() <= 0) {
            throw new CommandException(TranslationUtil.getRawTranslationString(sender, "commands.clan.setrent.disabled"));
        }
        if (!AdminControlledClanSettings.get(selectedClan).isServerOwned()) {
            double newRent = parseDouble(args[0]);
            if (newRent >= 0) {
                double maxRent = FormulaParser.eval(ClansModContainer.getConfig().getMaxRentFormula(), selectedClan, 0);
                if (maxRent <= 0 || newRent <= maxRent) {
                    ClanRent.get(selectedClan).setRent(newRent);
                    sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.setrent.success", selectedClanName, ClanRent.get(selectedClan).getRent()).setStyle(TextStyles.GREEN));
                } else {
                    sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.setrent.overmax", selectedClanName, Economy.getFormattedCurrency(maxRent)).setStyle(TextStyles.RED));
                }
            } else {
                sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.setrent.negative").setStyle(TextStyles.RED));
            }
        } else {
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.not_on_server", "setrent", selectedClanName).setStyle(TextStyles.RED));
        }
    }
}
