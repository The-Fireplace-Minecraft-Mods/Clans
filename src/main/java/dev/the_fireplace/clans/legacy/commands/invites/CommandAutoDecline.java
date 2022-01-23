package dev.the_fireplace.clans.legacy.commands.invites;

import dev.the_fireplace.clans.legacy.ClansModContainer;
import dev.the_fireplace.clans.legacy.clan.metadata.ClanNames;
import dev.the_fireplace.clans.legacy.commands.ClanSubCommand;
import dev.the_fireplace.clans.legacy.model.EnumRank;
import dev.the_fireplace.clans.legacy.player.InvitedPlayers;
import dev.the_fireplace.clans.legacy.util.TextStyles;
import dev.the_fireplace.clans.legacy.util.translation.TranslationUtil;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandAutoDecline extends ClanSubCommand
{
    @Override
    public String getName() {
        return "autodecline";
    }

    @Override
    public EnumRank getRequiredClanRank() {
        return ClansModContainer.getConfig().isAllowMultiClanMembership() ? EnumRank.ANY : EnumRank.NOCLAN;
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
    public void run(MinecraftServer server, ServerPlayerEntity sender, String[] args) {
        if (args.length == 0) {
            boolean willNowBlockAll = !InvitedPlayers.isBlockingAllInvites(sender.getUniqueID());
            InvitedPlayers.setGlobalInviteBlock(sender.getUniqueID(), willNowBlockAll);
            if (willNowBlockAll) {
                sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.autodecline.on").setStyle(TextStyles.GREEN));
            } else {
                sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.autodecline.off").setStyle(TextStyles.GREEN));
            }
        } else {
            UUID c = ClanNames.getClanByName(args[0]);
            if (c != null) {
                toggleClanInviteBlock(sender, c);
            } else {
                sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.common.notfound", args[0]).setStyle(TextStyles.RED));
            }
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, ClanNames.getClanNames()) : Collections.emptyList();
    }

    public static void toggleClanInviteBlock(ServerPlayerEntity sender, UUID clanId) {
        boolean willNowBlock = !InvitedPlayers.getBlockedClans(sender.getUniqueID()).contains(clanId);
        if (willNowBlock) {
            InvitedPlayers.addInviteBlock(sender.getUniqueID(), clanId);
            InvitedPlayers.removeInvite(sender.getUniqueID(), clanId);
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.autodecline.on_clan", ClanNames.get(clanId).getName()).setStyle(TextStyles.GREEN));
        } else {
            InvitedPlayers.removeInviteBlock(sender.getUniqueID(), clanId);
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.autodecline.off_clan", ClanNames.get(clanId).getName()).setStyle(TextStyles.GREEN));
        }
    }
}
