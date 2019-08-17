package the_fireplace.clans.logic;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import the_fireplace.clans.Clans;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.cache.RaidingParties;
import the_fireplace.clans.commands.teleportation.CommandHome;
import the_fireplace.clans.data.ClaimDataManager;
import the_fireplace.clans.data.PlayerDataManager;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import java.util.UUID;

public class PlayerEventLogic {
    public static void onPlayerLoggedIn(EntityPlayer player) {
        checkUpdateDefaultClan(player);
        PlayerDataManager.setShouldDisposeReferences(player.getUniqueID(), false);
    }

    public static void checkUpdateDefaultClan(EntityPlayer player) {
        if(!player.world.isRemote) {
            UUID defaultClan = PlayerDataManager.getDefaultClan(player.getUniqueID());
            if ((defaultClan != null && ClanCache.getClanById(defaultClan) == null) || (defaultClan == null && !ClanCache.getPlayerClans(player.getUniqueID()).isEmpty()) || (defaultClan != null && !ClanCache.getPlayerClans(player.getUniqueID()).contains(ClanCache.getClanById(defaultClan))))
                PlayerDataManager.updateDefaultClan(player.getUniqueID(), null);
        }
    }

    public static void onPlayerLoggedOut(UUID playerId) {
        ClanCache.getOpAutoClaimLands().remove(playerId);
        ClanCache.getOpAutoAbandonClaims().remove(playerId);
        ClanCache.getAutoAbandonClaims().remove(playerId);
        ClanCache.getAutoClaimLands().remove(playerId);
        ClanCache.getClanChattingPlayers().remove(playerId);
        PlayerDataManager.setShouldDisposeReferences(playerId, true);
    }

    public static void onPlayerChangedDimension(EntityPlayer player) {
        PlayerDataManager.setClaimWarning(player.getUniqueID(), false);
        Clan ocAutoClaim = ClanCache.getOpAutoClaimLands().remove(player.getUniqueID());
        boolean ocAutoAbandon = ClanCache.getOpAutoAbandonClaims().remove(player.getUniqueID());
        Clan cAutoAbandon = ClanCache.getAutoAbandonClaims().remove(player.getUniqueID());
        Clan cAutoClaim = ClanCache.getAutoClaimLands().remove(player.getUniqueID());
        if(ocAutoAbandon)
            player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "commands.opclan.autoabandon.stop").setStyle(TextStyles.GREEN));
        if(cAutoAbandon != null)
            player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "commands.clan.autoabandon.stop", cAutoAbandon.getClanName()).setStyle(TextStyles.GREEN));
        if(ocAutoClaim != null)
            player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "commands.clan.autoclaim.stop", ocAutoClaim.getClanName()).setStyle(TextStyles.GREEN));
        if(cAutoClaim != null)
            player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "commands.clan.autoclaim.stop", cAutoClaim.getClanName()).setStyle(TextStyles.GREEN));
    }

    public static ITextComponent getPlayerChatMessage(EntityPlayer player, ITextComponent initialMessage) {
        UUID defaultClanId = PlayerDataManager.getDefaultClan(player.getUniqueID());
        if (defaultClanId != null) {
            Clan playerDefaultClan = ClanCache.getClanById(defaultClanId);
            if (playerDefaultClan != null)
                return TranslationUtil.getTranslation("clans.chat.defaultclan", playerDefaultClan.getClanName()).setStyle(new Style().setColor(playerDefaultClan.getTextColor())).appendSibling(initialMessage.setStyle(TextStyles.RESET));
            else
                PlayerDataManager.updateDefaultClan(player.getUniqueID(), null);
        }
        return initialMessage;
    }

    public static void onPlayerRespawn(EntityPlayer player) {
        Clan defClan = ClanCache.getClanById(PlayerDataManager.getDefaultClan(player.getUniqueID()));
        if(defClan != null && defClan.hasHome() && defClan.getHome() != null)
            CommandHome.teleportHome(player, defClan, defClan.getHome(), player.dimension, true);
    }

    public static void sendClanChat(EntityPlayer player, ITextComponent message) {
        Clan clanChat = ClanCache.getClanChattingPlayers().get(player.getUniqueID());
        for(EntityPlayerMP member: clanChat.getOnlineMembers().keySet())
            member.sendMessage(TranslationUtil.getTranslation(member.getUniqueID(), "clans.chat.prefix", clanChat.getClanName()).setStyle(new Style().setColor(clanChat.getTextColor())).appendSibling(message));
    }

    public static float breakSpeed(EntityPlayer player, float oldSpeed) {//TODO should borderlands be impacted by this?
        if(RaidingParties.isRaidedBy(ClaimDataManager.getChunkClan(player.chunkCoordX, player.chunkCoordZ, player.dimension), player)) {
            return oldSpeed * (float)Clans.getConfig().getRaidBreakSpeedMultiplier();
        }
        return oldSpeed;
    }
}
