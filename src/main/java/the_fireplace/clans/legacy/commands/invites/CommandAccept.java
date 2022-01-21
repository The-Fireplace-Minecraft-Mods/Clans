package the_fireplace.clans.legacy.commands.invites;

import com.google.common.collect.Lists;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import the_fireplace.clans.clan.membership.ClanMemberMessager;
import the_fireplace.clans.clan.membership.ClanMembers;
import the_fireplace.clans.clan.membership.PlayerClans;
import the_fireplace.clans.clan.metadata.ClanNames;
import the_fireplace.clans.legacy.ClansModContainer;
import the_fireplace.clans.legacy.commands.ClanSubCommand;
import the_fireplace.clans.legacy.model.EnumRank;
import the_fireplace.clans.legacy.util.TextStyles;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;
import the_fireplace.clans.player.InvitedPlayers;
import the_fireplace.clans.player.PlayerClanSettings;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandAccept extends ClanSubCommand
{
    @Override
    public String getName() {
        return "accept";
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
    public void run(MinecraftServer server, EntityPlayerMP sender, String[] args) {
        UUID acceptClan = ClanNames.getClanByName(args[0]);
        if (acceptClan != null) {
            if (InvitedPlayers.getReceivedInvites(sender.getUniqueID()).contains(acceptClan)) {
                ClanMembers.get(acceptClan).addMember(sender.getUniqueID());
                if (PlayerClans.getClansPlayerIsIn(sender.getUniqueID()).size() == 1) {
                    PlayerClanSettings.setDefaultClan(sender.getUniqueID(), acceptClan);
                }
                InvitedPlayers.removeInvite(sender.getUniqueID(), acceptClan);
                sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.accept.success", ClanNames.get(acceptClan).getName()).setStyle(TextStyles.GREEN));
                ClanMemberMessager.get(acceptClan).messageAllOnline(sender, TextStyles.GREEN, "commands.clan.accept.accepted", sender.getDisplayNameString(), ClanNames.get(acceptClan).getName());
            } else {
                sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.invite.not_invited", args[0]).setStyle(TextStyles.RED));
            }
        } else {
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.common.notfound", args[0]).setStyle(TextStyles.RED));
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1 && sender instanceof EntityPlayerMP) {
            List<String> clanNames = Lists.newArrayList();
            for (UUID inviteClan : InvitedPlayers.getReceivedInvites(((EntityPlayerMP) sender).getUniqueID())) {
                clanNames.add(ClanNames.get(inviteClan).getName());
            }
            return getListOfStringsMatchingLastWord(args, clanNames);
        }
        return Collections.emptyList();
    }
}
