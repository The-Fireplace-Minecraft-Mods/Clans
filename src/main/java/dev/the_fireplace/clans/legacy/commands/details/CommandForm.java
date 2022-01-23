package dev.the_fireplace.clans.legacy.commands.details;

import dev.the_fireplace.clans.economy.Economy;
import dev.the_fireplace.clans.legacy.ClansModContainer;
import dev.the_fireplace.clans.legacy.clan.ClanCreator;
import dev.the_fireplace.clans.legacy.clan.membership.PlayerClans;
import dev.the_fireplace.clans.legacy.clan.metadata.ClanNames;
import dev.the_fireplace.clans.legacy.commands.ClanSubCommand;
import dev.the_fireplace.clans.legacy.config.Config;
import dev.the_fireplace.clans.legacy.model.EnumRank;
import dev.the_fireplace.clans.legacy.player.PlayerClanSettings;
import dev.the_fireplace.clans.legacy.util.TextStyles;
import dev.the_fireplace.clans.legacy.util.translation.TranslationUtil;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandForm extends ClanSubCommand
{
    @Override
    public String getName() {
        return "form";
    }

    @Override
    public EnumRank getRequiredClanRank() {
        return ClansModContainer.getConfig().isAllowMultiClanMembership() ? EnumRank.ANY : EnumRank.NOCLAN;
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
    public void run(MinecraftServer server, ServerPlayerEntity sender, String[] args) {
        if (selectedClan == null || ClansModContainer.getConfig().isAllowMultiClanMembership()) {
            String newClanName = TextStyles.stripFormatting(args[0]);
            if (Config.getInstance().chatCensor.censorClanNames) {
                newClanName = ClansModContainer.getChatCensorCompat().getCensoredString(newClanName);
            }
            if (ClansModContainer.getConfig().getMaxNameLength() > 0 && newClanName.length() > ClansModContainer.getConfig().getMaxNameLength()) {
                sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.setname.toolong", newClanName, ClansModContainer.getConfig().getMaxNameLength()).setStyle(TextStyles.RED));
            } else if (!ClanNames.isClanNameAvailable(newClanName)) {
                sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.setname.taken", newClanName).setStyle(TextStyles.RED));
            } else {
                if (Economy.deductAmount(ClansModContainer.getConfig().getFormClanCost(), sender.getUniqueID())) {
                    UUID c = ClanCreator.createStandardClan(newClanName, sender.getUniqueID());
                    if (PlayerClans.countClansPlayerIsIn(sender.getUniqueID()) == 1) {
                        PlayerClanSettings.setDefaultClan(sender.getUniqueID(), c);
                    }
                    sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.form.success").setStyle(TextStyles.GREEN));
                } else {
                    sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.form.insufficient_funds", Economy.getFormattedCurrency(ClansModContainer.getConfig().getFormClanCost())).setStyle(TextStyles.RED));
                }
            }
        } else {
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.form.already_in_clan").setStyle(TextStyles.RED));
        }
    }
}
