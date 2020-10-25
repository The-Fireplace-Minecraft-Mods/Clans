package the_fireplace.clans.legacy.logic;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import the_fireplace.clans.ClansModContainer;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanDatabase;
import the_fireplace.clans.clan.ClanMemberCache;
import the_fireplace.clans.clan.ClanNameCache;
import the_fireplace.clans.legacy.cache.PlayerAutoClaimData;
import the_fireplace.clans.legacy.cache.PlayerCache;
import the_fireplace.clans.legacy.cache.RaidingParties;
import the_fireplace.clans.legacy.data.ClaimData;
import the_fireplace.clans.legacy.util.EntityUtil;
import the_fireplace.clans.legacy.util.TextStyles;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;
import the_fireplace.clans.player.PlayerClanSettings;
import the_fireplace.clans.player.PlayerDataStorage;
import the_fireplace.clans.player.PlayerLastSeenData;

import java.util.UUID;

public class PlayerEventLogic {
    public static void onPlayerLoggedIn(EntityPlayer player) {
        checkUpdateDefaultClan(player);
        PlayerLastSeenData.updateLastSeen(player.getUniqueID());
        PlayerDataStorage.setShouldDisposeReferences(player.getUniqueID(), false);
        PlayerCache.setNeedsCleanup(player.getUniqueID(), false);
    }

    public static void onFirstLogin(UUID playerId) {
        Clan serverDefault = ClanNameCache.getClanByName(ClansModContainer.getConfig().getServerDefaultClan());
        if(serverDefault != null)
            serverDefault.addMember(playerId);
        else if(!ClansModContainer.getConfig().getServerDefaultClan().equalsIgnoreCase("N/A"))
            ClansModContainer.getLogger().warn("Invalid server default clan {}, players won't be added to it.", ClansModContainer.getConfig().getServerDefaultClan());
    }

    public static void checkUpdateDefaultClan(EntityPlayer player) {
        if(!player.world.isRemote) {
            UUID defaultClan = PlayerClanSettings.getDefaultClan(player.getUniqueID());
            if ((defaultClan != null && ClanDatabase.getClanById(defaultClan) == null) || (defaultClan == null && !ClanMemberCache.getClansPlayerIsIn(player.getUniqueID()).isEmpty()) || (defaultClan != null && !ClanMemberCache.getClansPlayerIsIn(player.getUniqueID()).contains(ClanDatabase.getClanById(defaultClan))))
                PlayerClanSettings.updateDefaultClanIfNeeded(player.getUniqueID(), null);
        }
    }

    public static void onPlayerLoggedOut(UUID playerId) {
        PlayerAutoClaimData.uncacheClaimingSettings(playerId);
        RaidingParties.playerLoggedOut(playerId);
        PlayerLastSeenData.updateLastSeen(playerId);
        PlayerDataStorage.setShouldDisposeReferences(playerId, true);
        PlayerCache.setNeedsCleanup(playerId, true);
    }

    public static void onPlayerChangedDimension(EntityPlayer player) {
        if(!player.world.isRemote) {
            PlayerCache.setClaimWarning(player.getUniqueID(), false);
            Clan ocAutoClaim = PlayerAutoClaimData.cancelOpAutoClaim(player.getUniqueID());
            boolean ocAutoAbandon = PlayerAutoClaimData.cancelOpAutoAbandon(player.getUniqueID());
            Clan cAutoAbandon = PlayerAutoClaimData.cancelAutoAbandon(player.getUniqueID());
            Clan cAutoClaim = PlayerAutoClaimData.cancelAutoClaim(player.getUniqueID());
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
            UUID defaultClanId = PlayerClanSettings.getDefaultClan(player.getUniqueID());
            if (defaultClanId != null) {
                Clan playerDefaultClan = ClanDatabase.getClanById(defaultClanId);
                if (playerDefaultClan != null)
                    return TranslationUtil.getTranslation(ClansModContainer.getConfig().getDefaultClanPrefix(), playerDefaultClan.getName()).setStyle(new Style().setColor(playerDefaultClan.getTextColor())).appendSibling(initialMessage.setStyle(TextStyles.RESET));
                else
                    PlayerClanSettings.updateDefaultClanIfNeeded(player.getUniqueID(), null);
            }
        }
        return initialMessage;
    }

    public static void onPlayerRespawn(EntityPlayer player) {
        if(!player.world.isRemote && ClansModContainer.getConfig().isClanHomeFallbackSpawnpoint() && player.bedLocation == null) {
            Clan defClan = ClanDatabase.getClanById(PlayerClanSettings.getDefaultClan(player.getUniqueID()));
            if (defClan != null && defClan.hasHome() && defClan.getHome() != null)
                EntityUtil.teleportHome(player, defClan.getHome(), defClan.getHomeDim(), player.dimension, true);
        }
    }

    public static void sendClanChat(EntityPlayer player, ITextComponent message) {
        if(!player.world.isRemote) {
            Clan clanChat = PlayerCache.getChattingWithClan(player);
            for (EntityPlayerMP member : clanChat.getOnlineMembers().keySet())
                member.sendMessage(TranslationUtil.getTranslation(member.getUniqueID(), "clans.chat.prefix", clanChat.getName()).setStyle(new Style().setColor(clanChat.getTextColor())).appendSibling(message));
        }
    }

    public static float breakSpeed(EntityPlayer player, float oldSpeed) {//TODO should borderlands be impacted by this?
        if(!player.world.isRemote && RaidingParties.isRaidedBy(ClaimData.getChunkClan(player.chunkCoordX, player.chunkCoordZ, player.dimension), player)) {
            return oldSpeed * (float) ClansModContainer.getConfig().getRaidBreakSpeedMultiplier();
        }
        return oldSpeed;
    }
    
    public static void onPlayerDamage(EntityPlayerMP player) {
        if(PlayerCache.cancelClanHomeWarmup(player))
            player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "commands.clan.home.cancelled").setStyle(TextStyles.RED));
    }
}
