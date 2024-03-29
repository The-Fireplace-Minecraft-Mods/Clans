package the_fireplace.clans.legacy.commands.invites;

import com.google.common.collect.Lists;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import the_fireplace.clans.clan.membership.ClanMemberMessager;
import the_fireplace.clans.clan.metadata.ClanNames;
import the_fireplace.clans.legacy.ClansModContainer;
import the_fireplace.clans.legacy.commands.ClanSubCommand;
import the_fireplace.clans.legacy.model.EnumRank;
import the_fireplace.clans.legacy.util.TextStyles;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;
import the_fireplace.clans.player.InvitedPlayers;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandDecline extends ClanSubCommand
{
    @Override
    public String getName() {
        return "decline";
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
        return 2;
    }

    @Override
    public void run(MinecraftServer server, EntityPlayerMP sender, String[] args) {
        UUID declineClan = ClanNames.getClanByName(args[0]);
        if (declineClan != null) {
            if (InvitedPlayers.getReceivedInvites(sender.getUniqueID()).contains(declineClan)) {
                InvitedPlayers.removeInvite(sender.getUniqueID(), declineClan);
                sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.decline.success", ClanNames.get(declineClan).getName()).setStyle(TextStyles.GREEN));
                ClanMemberMessager.get(declineClan).messageAllOnline(EnumRank.ADMIN, TextStyles.YELLOW, "commands.clan.decline.declined", sender.getDisplayNameString(), ClanNames.get(declineClan).getName());
            } else if (args.length < 2) {
                sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.invite.not_invited", args[0]).setStyle(TextStyles.RED));
            } else// if(args[1].equalsIgnoreCase("block"))//TODO add error message if they put an invalid argument, instead of accepting anything
            {
                CommandAutoDecline.toggleClanInviteBlock(sender, declineClan);
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
        } else if (args.length == 2) {
            return Collections.singletonList("block");
        }
        return Collections.emptyList();
    }
}
