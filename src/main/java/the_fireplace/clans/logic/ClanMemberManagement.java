package the_fireplace.clans.logic;

import com.mojang.authlib.GameProfile;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.lang3.ArrayUtils;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.data.PlayerData;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import java.util.Map;

public class ClanMemberManagement {
    public static void promoteClanMember(MinecraftServer server, ICommandSender sender, String playerName, Clan clan) throws CommandException {
        GameProfile target = server.getPlayerProfileCache().getGameProfileForUsername(playerName);

        if(target != null) {
            if (!ClanCache.getPlayerClans(target.getId()).isEmpty()) {
                if (ClanCache.getPlayerClans(target.getId()).contains(clan)) {
                    if (clan.promoteMember(target.getId())) {
                        sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.promote.success", target.getName(), clan.getMembers().get(target.getId()).toString().toLowerCase(), clan.getName()).setStyle(TextStyles.GREEN));
                        for(Map.Entry<EntityPlayerMP, EnumRank> m : clan.getOnlineMembers().entrySet())
                            if(m.getValue().greaterOrEquals(clan.getMembers().get(target.getId())))
                                if(!m.getKey().getUniqueID().equals(target.getId()))
                                    m.getKey().sendMessage(TranslationUtil.getTranslation(m.getKey().getUniqueID(), "commands.clan.promote.notify", target.getName(), clan.getMembers().get(target.getId()).toString().toLowerCase(), clan.getName(), sender.getDisplayName().getFormattedText()).setStyle(TextStyles.GREEN));
                        if(ArrayUtils.contains(server.getPlayerList().getOnlinePlayerProfiles(), target)) {
                            EntityPlayerMP targetPlayer = CommandBase.getPlayer(server, sender, target.getName());
                            targetPlayer.sendMessage(TranslationUtil.getTranslation(targetPlayer.getUniqueID(), "commands.clan.promote.promoted", clan.getName(), clan.getMembers().get(target.getId()).toString().toLowerCase(), sender.getName()).setStyle(TextStyles.GREEN));
                        }
                    } else
                        sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.promote.error", target.getName()).setStyle(TextStyles.RED));
                } else
                    sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.player_not_in_clan", target.getName(), clan.getName()).setStyle(TextStyles.RED));
            } else
                sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.player_not_in_clan", target.getName(), clan.getName()).setStyle(TextStyles.RED));
        } else
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.playernotfound", playerName).setStyle(TextStyles.RED));
    }

    public static void demoteClanMember(MinecraftServer server, ICommandSender sender, String playerName, Clan clan) throws CommandException {
        GameProfile target = server.getPlayerProfileCache().getGameProfileForUsername(playerName);

        if(target != null) {
            if (!ClanCache.getPlayerClans(target.getId()).isEmpty()) {
                if (ClanCache.getPlayerClans(target.getId()).contains(clan)) {
                    if (clan.demoteMember(target.getId())) {
                        sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.demote.success", target.getName(), clan.getMembers().get(target.getId()).toString().toLowerCase(), clan.getName()).setStyle(TextStyles.GREEN));
                        if(ArrayUtils.contains(server.getPlayerList().getOnlinePlayerProfiles(), target)) {
                            EntityPlayerMP targetPlayer = CommandBase.getPlayer(server, sender, target.getName());
                            targetPlayer.sendMessage(TranslationUtil.getTranslation(targetPlayer.getUniqueID(), "commands.clan.demote.demoted", clan.getName(), clan.getMembers().get(target.getId()).toString().toLowerCase(), sender.getName()).setStyle(TextStyles.YELLOW));
                        }
                    } else
                        sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.demote.error", target.getName()).setStyle(TextStyles.RED));
                } else
                    sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.player_not_in_clan", target.getName(), clan.getName()).setStyle(TextStyles.RED));
            } else
                sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.player_not_in_clan", target.getName(), clan.getName()).setStyle(TextStyles.RED));
        } else
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.playernotfound", playerName).setStyle(TextStyles.RED));
    }

    public static void setRank(MinecraftServer server, ICommandSender sender, String playerName, Clan clan, EnumRank rank) throws CommandException {
        GameProfile target = server.getPlayerProfileCache().getGameProfileForUsername(playerName);

        if(target != null) {
            clan.addMember(target.getId(), rank);
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.opclan.setrank.success", target.getName(), rank.toString().toLowerCase(), clan.getName()).setStyle(TextStyles.GREEN));
            if(ArrayUtils.contains(server.getPlayerList().getOnlinePlayerProfiles(), target)) {
                EntityPlayerMP targetPlayer = CommandBase.getPlayer(server, sender, target.getName());
                if(targetPlayer != sender)
                    targetPlayer.sendMessage(TranslationUtil.getTranslation(targetPlayer.getUniqueID(), "commands.opclan.setrank.set", rank.toString().toLowerCase(), clan.getName(), sender.getName()).setStyle(TextStyles.YELLOW));
            }
        } else
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.playernotfound", playerName).setStyle(TextStyles.RED));
    }

    public static void kickMember(MinecraftServer server, ICommandSender sender, Clan selectedClan, GameProfile target) throws CommandException {
        if(selectedClan.removeMember(target.getId())) {
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.kick.success", target.getName(), selectedClan.getName()).setStyle(TextStyles.GREEN));
            selectedClan.messageAllOnline(sender instanceof EntityPlayerMP ? (EntityPlayerMP)sender : null, TextStyles.YELLOW, "commands.clan.kick.kicked_other", target.getName(), selectedClan.getName(), sender.getDisplayName().getFormattedText());
            if(ArrayUtils.contains(server.getPlayerList().getOnlinePlayerProfiles(), target)) {
                EntityPlayerMP targetPlayer = CommandBase.getPlayer(server, sender, target.getName());
                if(sender instanceof EntityPlayerMP && !((EntityPlayerMP) sender).getUniqueID().equals(target.getId()))
                    targetPlayer.sendMessage(TranslationUtil.getTranslation(targetPlayer.getUniqueID(), "commands.clan.kick.kicked", selectedClan.getName(), sender.getName()).setStyle(TextStyles.YELLOW));
                if(selectedClan.getId().equals(PlayerData.getDefaultClan(targetPlayer.getUniqueID())))
                    PlayerData.updateDefaultClan(targetPlayer.getUniqueID(), selectedClan.getId());
            }
        } else
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.kick.fail", target.getName(), selectedClan.getName()).setStyle(TextStyles.RED));
    }
}
