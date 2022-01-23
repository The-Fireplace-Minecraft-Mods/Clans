package dev.the_fireplace.clans.legacy.commands.finance;

import dev.the_fireplace.clans.economy.Economy;
import dev.the_fireplace.clans.legacy.clan.admin.AdminControlledClanSettings;
import dev.the_fireplace.clans.legacy.commands.ClanSubCommand;
import dev.the_fireplace.clans.legacy.model.EnumRank;
import dev.the_fireplace.clans.legacy.util.TextStyles;
import dev.the_fireplace.clans.legacy.util.translation.TranslationUtil;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandBalance extends ClanSubCommand
{
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
    public void run(MinecraftServer server, ServerPlayerEntity sender, String[] args) {
        if (!AdminControlledClanSettings.get(selectedClan).isServerOwned()) {
            double balance = Economy.getBalance(selectedClan);
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.balance.balance", selectedClanName, Economy.getFormattedCurrency(balance)).setStyle(TextStyles.GREEN));
        } else {
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.not_on_server", "balance", selectedClanName).setStyle(TextStyles.RED));
        }
    }
}
