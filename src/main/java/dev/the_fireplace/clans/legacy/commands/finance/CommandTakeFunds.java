package dev.the_fireplace.clans.legacy.commands.finance;

import dev.the_fireplace.clans.economy.Economy;
import dev.the_fireplace.clans.legacy.ClansModContainer;
import dev.the_fireplace.clans.legacy.clan.admin.AdminControlledClanSettings;
import dev.the_fireplace.clans.legacy.clan.membership.ClanMemberMessager;
import dev.the_fireplace.clans.legacy.commands.ClanSubCommand;
import dev.the_fireplace.clans.legacy.model.EnumRank;
import dev.the_fireplace.clans.legacy.util.TextStyles;
import dev.the_fireplace.clans.legacy.util.translation.TranslationUtil;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandTakeFunds extends ClanSubCommand
{
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
    public void run(MinecraftServer server, ServerPlayerEntity sender, String[] args) throws CommandException {
        if (!ClansModContainer.getConfig().isLeaderWithdrawFunds()) {
            throw new CommandException(TranslationUtil.getRawTranslationString(sender.getUniqueID(), "commands.clan.takefunds.disabled"));
        }
        if (!AdminControlledClanSettings.get(selectedClan).isServerOwned()) {
            double amount = parseDouble(args[0]);
            if (Economy.deductAmount(amount, selectedClan)) {
                if (Economy.addAmount(amount, sender.getUniqueID())) {
                    sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.takefunds.success", Economy.getFormattedCurrency(amount), selectedClanName).setStyle(TextStyles.GREEN));
                    ClanMemberMessager.get(selectedClan).messageAllOnline(sender, TextStyles.GREEN, "commands.clan.takefunds.taken", sender.getDisplayNameString(), Economy.getFormattedCurrency(amount), selectedClanName);
                } else {
                    Economy.addAmount(amount, selectedClan);
                    sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "clans.error.no_player_econ_acct").setStyle(TextStyles.RED));
                }
            } else {
                sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.common.insufficient_clan_funds", selectedClanName).setStyle(TextStyles.RED));
            }
        } else {
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.not_on_server", "takefunds", selectedClanName).setStyle(TextStyles.RED));
        }
    }
}
