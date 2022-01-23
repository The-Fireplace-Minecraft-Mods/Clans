package dev.the_fireplace.clans.legacy.commands.members;

import dev.the_fireplace.clans.legacy.clan.admin.AdminControlledClanSettings;
import dev.the_fireplace.clans.legacy.clan.membership.ClanMemberMessager;
import dev.the_fireplace.clans.legacy.clan.membership.ClanMembers;
import dev.the_fireplace.clans.legacy.commands.ClanSubCommand;
import dev.the_fireplace.clans.legacy.model.EnumRank;
import dev.the_fireplace.clans.legacy.player.PlayerClanSettings;
import dev.the_fireplace.clans.legacy.util.TextStyles;
import dev.the_fireplace.clans.legacy.util.translation.TranslationUtil;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandLeave extends ClanSubCommand
{
    @Override
    public String getName() {
        return "leave";
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
        EnumRank senderRank = ClanMembers.get(selectedClan).getRank(sender.getUniqueID());
        if (unableToLeave(sender, senderRank)) {
            return;
        }
        if (ClanMembers.get(selectedClan).removeMember(sender.getUniqueID())) {
            PlayerClanSettings.updateDefaultClanIfNeeded(sender.getUniqueID(), selectedClan);
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.leave.success", selectedClanName).setStyle(TextStyles.GREEN));
            ClanMemberMessager.get(selectedClan).messageAllOnline(TextStyles.YELLOW, "commands.clan.leave.left", sender.getDisplayNameString(), selectedClanName);
        }
    }

    private boolean unableToLeave(ServerPlayerEntity sender, EnumRank senderRank) {
        if (senderRank == EnumRank.LEADER && !AdminControlledClanSettings.get(selectedClan).isServerOwned()) {
            if (ClanMembers.get(selectedClan).getMemberCount() == 1) {
                sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.leave.disband", selectedClanName).setStyle(TextStyles.RED));
                return true;
            }
            if (ClanMembers.get(selectedClan).getLeaderCount() <= 1) {
                sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.leave.promote", selectedClanName).setStyle(TextStyles.RED));
                return true;
            }
        }
        return false;
    }
}
