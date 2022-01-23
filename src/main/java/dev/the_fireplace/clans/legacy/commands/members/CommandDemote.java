package dev.the_fireplace.clans.legacy.commands.members;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import dev.the_fireplace.clans.legacy.clan.membership.ClanMembers;
import dev.the_fireplace.clans.legacy.commands.ClanSubCommand;
import dev.the_fireplace.clans.legacy.logic.ClanMemberManagement;
import dev.the_fireplace.clans.legacy.model.EnumRank;
import dev.the_fireplace.clans.legacy.util.TextStyles;
import dev.the_fireplace.clans.legacy.util.translation.TranslationUtil;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandDemote extends ClanSubCommand
{
    @Override
    public String getName() {
        return "demote";
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
        if (ClanMembers.get(selectedClan).getMemberRanks().get(sender.getUniqueID()).equals(EnumRank.LEADER)) {
            ClanMemberManagement.demoteClanMember(server, sender, args[0], selectedClan);
        } else {
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.common.not_leader", selectedClanName).setStyle(TextStyles.RED));
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        ArrayList<String> playerNames = Lists.newArrayList();
        for (UUID player : ClanMembers.get(selectedClan).getMemberRanks().keySet()) {
            GameProfile playerProf = server.getPlayerProfileCache().getProfileByUUID(player);
            if (playerProf != null && !ClanMembers.get(selectedClan).getMemberRanks().get(player).equals(EnumRank.MEMBER)) {
                playerNames.add(playerProf.getName());
            }
        }
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, playerNames) : Collections.emptyList();
    }
}
