package the_fireplace.clans.legacy.logic;

import com.mojang.authlib.GameProfile;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.lang3.ArrayUtils;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.membership.ClanMemberMessager;
import the_fireplace.clans.clan.membership.ClanMembers;
import the_fireplace.clans.clan.membership.PlayerClans;
import the_fireplace.clans.legacy.model.EnumRank;
import the_fireplace.clans.legacy.util.TextStyles;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;
import the_fireplace.clans.player.PlayerClanSettings;

import java.util.Map;

public class ClanMemberManagement {
    public static void promoteClanMember(MinecraftServer server, ICommandSender sender, String playerName, Clan clan) throws CommandException {
        GameProfile target = server.getPlayerProfileCache().getGameProfileForUsername(playerName);

        if(target != null) {
            if (!PlayerClans.getClansPlayerIsIn(target.getId()).isEmpty()) {
                if (PlayerClans.getClansPlayerIsIn(target.getId()).contains(clan)) {
                    if (ClanMembers.get().promoteMember(target.getId())) {
                        sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.promote.success", target.getName(), ClanMembers.get().getMemberRanks().get(target.getId()).toString().toLowerCase(), clan.getClanMetadata().getClanName()).setStyle(TextStyles.GREEN));
                        for(Map.Entry<EntityPlayerMP, EnumRank> m : ClanMembers.get().getOnlineMemberRanks().entrySet())
                            if(m.getValue().greaterOrEquals(ClanMembers.get().getMemberRanks().get(target.getId())))
                                if(!m.getKey().getUniqueID().equals(target.getId()))
                                    m.getKey().sendMessage(TranslationUtil.getTranslation(m.getKey().getUniqueID(), "commands.clan.promote.notify", target.getName(), ClanMembers.get().getMemberRanks().get(target.getId()).toString().toLowerCase(), clan.getClanMetadata().getClanName(), sender.getDisplayName().getFormattedText()).setStyle(TextStyles.GREEN));
                        if(ArrayUtils.contains(server.getPlayerList().getOnlinePlayerProfiles(), target)) {
                            EntityPlayerMP targetPlayer = CommandBase.getPlayer(server, sender, target.getName());
                            targetPlayer.sendMessage(TranslationUtil.getTranslation(targetPlayer.getUniqueID(), "commands.clan.promote.promoted", clan.getClanMetadata().getClanName(), ClanMembers.get().getMemberRanks().get(target.getId()).toString().toLowerCase(), sender.getName()).setStyle(TextStyles.GREEN));
                        }
                    } else
                        sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.promote.error", target.getName()).setStyle(TextStyles.RED));
                } else
                    sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.player_not_in_clan", target.getName(), clan.getClanMetadata().getClanName()).setStyle(TextStyles.RED));
            } else
                sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.player_not_in_clan", target.getName(), clan.getClanMetadata().getClanName()).setStyle(TextStyles.RED));
        } else
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.playernotfound", playerName).setStyle(TextStyles.RED));
    }

    public static void demoteClanMember(MinecraftServer server, ICommandSender sender, String playerName, Clan clan) throws CommandException {
        GameProfile target = server.getPlayerProfileCache().getGameProfileForUsername(playerName);

        if(target != null) {
            if (!PlayerClans.getClansPlayerIsIn(target.getId()).isEmpty()) {
                if (PlayerClans.getClansPlayerIsIn(target.getId()).contains(clan)) {
                    if (ClanMembers.get().demoteMember(target.getId())) {
                        sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.demote.success", target.getName(), ClanMembers.get().getMemberRanks().get(target.getId()).toString().toLowerCase(), clan.getClanMetadata().getClanName()).setStyle(TextStyles.GREEN));
                        if(ArrayUtils.contains(server.getPlayerList().getOnlinePlayerProfiles(), target)) {
                            EntityPlayerMP targetPlayer = CommandBase.getPlayer(server, sender, target.getName());
                            targetPlayer.sendMessage(TranslationUtil.getTranslation(targetPlayer.getUniqueID(), "commands.clan.demote.demoted", clan.getClanMetadata().getClanName(), ClanMembers.get().getMemberRanks().get(target.getId()).toString().toLowerCase(), sender.getName()).setStyle(TextStyles.YELLOW));
                        }
                    } else
                        sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.demote.error", target.getName()).setStyle(TextStyles.RED));
                } else
                    sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.player_not_in_clan", target.getName(), clan.getClanMetadata().getClanName()).setStyle(TextStyles.RED));
            } else
                sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.player_not_in_clan", target.getName(), clan.getClanMetadata().getClanName()).setStyle(TextStyles.RED));
        } else
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.playernotfound", playerName).setStyle(TextStyles.RED));
    }

    public static void setRank(MinecraftServer server, ICommandSender sender, String playerName, Clan clan, EnumRank rank) throws CommandException {
        GameProfile target = server.getPlayerProfileCache().getGameProfileForUsername(playerName);

        if(target != null) {
            ClanMembers.get().addMember(target.getId(), rank);
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.opclan.setrank.success", target.getName(), rank.toString().toLowerCase(), clan.getClanMetadata().getClanName()).setStyle(TextStyles.GREEN));
            if(ArrayUtils.contains(server.getPlayerList().getOnlinePlayerProfiles(), target)) {
                EntityPlayerMP targetPlayer = CommandBase.getPlayer(server, sender, target.getName());
                if(targetPlayer != sender)
                    targetPlayer.sendMessage(TranslationUtil.getTranslation(targetPlayer.getUniqueID(), "commands.opclan.setrank.set", rank.toString().toLowerCase(), clan.getClanMetadata().getClanName(), sender.getName()).setStyle(TextStyles.YELLOW));
            }
        } else
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.playernotfound", playerName).setStyle(TextStyles.RED));
    }

    public static void kickMember(MinecraftServer server, ICommandSender sender, Clan selectedClan, GameProfile target) throws CommandException {
        if(ClanMembers.get().removeMember(target.getId())) {
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.kick.success", target.getName(), selectedClan.getClanMetadata().getClanName()).setStyle(TextStyles.GREEN));
            EntityPlayerMP excluded = sender instanceof EntityPlayerMP ? (EntityPlayerMP)sender : null;
            ClanMemberMessager.get().messageAllOnline(excluded, TextStyles.YELLOW, "commands.clan.kick.kicked_other", target.getName(), selectedClan.getClanMetadata().getClanName(), sender.getDisplayName().getFormattedText());
            if(ArrayUtils.contains(server.getPlayerList().getOnlinePlayerProfiles(), target)) {
                EntityPlayerMP targetPlayer = CommandBase.getPlayer(server, sender, target.getName());
                if(sender instanceof EntityPlayerMP && !((EntityPlayerMP) sender).getUniqueID().equals(target.getId()))
                    targetPlayer.sendMessage(TranslationUtil.getTranslation(targetPlayer.getUniqueID(), "commands.clan.kick.kicked", selectedClan.getClanMetadata().getClanName(), sender.getName()).setStyle(TextStyles.YELLOW));
                if(selectedClan.getClanMetadata().getClanId().equals(PlayerClanSettings.getDefaultClan(targetPlayer.getUniqueID())))
                    PlayerClanSettings.updateDefaultClanIfNeeded(targetPlayer.getUniqueID(), selectedClan.getClanMetadata().getClanId());
            }
        } else
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.kick.fail", target.getName(), selectedClan.getClanMetadata().getClanName()).setStyle(TextStyles.RED));
    }
}
