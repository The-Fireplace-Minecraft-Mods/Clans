package the_fireplace.clans.clan;

import the_fireplace.clans.api.event.ClanFormedEvent;
import the_fireplace.clans.clan.admin.AdminControlledClanSettings;
import the_fireplace.clans.clan.economics.ClanEconomicFunctions;
import the_fireplace.clans.clan.membership.ClanMembers;
import the_fireplace.clans.clan.metadata.ClanNames;
import the_fireplace.clans.legacy.ClansModContainer;
import the_fireplace.clans.legacy.model.EnumRank;
import the_fireplace.clans.legacy.util.ClansEventManager;
import the_fireplace.clans.player.InvitedPlayers;

import java.util.UUID;

public final class ClanCreator {
    public static UUID createStandardClan(String clanName, UUID leaderId) {
        UUID clan = ClanIdRegistry.createAndRegisterClanId();

        ClanNames.get(clan).setName(clanName);
        ClanMembers.get(clan).addMember(leaderId, EnumRank.LEADER);
        ClanEconomicFunctions.get(clan).setInitialBalance();
        if(!ClansModContainer.getConfig().isAllowMultiClanMembership())
            InvitedPlayers.clearReceivedInvites(leaderId);

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
