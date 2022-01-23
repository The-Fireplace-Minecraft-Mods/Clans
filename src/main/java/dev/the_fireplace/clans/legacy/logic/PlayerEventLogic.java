package dev.the_fireplace.clans.legacy.logic;

import dev.the_fireplace.clans.legacy.ClansModContainer;
import dev.the_fireplace.clans.legacy.api.ClaimAccessor;
import dev.the_fireplace.clans.legacy.cache.PlayerCache;
import dev.the_fireplace.clans.legacy.cache.RaidingParties;
import dev.the_fireplace.clans.legacy.clan.ClanIdRegistry;
import dev.the_fireplace.clans.legacy.clan.home.ClanHomes;
import dev.the_fireplace.clans.legacy.clan.membership.ClanMembers;
import dev.the_fireplace.clans.legacy.clan.membership.PlayerClans;
import dev.the_fireplace.clans.legacy.clan.metadata.ClanColors;
import dev.the_fireplace.clans.legacy.clan.metadata.ClanNames;
import dev.the_fireplace.clans.legacy.player.PlayerClanSettings;
import dev.the_fireplace.clans.legacy.player.PlayerDataStorage;
import dev.the_fireplace.clans.legacy.player.PlayerLastSeenData;
import dev.the_fireplace.clans.legacy.player.autoland.AutoAbandon;
import dev.the_fireplace.clans.legacy.player.autoland.AutoClaim;
import dev.the_fireplace.clans.legacy.player.autoland.OpAutoAbandon;
import dev.the_fireplace.clans.legacy.player.autoland.OpAutoClaim;
import dev.the_fireplace.clans.legacy.util.EntityUtil;
import dev.the_fireplace.clans.legacy.util.TextStyles;
import dev.the_fireplace.clans.legacy.util.translation.TranslationUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;

import java.util.UUID;

public class PlayerEventLogic
{
    public static void onPlayerLoggedIn(EntityPlayer player) {
        checkUpdateDefaultClan(player);
        PlayerLastSeenData.updateLastSeen(player.getUniqueID());
        PlayerDataStorage.setShouldDisposeReferences(player.getUniqueID(), false);
        PlayerCache.setNeedsCleanup(player.getUniqueID(), false);
    }

    public static void onFirstLogin(UUID playerId) {
        UUID serverDefault = ClanNames.getClanByName(ClansModContainer.getConfig().getServerDefaultClan());
        if (serverDefault != null) {
            ClanMembers.get(serverDefault).addMember(playerId);
        } else if (!ClansModContainer.getConfig().getServerDefaultClan().equalsIgnoreCase("N/A")) {
            ClansModContainer.getLogger().warn("Invalid server default clan {}, players won't be added to it.", ClansModContainer.getConfig().getServerDefaultClan());
        }
    }

    public static void checkUpdateDefaultClan(EntityPlayer player) {
        if (!player.world.isRemote) {
            UUID defaultClan = PlayerClanSettings.getDefaultClan(player.getUniqueID());
            if ((defaultClan != null && ClanIdRegistry.isValidClan(defaultClan)) || (defaultClan == null && !PlayerClans.getClansPlayerIsIn(player.getUniqueID()).isEmpty()) || (defaultClan != null && !PlayerClans.getClansPlayerIsIn(player.getUniqueID()).contains(defaultClan))) {
                PlayerClanSettings.updateDefaultClanIfNeeded(player.getUniqueID(), null);
            }
        }
    }

    public static void onPlayerLoggedOut(UUID playerId) {
        AutoAbandon.cancelAutoAbandon(playerId);
        AutoClaim.cancelAutoClaim(playerId);
        OpAutoAbandon.cancelOpAutoAbandon(playerId);
        OpAutoClaim.cancelAutoClaim(playerId);
        RaidingParties.playerLoggedOut(playerId);
        PlayerLastSeenData.updateLastSeen(playerId);
        PlayerDataStorage.setShouldDisposeReferences(playerId, true);
        PlayerCache.setNeedsCleanup(playerId, true);
    }

    public static void onPlayerChangedDimension(EntityPlayer player) {
        if (!player.world.isRemote) {
            PlayerCache.setClaimWarning(player.getUniqueID(), false);
            UUID ocAutoClaim = OpAutoClaim.cancelAutoClaim(player.getUniqueID());
            boolean ocAutoAbandon = OpAutoAbandon.cancelOpAutoAbandon(player.getUniqueID());
            UUID cAutoAbandon = AutoAbandon.cancelAutoAbandon(player.getUniqueID());
            UUID cAutoClaim = AutoClaim.cancelAutoClaim(player.getUniqueID());
            if (ocAutoAbandon) {
                player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "commands.opclan.autoabandon.stop").setStyle(TextStyles.GREEN));
            }
            if (cAutoAbandon != null) {
                player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "commands.clan.autoabandon.stop", ClanNames.get(cAutoAbandon).getName()).setStyle(TextStyles.GREEN));
            }
            if (ocAutoClaim != null) {
                player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "commands.clan.autoclaim.stop", ClanNames.get(ocAutoClaim).getName()).setStyle(TextStyles.GREEN));
            }
            if (cAutoClaim != null) {
                player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "commands.clan.autoclaim.stop", ClanNames.get(cAutoClaim).getName()).setStyle(TextStyles.GREEN));
            }
        }
    }

    public static ITextComponent getPlayerChatMessage(EntityPlayer player, ITextComponent initialMessage) {
        if (!player.world.isRemote) {
            UUID defaultClanId = PlayerClanSettings.getDefaultClan(player.getUniqueID());
            if (defaultClanId != null) {
                if (ClanIdRegistry.isValidClan(defaultClanId)) {
                    return TranslationUtil.getTranslation(ClansModContainer.getConfig().getDefaultClanPrefix(), ClanNames.get(defaultClanId).getName()).setStyle(new Style().setColor(ClanColors.get(defaultClanId).getColorFormatting())).appendSibling(initialMessage.setStyle(TextStyles.RESET));
                } else {
                    PlayerClanSettings.updateDefaultClanIfNeeded(player.getUniqueID(), null);
                }
            }
        }
        return initialMessage;
    }

    public static void onPlayerRespawn(EntityPlayer player) {
        if (!player.world.isRemote && ClansModContainer.getConfig().isClanHomeFallbackSpawnpoint() && player.bedLocation == null) {
            UUID defClan = PlayerClanSettings.getDefaultClan(player.getUniqueID());
            if (defClan != null && ClanHomes.hasHome(defClan)) {
                EntityUtil.teleportHome(player, ClanHomes.get(defClan).toBlockPos(), ClanHomes.get(defClan).getHomeDim(), player.dimension, true);
            }
        }
    }

    public static void sendClanChat(EntityPlayer player, ITextComponent message) {
        if (!player.world.isRemote) {
            UUID clanChat = PlayerCache.getChattingWithClan(player);
            for (ServerPlayerEntity member : ClanMembers.get(clanChat).getOnlineMemberRanks().keySet()) {
                member.sendMessage(TranslationUtil.getTranslation(member.getUniqueID(), "clans.chat.prefix", ClanNames.get(clanChat).getName()).setStyle(new Style().setColor(ClanColors.get(clanChat).getColorFormatting())).appendSibling(message));
            }
        }
    }

    public static float breakSpeed(EntityPlayer player, float oldSpeed) {//TODO should borderlands be impacted by this?
        if (!player.world.isRemote && RaidingParties.isRaidedBy(ClaimAccessor.getInstance().getChunkClan(player.chunkCoordX, player.chunkCoordZ, player.dimension), player)) {
            return oldSpeed * (float) ClansModContainer.getConfig().getRaidBreakSpeedMultiplier();
        }
        return oldSpeed;
    }

    public static void onPlayerDamage(ServerPlayerEntity player) {
        if (PlayerCache.cancelClanHomeWarmup(player)) {
            player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "commands.clan.home.cancelled").setStyle(TextStyles.RED));
        }
    }
}
