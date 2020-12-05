package the_fireplace.clans.clan;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import the_fireplace.clans.clan.accesscontrol.ClanLocks;
import the_fireplace.clans.clan.accesscontrol.ClanPermissions;
import the_fireplace.clans.clan.admin.AdminControlledClanSettings;
import the_fireplace.clans.clan.economics.ClanEconomicFunctions;
import the_fireplace.clans.clan.economics.ClanRent;
import the_fireplace.clans.clan.economics.ClanUpkeep;
import the_fireplace.clans.clan.home.ClanHomes;
import the_fireplace.clans.clan.land.ClanClaims;
import the_fireplace.clans.clan.membership.ClanMemberMessager;
import the_fireplace.clans.clan.membership.ClanMembers;
import the_fireplace.clans.clan.metadata.ClanBanners;
import the_fireplace.clans.clan.metadata.ClanColors;
import the_fireplace.clans.clan.metadata.ClanDescriptions;
import the_fireplace.clans.clan.metadata.ClanNames;
import the_fireplace.clans.clan.raids.ClanRaidStats;
import the_fireplace.clans.clan.raids.ClanShield;
import the_fireplace.clans.clan.raids.ClanWeaknessFactor;
import the_fireplace.clans.economy.Economy;
import the_fireplace.clans.legacy.ClansModContainer;
import the_fireplace.clans.legacy.cache.RaidingParties;
import the_fireplace.clans.legacy.data.ClaimData;
import the_fireplace.clans.legacy.util.FormulaParser;
import the_fireplace.clans.legacy.util.TextStyles;
import the_fireplace.clans.player.PlayerClanSettings;

import javax.annotation.Nullable;
import java.util.UUID;

public final class ClanDisbander {
    private final UUID clan;

    private ClanDisbander(UUID clan) {
        this.clan = clan;
    }

    public static ClanDisbander create(UUID clan) {
        return new ClanDisbander(clan);
    }

    public double getDisbandCost() {
        return FormulaParser.eval(ClansModContainer.getConfig().getDisbandFeeFormula(), clan, 1.0);
    }

    /**
     * Disbands a clan and unregisters cache for it where needed.
     * @param sender
     * The player that initiated this disband, if any. Used to determine which clan member, if any, should be exempt from the disband message
     * @param disbandMessageTranslationKey
     * The translation key of the message to go out to all online clan members when it gets disbanded
     * @param translationArgs
     * The arguments to pass in to the translation
     */
    public void disband(@Nullable ICommandSender sender, String disbandMessageTranslationKey, Object... translationArgs) {
        stopRaid();
        ClansModContainer.getDynmapCompat().clearAllClanMarkers(clan);
        notifyMembersOfDisband(sender, disbandMessageTranslationKey, translationArgs);
        boolean isServerClan = AdminControlledClanSettings.get(clan).isServerOwned();
        if(!isServerClan)
            distributeLiquidValueToClanMembers();
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
        ClanClaims.delete(clan);
        ClanMembers.delete(clan);
        ClanBanners.delete(clan);
        ClanColors.delete(clan);
        ClanDescriptions.delete(clan);
        ClanNames.delete(clan);
        ClanRaidStats.delete(clan);
        ClanShield.delete(clan);
        ClanWeaknessFactor.delete(clan);
        ClaimData.delClan(clan);
    }

    private void notifyMembersOfDisband(@Nullable ICommandSender sender, String disbandMessageTranslationKey, Object... translationArgs) {
        EntityPlayerMP senderEntity = sender instanceof EntityPlayerMP ? (EntityPlayerMP) sender : null;

        ClanMemberMessager.get(clan).messageAllOnline(senderEntity, TextStyles.YELLOW, disbandMessageTranslationKey, translationArgs);
    }

    private void updateMembersDefaultClans() {
        for (UUID member : ClanMembers.get(clan).getMembers()) {
            PlayerClanSettings.updateDefaultClanIfNeeded(member, clan);
        }
    }

    private void stopRaid() {
        if(RaidingParties.hasActiveRaid(clan))
            RaidingParties.getActiveRaid(clan).raiderVictory();
        if(RaidingParties.isPreparingRaid(clan))
            RaidingParties.removeRaid(RaidingParties.getInactiveRaid(clan));
    }

    private void distributeLiquidValueToClanMembers() {
        ClanEconomicFunctions economicFunctions = ClanEconomicFunctions.get(clan);
        double distFunds = economicFunctions.getLiquidValue();
        if (ClansModContainer.getConfig().isLeaderRecieveDisbandFunds())
            distFunds = economicFunctions.divideFundsAmongLeaders(distFunds);
        economicFunctions.divideFundsAmongAllMembers(distFunds);
        Economy.deductAmount(Economy.getBalance(clan), clan);
    }
}
