package dev.the_fireplace.clans.legacy.logic;

import com.mojang.authlib.GameProfile;
import dev.the_fireplace.clans.legacy.clan.membership.ClanMemberMessager;
import dev.the_fireplace.clans.legacy.clan.membership.ClanMembers;
import dev.the_fireplace.clans.legacy.clan.membership.PlayerClans;
import dev.the_fireplace.clans.legacy.clan.metadata.ClanNames;
import dev.the_fireplace.clans.legacy.model.EnumRank;
import dev.the_fireplace.clans.legacy.player.PlayerClanSettings;
import dev.the_fireplace.clans.legacy.util.TextStyles;
import dev.the_fireplace.clans.legacy.util.translation.TranslationUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Map;
import java.util.UUID;

public class ClanMemberManagement
{
    public static void promoteClanMember(MinecraftServer server, ICommandSender sender, String playerName, UUID clan) throws CommandException {
        GameProfile target = server.getPlayerProfileCache().getGameProfileForUsername(playerName);

        if (target != null) {
            if (!PlayerClans.getClansPlayerIsIn(target.getId()).isEmpty()) {
                if (PlayerClans.getClansPlayerIsIn(target.getId()).contains(clan)) {
                    if (ClanMembers.get(clan).promoteMember(target.getId())) {
                        sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.promote.success", target.getName(), ClanMembers.get(clan).getMemberRanks().get(target.getId()).toString().toLowerCase(), ClanNames.get(clan).getName()).setStyle(TextStyles.GREEN));
                        for (Map.Entry<ServerPlayerEntity, EnumRank> m : ClanMembers.get(clan).getOnlineMemberRanks().entrySet()) {
                            if (m.getValue().greaterOrEquals(ClanMembers.get(clan).getMemberRanks().get(target.getId()))) {
                                if (!m.getKey().getUniqueID().equals(target.getId())) {
                                    m.getKey().sendMessage(TranslationUtil.getTranslation(m.getKey().getUniqueID(), "commands.clan.promote.notify", target.getName(), ClanMembers.get(clan).getMemberRanks().get(target.getId()).toString().toLowerCase(), ClanNames.get(clan).getName(), sender.getDisplayName().getFormattedText()).setStyle(TextStyles.GREEN));
                                }
                            }
                        }
                        if (ArrayUtils.contains(server.getPlayerList().getOnlinePlayerProfiles(), target)) {
                            ServerPlayerEntity targetPlayer = CommandBase.getPlayer(server, sender, target.getName());
                            targetPlayer.sendMessage(TranslationUtil.getTranslation(targetPlayer.getUniqueID(), "commands.clan.promote.promoted", ClanNames.get(clan).getName(), ClanMembers.get(clan).getMemberRanks().get(target.getId()).toString().toLowerCase(), sender.getName()).setStyle(TextStyles.GREEN));
                        }
                    } else {
                        sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.promote.error", target.getName()).setStyle(TextStyles.RED));
                    }
                } else {
                    sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.player_not_in_clan", target.getName(), ClanNames.get(clan).getName()).setStyle(TextStyles.RED));
                }
            } else {
                sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.player_not_in_clan", target.getName(), ClanNames.get(clan).getName()).setStyle(TextStyles.RED));
            }
        } else {
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.playernotfound", playerName).setStyle(TextStyles.RED));
        }
    }

    public static void demoteClanMember(MinecraftServer server, ICommandSender sender, String playerName, UUID clan) throws CommandException {
        GameProfile target = server.getPlayerProfileCache().getGameProfileForUsername(playerName);

        if (target != null) {
            if (!PlayerClans.getClansPlayerIsIn(target.getId()).isEmpty()) {
                if (PlayerClans.getClansPlayerIsIn(target.getId()).contains(clan)) {
                    if (ClanMembers.get(clan).demoteMember(target.getId())) {
                        sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.demote.success", target.getName(), ClanMembers.get(clan).getMemberRanks().get(target.getId()).toString().toLowerCase(), ClanNames.get(clan).getName()).setStyle(TextStyles.GREEN));
                        if (ArrayUtils.contains(server.getPlayerList().getOnlinePlayerProfiles(), target)) {
                            ServerPlayerEntity targetPlayer = CommandBase.getPlayer(server, sender, target.getName());
                            targetPlayer.sendMessage(TranslationUtil.getTranslation(targetPlayer.getUniqueID(), "commands.clan.demote.demoted", ClanNames.get(clan).getName(), ClanMembers.get(clan).getMemberRanks().get(target.getId()).toString().toLowerCase(), sender.getName()).setStyle(TextStyles.YELLOW));
                        }
                    } else {
                        sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.demote.error", target.getName()).setStyle(TextStyles.RED));
                    }
                } else {
                    sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.player_not_in_clan", target.getName(), ClanNames.get(clan).getName()).setStyle(TextStyles.RED));
                }
            } else {
                sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.player_not_in_clan", target.getName(), ClanNames.get(clan).getName()).setStyle(TextStyles.RED));
            }
        } else {
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.playernotfound", playerName).setStyle(TextStyles.RED));
        }
    }

    public static void setRank(MinecraftServer server, ICommandSender sender, String playerName, UUID clan, EnumRank rank) throws CommandException {
        GameProfile target = server.getPlayerProfileCache().getGameProfileForUsername(playerName);

        if (target != null) {
            ClanMembers.get(clan).addMember(target.getId(), rank);
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.opclan.setrank.success", target.getName(), rank.toString().toLowerCase(), ClanNames.get(clan).getName()).setStyle(TextStyles.GREEN));
            if (ArrayUtils.contains(server.getPlayerList().getOnlinePlayerProfiles(), target)) {
                ServerPlayerEntity targetPlayer = CommandBase.getPlayer(server, sender, target.getName());
                if (targetPlayer != sender) {
                    targetPlayer.sendMessage(TranslationUtil.getTranslation(targetPlayer.getUniqueID(), "commands.opclan.setrank.set", rank.toString().toLowerCase(), ClanNames.get(clan).getName(), sender.getName()).setStyle(TextStyles.YELLOW));
                }
            }
        } else {
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.playernotfound", playerName).setStyle(TextStyles.RED));
        }
    }

    public static void kickMember(MinecraftServer server, ICommandSender sender, UUID clan, GameProfile target) throws CommandException {
        String clanName = ClanNames.get(clan).getName();
        if (ClanMembers.get(clan).removeMember(target.getId())) {
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.kick.success", target.getName(), clanName).setStyle(TextStyles.GREEN));
            ServerPlayerEntity excluded = sender instanceof ServerPlayerEntity ? (ServerPlayerEntity) sender : null;
            ClanMemberMessager.get(clan).messageAllOnline(excluded, TextStyles.YELLOW, "commands.clan.kick.kicked_other", target.getName(), clanName, sender.getDisplayName().getFormattedText());
            if (ArrayUtils.contains(server.getPlayerList().getOnlinePlayerProfiles(), target)) {
                ServerPlayerEntity targetPlayer = CommandBase.getPlayer(server, sender, target.getName());
                if (sender instanceof ServerPlayerEntity && !((ServerPlayerEntity) sender).getUniqueID().equals(target.getId())) {
                    targetPlayer.sendMessage(TranslationUtil.getTranslation(targetPlayer.getUniqueID(), "commands.clan.kick.kicked", clanName, sender.getName()).setStyle(TextStyles.YELLOW));
                }
                if (clan.equals(PlayerClanSettings.getDefaultClan(targetPlayer.getUniqueID()))) {
                    PlayerClanSettings.updateDefaultClanIfNeeded(targetPlayer.getUniqueID(), clan);
                }
            }
        } else {
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.kick.fail", target.getName(), clanName).setStyle(TextStyles.RED));
        }
    }
}
