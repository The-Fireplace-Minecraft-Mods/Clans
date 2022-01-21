package the_fireplace.clans.legacy.commands.details;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.ArrayUtils;
import the_fireplace.clans.clan.membership.ClanMembers;
import the_fireplace.clans.clan.membership.PlayerClans;
import the_fireplace.clans.clan.metadata.ClanNames;
import the_fireplace.clans.legacy.commands.ClanSubCommand;
import the_fireplace.clans.legacy.model.EnumRank;
import the_fireplace.clans.legacy.util.TextStyles;
import the_fireplace.clans.legacy.util.TimeUtils;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;
import the_fireplace.clans.player.PlayerClanSettings;
import the_fireplace.clans.player.PlayerLastSeenData;
import the_fireplace.clans.player.PlayerRaidStats;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandPlayerInfo extends ClanSubCommand
{
    @Override
    public String getName() {
        return "playerinfo";
    }

    @Override
    public EnumRank getRequiredClanRank() {
        return EnumRank.ANY;
    }

    @Override
    public int getMinArgs() {
        return 0;
    }

    @Override
    public int getMaxArgs() {
        return 1;
    }

    @Override
    protected void runFromAnywhere(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            if (sender instanceof EntityPlayerMP) {
                showPlayerClanDetails(server, sender, ((EntityPlayerMP) sender).getGameProfile());
            } else {
                sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.playerinfo.console", TranslationUtil.getStringTranslation(getUsage(sender))).setStyle(TextStyles.RED));
            }
        } else {
            GameProfile targetPlayer = parsePlayerName(server, args[0]);
            showPlayerClanDetails(server, sender, targetPlayer);
        }
    }

    @Override
    protected boolean allowConsoleUsage() {
        return true;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, server.getPlayerProfileCache().getUsernames()) : Collections.emptyList();
    }

    private void showPlayerClanDetails(MinecraftServer server, ICommandSender sender, GameProfile target) {
        sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.playerinfo.name", target.getName()).setStyle(TextStyles.GREEN));
        sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.playerinfo.wlr", PlayerRaidStats.getRaidWLR(target.getId())).setStyle(TextStyles.GREEN));
        sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.playerinfo.last_seen", TimeUtils.getFormattedTime(PlayerLastSeenData.getLastSeen(target.getId()))).setStyle(TextStyles.GREEN));
        List<UUID> leaderInClans = Lists.newArrayList();
        List<UUID> adminInClans = Lists.newArrayList();
        List<UUID> memberInClans = Lists.newArrayList();
        for (UUID clan : PlayerClans.getClansPlayerIsIn(target.getId())) {
            EnumRank rank = ClanMembers.get(clan).getRank(target.getId());
            switch (rank) {
                case LEADER:
                    leaderInClans.add(clan);
                    break;
                case ADMIN:
                    adminInClans.add(clan);
                    break;
                case MEMBER:
                    memberInClans.add(clan);
                    break;
            }
        }
        if (!leaderInClans.isEmpty() || !adminInClans.isEmpty() || !memberInClans.isEmpty()) {
            UUID defaultClan = null;
            if (ArrayUtils.contains(server.getOnlinePlayerProfiles(), target)) {
                defaultClan = PlayerClanSettings.getDefaultClan(target.getId());
            }
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.playerinfo.clans").setStyle(TextStyles.GREEN));
            for (UUID leader : leaderInClans) {
                sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.playerinfo.leader", ClanNames.get(leader).getName()).setStyle(leader.equals(defaultClan) ? TextStyles.ONLINE_ADMIN : TextStyles.GREEN));
            }
            for (UUID admin : adminInClans) {
                sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.playerinfo.admin", ClanNames.get(admin).getName()).setStyle(admin.equals(defaultClan) ? TextStyles.ONLINE_ADMIN : TextStyles.GREEN));
            }
            for (UUID member : memberInClans) {
                sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.playerinfo.member", ClanNames.get(member).getName()).setStyle(member.equals(defaultClan) ? TextStyles.ONLINE_ADMIN : TextStyles.GREEN));
            }
        } else {
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.playerinfo.noclans", target.getName()).setStyle(TextStyles.GREEN));
        }
    }
}
