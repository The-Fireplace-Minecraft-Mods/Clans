package the_fireplace.clans.legacy.commands.members;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.clan.admin.AdminControlledClanSettings;
import the_fireplace.clans.clan.membership.ClanMemberMessager;
import the_fireplace.clans.clan.membership.ClanMembers;
import the_fireplace.clans.legacy.commands.ClanSubCommand;
import the_fireplace.clans.legacy.model.EnumRank;
import the_fireplace.clans.legacy.util.TextStyles;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;
import the_fireplace.clans.player.PlayerClanSettings;

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
    public void run(MinecraftServer server, EntityPlayerMP sender, String[] args) {
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

    private boolean unableToLeave(EntityPlayerMP sender, EnumRank senderRank) {
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
