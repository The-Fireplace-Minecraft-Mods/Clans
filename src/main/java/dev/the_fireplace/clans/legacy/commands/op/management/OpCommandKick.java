package dev.the_fireplace.clans.legacy.commands.op.management;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import dev.the_fireplace.clans.legacy.clan.membership.ClanMembers;
import dev.the_fireplace.clans.legacy.clan.membership.PlayerClans;
import dev.the_fireplace.clans.legacy.clan.metadata.ClanNames;
import dev.the_fireplace.clans.legacy.commands.OpClanSubCommand;
import dev.the_fireplace.clans.legacy.logic.ClanMemberManagement;
import dev.the_fireplace.clans.legacy.model.EnumRank;
import dev.the_fireplace.clans.legacy.util.TextStyles;
import dev.the_fireplace.clans.legacy.util.translation.TranslationUtil;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OpCommandKick extends OpClanSubCommand
{
    @Override
    public String getName() {
        return "kick";
    }

    @Override
    public int getMinArgs() {
        return 2;
    }

    @Override
    public int getMaxArgs() {
        return 2;
    }

    @Override
    protected void runFromAnywhere(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        String clanName = args[0];
        String playerName = args[1];
        UUID clan = ClanNames.getClanByName(clanName);
        if (clan != null) {
            GameProfile target = server.getPlayerProfileCache().getGameProfileForUsername(playerName);

            if (target != null) {
                if (!PlayerClans.getClansPlayerIsIn(target.getId()).isEmpty()) {
                    if (PlayerClans.getClansPlayerIsIn(target.getId()).contains(clan)) {
                        ClanMemberManagement.kickMember(server, sender, clan, target);
                    } else {
                        sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.player_not_in_clan", target.getName(), ClanNames.get(clan).getName()).setStyle(TextStyles.RED));
                    }
                } else {
                    sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.player_not_in_clan", target.getName(), ClanNames.get(clan).getName()).setStyle(TextStyles.RED));
                }
            } else {
                sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.playernotfound", playerName).setStyle(TextStyles.RED));
            }
        } else {
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.notfound", clanName).setStyle(TextStyles.RED));
        }
    }

    @SuppressWarnings("Duplicates")
    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, ClanNames.getClanNames());
        }
        UUID targetClan = ClanNames.getClanByName(args[0]);
        if (targetClan != null && args.length == 2) {
            List<String> playerNames = Lists.newArrayList();
            for (UUID player : ClanMembers.get(targetClan).getMembers()) {
                GameProfile playerProf = server.getPlayerProfileCache().getProfileByUUID(player);
                if (playerProf != null && !ClanMembers.get(targetClan).getRank(player).equals(EnumRank.LEADER)) {
                    playerNames.add(playerProf.getName());
                }
            }
            return getListOfStringsMatchingLastWord(args, playerNames);
        }
        return Collections.emptyList();
    }
}
