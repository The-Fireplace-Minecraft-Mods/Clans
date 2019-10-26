package the_fireplace.clans.logic;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import the_fireplace.clans.Clans;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.cache.PlayerCache;
import the_fireplace.clans.cache.RaidingParties;
import the_fireplace.clans.commands.teleportation.CommandHome;
import the_fireplace.clans.data.ClaimData;
import the_fireplace.clans.data.PlayerData;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import java.util.UUID;

public class PlayerEventLogic {
    public static void onPlayerLoggedIn(EntityPlayer player) {
        checkUpdateDefaultClan(player);
        PlayerData.setShouldDisposeReferences(player.getUniqueID(), false);
        PlayerCache.setNeedsCleanup(player.getUniqueID(), false);
    }

    public static void onFirstLogin(UUID playerId) {
        Clan serverDefault = ClanCache.getClanByName(Clans.getConfig().getServerDefaultClan());
        if(serverDefault != null)
            serverDefault.addMember(playerId);
    }

    public static void checkUpdateDefaultClan(EntityPlayer player) {
        if(!player.world.isRemote) {
            UUID defaultClan = PlayerData.getDefaultClan(player.getUniqueID());
            if ((defaultClan != null && ClanCache.getClanById(defaultClan) == null) || (defaultClan == null && !ClanCache.getPlayerClans(player.getUniqueID()).isEmpty()) || (defaultClan != null && !ClanCache.getPlayerClans(player.getUniqueID()).contains(ClanCache.getClanById(defaultClan))))
                PlayerData.updateDefaultClan(player.getUniqueID(), null);
        }
    }

    public static void onPlayerLoggedOut(UUID playerId) {
        ClanCache.opAutoClaimLands.remove(playerId);
        ClanCache.opAutoAbandonClaims.remove(playerId);
        ClanCache.autoAbandonClaims.remove(playerId);
        ClanCache.autoClaimLands.remove(playerId);
        ClanCache.clanChattingPlayers.remove(playerId);
        RaidingParties.playerLoggedOut(playerId);
        PlayerData.setShouldDisposeReferences(playerId, true);
        PlayerCache.setNeedsCleanup(playerId, true);
    }

    public static void onPlayerChangedDimension(EntityPlayer player) {
        if(!player.world.isRemote) {
            PlayerCache.setClaimWarning(player.getUniqueID(), false);
            Clan ocAutoClaim = ClanCache.opAutoClaimLands.remove(player.getUniqueID());
            boolean ocAutoAbandon = ClanCache.opAutoAbandonClaims.remove(player.getUniqueID());
            Clan cAutoAbandon = ClanCache.autoAbandonClaims.remove(player.getUniqueID());
            Clan cAutoClaim = ClanCache.autoClaimLands.remove(player.getUniqueID());
            if (ocAutoAbandon)
                player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "commands.opclan.autoabandon.stop").setStyle(TextStyles.GREEN));
            if (cAutoAbandon != null)
                player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "commands.clan.autoabandon.stop", cAutoAbandon.getName()).setStyle(TextStyles.GREEN));
            if (ocAutoClaim != null)
                player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "commands.clan.autoclaim.stop", ocAutoClaim.getName()).setStyle(TextStyles.GREEN));
            if (cAutoClaim != null)
                player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "commands.clan.autoclaim.stop", cAutoClaim.getName()).setStyle(TextStyles.GREEN));
        }
    }

    public static ITextComponent getPlayerChatMessage(EntityPlayer player, ITextComponent initialMessage) {
        if(!player.world.isRemote) {
            UUID defaultClanId = PlayerData.getDefaultClan(player.getUniqueID());
            if (defaultClanId != null) {
                Clan playerDefaultClan = ClanCache.getClanById(defaultClanId);
                if (playerDefaultClan != null)
                    return TranslationUtil.getTranslation(Clans.getConfig().getDefaultClanPrefix(), playerDefaultClan.getName()).setStyle(new Style().setColor(playerDefaultClan.getTextColor())).appendSibling(initialMessage.setStyle(TextStyles.RESET));
                else
                    PlayerData.updateDefaultClan(player.getUniqueID(), null);
            }
        }
        return initialMessage;
    }

    public static void onPlayerRespawn(EntityPlayer player) {
        if(!player.world.isRemote) {
            Clan defClan = ClanCache.getClanById(PlayerData.getDefaultClan(player.getUniqueID()));
            if (defClan != null && defClan.hasHome() && defClan.getHome() != null)
                CommandHome.teleportHome(player, defClan, defClan.getHome(), player.dimension, true);
        }
    }

    public static void sendClanChat(EntityPlayer player, ITextComponent message) {
        if(!player.world.isRemote) {
            Clan clanChat = ClanCache.clanChattingPlayers.get(player.getUniqueID());
            for (EntityPlayerMP member : clanChat.getOnlineMembers().keySet())
                member.sendMessage(TranslationUtil.getTranslation(member.getUniqueID(), "clans.chat.prefix", clanChat.getName()).setStyle(new Style().setColor(clanChat.getTextColor())).appendSibling(message));
        }
    }

    public static float breakSpeed(EntityPlayer player, float oldSpeed) {//TODO should borderlands be impacted by this?
        if(!player.world.isRemote && RaidingParties.isRaidedBy(ClaimData.getChunkClan(player.chunkCoordX, player.chunkCoordZ, player.dimension), player)) {
            return oldSpeed * (float)Clans.getConfig().getRaidBreakSpeedMultiplier();
        }
        return oldSpeed;
    }
    
    public static void onPlayerDamage(EntityPlayer player) {
        //noinspection SuspiciousMethodCalls
        if(!player.world.isRemote && PlayerCache.clanHomeWarmups.remove(player) != null)
            player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "commands.clan.home.cancelled").setStyle(TextStyles.RED));
    }
}
