package the_fireplace.clans.legacy.commands.details;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.clan.ClanDisbander;
import the_fireplace.clans.clan.admin.AdminControlledClanSettings;
import the_fireplace.clans.clan.membership.ClanMembers;
import the_fireplace.clans.economy.Economy;
import the_fireplace.clans.legacy.ClansModContainer;
import the_fireplace.clans.legacy.commands.ClanSubCommand;
import the_fireplace.clans.legacy.model.EnumRank;
import the_fireplace.clans.legacy.util.TextStyles;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandDisband extends ClanSubCommand
{
    @Override
    public String getName() {
        return "disband";
    }

    @Override
    public EnumRank getRequiredClanRank() {
        return EnumRank.LEADER;
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
        if (ClanMembers.get(selectedClan).getMemberRanks().get(sender.getUniqueID()).equals(EnumRank.LEADER)) {
            if (!AdminControlledClanSettings.get(selectedClan).isServerOwned()) {
                ClanDisbander disbander = ClanDisbander.create(selectedClan);
                if (ClansModContainer.getConfig().getDisbandFeeFormula().isEmpty() || Economy.deductAmount(disbander.getDisbandCost(), selectedClan)) {
                    disbander.disband(sender, "commands.clan.disband.disbanded", selectedClanName, sender.getName());
                    sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.disband.success", selectedClanName).setStyle(TextStyles.GREEN));
                } else {
                    sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.disband.insufficient_funds", selectedClanName, Economy.getFormattedCurrency(disbander.getDisbandCost())).setStyle(TextStyles.RED));
                }
            } else {
                sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.disband.server", selectedClanName).setStyle(TextStyles.RED));
            }
        } else {
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.common.not_leader", selectedClanName).setStyle(TextStyles.RED));
        }
    }
}
