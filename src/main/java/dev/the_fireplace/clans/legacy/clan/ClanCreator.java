package dev.the_fireplace.clans.legacy.clan;

import dev.the_fireplace.clans.api.event.ClanFormedEvent;
import dev.the_fireplace.clans.legacy.ClansModContainer;
import dev.the_fireplace.clans.legacy.clan.admin.AdminControlledClanSettings;
import dev.the_fireplace.clans.legacy.clan.economics.ClanEconomicFunctions;
import dev.the_fireplace.clans.legacy.clan.membership.ClanMembers;
import dev.the_fireplace.clans.legacy.clan.metadata.ClanNames;
import dev.the_fireplace.clans.legacy.model.EnumRank;
import dev.the_fireplace.clans.legacy.player.InvitedPlayers;
import dev.the_fireplace.clans.legacy.util.ClansEventManager;

import java.util.UUID;

public final class ClanCreator
{
    public static UUID createStandardClan(String clanName, UUID leaderId) {
        UUID clan = ClanIdRegistry.createAndRegisterClanId();

        ClanNames.get(clan).setName(clanName);
        ClanMembers.get(clan).addMember(leaderId, EnumRank.LEADER);
        ClanEconomicFunctions.get(clan).setInitialBalance();
        if (!ClansModContainer.getConfig().isAllowMultiClanMembership()) {
            InvitedPlayers.clearReceivedInvites(leaderId);
        }

        ClansEventManager.fireEvent(new ClanFormedEvent(leaderId, clan));

        return clan;
    }

    public static UUID createServerClan(String clanName) {
        UUID clan = ClanIdRegistry.createAndRegisterClanId();

        ClanNames.get(clan).setName(clanName);
        AdminControlledClanSettings.get(clan).setServerOwned(true);

        ClansEventManager.fireEvent(new ClanFormedEvent(null, clan));

        return clan;
    }
}
