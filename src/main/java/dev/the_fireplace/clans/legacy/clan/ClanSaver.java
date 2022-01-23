package dev.the_fireplace.clans.legacy.clan;

import dev.the_fireplace.clans.legacy.clan.accesscontrol.ClanLocks;
import dev.the_fireplace.clans.legacy.clan.accesscontrol.ClanPermissions;
import dev.the_fireplace.clans.legacy.clan.admin.AdminControlledClanSettings;
import dev.the_fireplace.clans.legacy.clan.economics.ClanRent;
import dev.the_fireplace.clans.legacy.clan.economics.ClanUpkeep;
import dev.the_fireplace.clans.legacy.clan.home.ClanHomes;
import dev.the_fireplace.clans.legacy.clan.membership.ClanMembers;
import dev.the_fireplace.clans.legacy.clan.metadata.ClanBanners;
import dev.the_fireplace.clans.legacy.clan.metadata.ClanColors;
import dev.the_fireplace.clans.legacy.clan.metadata.ClanDescriptions;
import dev.the_fireplace.clans.legacy.clan.metadata.ClanNames;
import dev.the_fireplace.clans.legacy.clan.raids.ClanRaidStats;
import dev.the_fireplace.clans.legacy.clan.raids.ClanShield;
import dev.the_fireplace.clans.legacy.clan.raids.ClanWeaknessFactor;

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
