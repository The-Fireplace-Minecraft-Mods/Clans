package the_fireplace.clans.clan;

import the_fireplace.clans.clan.accesscontrol.ClanLocks;
import the_fireplace.clans.clan.accesscontrol.ClanPermissions;
import the_fireplace.clans.clan.admin.AdminControlledClanSettings;
import the_fireplace.clans.clan.economics.ClanRent;
import the_fireplace.clans.clan.economics.ClanUpkeep;
import the_fireplace.clans.clan.home.ClanHomes;
import the_fireplace.clans.clan.membership.ClanMembers;
import the_fireplace.clans.clan.metadata.ClanBanners;
import the_fireplace.clans.clan.metadata.ClanColors;
import the_fireplace.clans.clan.metadata.ClanDescriptions;
import the_fireplace.clans.clan.metadata.ClanNames;
import the_fireplace.clans.clan.raids.ClanRaidStats;
import the_fireplace.clans.clan.raids.ClanShield;
import the_fireplace.clans.clan.raids.ClanWeaknessFactor;

import java.util.UUID;

public final class ClanSaver
{
    public static void saveAll() {
        for (UUID uuid : ClanIdRegistry.getIds()) {
            saveClan(uuid);
        }
    }

    private static void saveClan(UUID clan) {
        ClanLocks.get(clan).save();
        ClanPermissions.get(clan).save();
        AdminControlledClanSettings.get(clan).save();
        ClanRent.get(clan).save();
        ClanUpkeep.get(clan).save();
        if (ClanHomes.hasHome(clan)) {
            ClanHomes.get(clan).save();
        }
        ClanRent.get(clan).save();
        ClanMembers.get(clan).save();
        if (ClanBanners.hasBanner(clan)) {
            ClanBanners.get(clan).save();
        }
        ClanColors.get(clan).save();
        ClanDescriptions.get(clan).save();
        ClanNames.get(clan).save();
        ClanRaidStats.get(clan).save();
        ClanShield.get(clan).save();
        ClanWeaknessFactor.get(clan).save();
    }
}
