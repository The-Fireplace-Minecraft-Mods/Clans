package dev.the_fireplace.clans.legacy.clan;

import dev.the_fireplace.clans.economy.Economy;
import dev.the_fireplace.clans.legacy.ClansModContainer;
import dev.the_fireplace.clans.legacy.api.ClaimAccessor;
import dev.the_fireplace.clans.legacy.cache.RaidingParties;
import dev.the_fireplace.clans.legacy.clan.accesscontrol.ClanLocks;
import dev.the_fireplace.clans.legacy.clan.accesscontrol.ClanPermissions;
import dev.the_fireplace.clans.legacy.clan.admin.AdminControlledClanSettings;
import dev.the_fireplace.clans.legacy.clan.economics.ClanEconomicFunctions;
import dev.the_fireplace.clans.legacy.clan.economics.ClanRent;
import dev.the_fireplace.clans.legacy.clan.economics.ClanUpkeep;
import dev.the_fireplace.clans.legacy.clan.home.ClanHomes;
import dev.the_fireplace.clans.legacy.clan.land.ClanClaimCount;
import dev.the_fireplace.clans.legacy.clan.membership.ClanMemberMessager;
import dev.the_fireplace.clans.legacy.clan.membership.ClanMembers;
import dev.the_fireplace.clans.legacy.clan.membership.PlayerClans;
import dev.the_fireplace.clans.legacy.clan.metadata.ClanBanners;
import dev.the_fireplace.clans.legacy.clan.metadata.ClanColors;
import dev.the_fireplace.clans.legacy.clan.metadata.ClanDescriptions;
import dev.the_fireplace.clans.legacy.clan.metadata.ClanNames;
import dev.the_fireplace.clans.legacy.clan.raids.ClanRaidStats;
import dev.the_fireplace.clans.legacy.clan.raids.ClanShield;
import dev.the_fireplace.clans.legacy.clan.raids.ClanWeaknessFactor;
import dev.the_fireplace.clans.legacy.player.PlayerClanSettings;
import dev.the_fireplace.clans.legacy.util.FormulaParser;
import dev.the_fireplace.clans.legacy.util.TextStyles;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.network.ServerPlayerEntity;

import javax.annotation.Nullable;
import java.util.UUID;

public final class ClanDisbander
{
    private final UUID clan;

    private ClanDisbander(UUID clan) {
        this.clan = clan;
    }

    public static ClanDisbander create(UUID clan) {
        return new ClanDisbander(clan);
    }

    public double getDisbandCost() {
        return FormulaParser.eval(ClansModContainer.getConfig().getDisbandFeeFormula(), clan, 0.0);
    }

    /**
     * Disbands a clan and unregisters cache for it where needed.
     *
     * @param sender                       The player that initiated this disband, if any. Used to determine which clan member, if any, should be exempt from the disband message
     * @param disbandMessageTranslationKey The translation key of the message to go out to all online clan members when it gets disbanded
     * @param translationArgs              The arguments to pass in to the translation
     */
    public void disband(@Nullable ICommandSender sender, String disbandMessageTranslationKey, Object... translationArgs) {
        stopRaid();
        ClansModContainer.getDynmapCompat().clearAllClanMarkers(clan);
        notifyMembersOfDisband(sender, disbandMessageTranslationKey, translationArgs);
        boolean isServerClan = AdminControlledClanSettings.get(clan).isServerOwned();
        if (!isServerClan) {
            distributeLiquidValueToClanMembers();
        }
        updateMembersDefaultClans();
        unregisterClan();
    }

    private void unregisterClan() {
        ClanIdRegistry.deleteClanId(clan);
        ClanLocks.delete(clan);
        ClanPermissions.delete(clan);
        AdminControlledClanSettings.delete(clan);
        ClanRent.delete(clan);
        ClanUpkeep.delete(clan);
        ClanHomes.delete(clan);
        ClanClaimCount.delete(clan);
        ClanMembers.delete(clan);
        PlayerClans.uncacheClan(clan);
        ClanBanners.delete(clan);
        ClanColors.delete(clan);
        ClanDescriptions.delete(clan);
        ClanNames.delete(clan);
        ClanRaidStats.delete(clan);
        ClanShield.delete(clan);
        ClanWeaknessFactor.delete(clan);
        ClaimAccessor.getInstance().deleteClanClaims(clan);
    }

    private void notifyMembersOfDisband(@Nullable ICommandSender sender, String disbandMessageTranslationKey, Object... translationArgs) {
        ServerPlayerEntity senderEntity = sender instanceof ServerPlayerEntity ? (ServerPlayerEntity) sender : null;

        ClanMemberMessager.get(clan).messageAllOnline(senderEntity, TextStyles.YELLOW, disbandMessageTranslationKey, translationArgs);
    }

    private void updateMembersDefaultClans() {
        for (UUID member : ClanMembers.get(clan).getMembers()) {
            PlayerClanSettings.updateDefaultClanIfNeeded(member, clan);
        }
    }

    private void stopRaid() {
        if (RaidingParties.hasActiveRaid(clan)) {
            RaidingParties.getActiveRaid(clan).raiderVictory();
        }
        if (RaidingParties.isPreparingRaid(clan)) {
            RaidingParties.removeRaid(RaidingParties.getInactiveRaid(clan));
        }
    }

    private void distributeLiquidValueToClanMembers() {
        ClanEconomicFunctions economicFunctions = ClanEconomicFunctions.get(clan);
        double distFunds = economicFunctions.getLiquidValue();
        if (ClansModContainer.getConfig().isLeaderRecieveDisbandFunds()) {
            distFunds = economicFunctions.divideFundsAmongLeaders(distFunds);
        }
        economicFunctions.divideFundsAmongAllMembers(distFunds);
        Economy.deductAmount(Economy.getBalance(clan), clan);
    }
}
