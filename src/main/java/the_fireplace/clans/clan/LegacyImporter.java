package the_fireplace.clans.clan;

import the_fireplace.clans.clan.accesscontrol.ClanLocks;
import the_fireplace.clans.clan.accesscontrol.ClanPermissions;
import the_fireplace.clans.clan.economics.ClanRent;
import the_fireplace.clans.clan.economics.ClanUpkeep;
import the_fireplace.clans.clan.home.ClanHomes;
import the_fireplace.clans.clan.land.ClanClaims;
import the_fireplace.clans.clan.membership.ClanMembers;
import the_fireplace.clans.clan.metadata.ClanBanners;
import the_fireplace.clans.clan.metadata.ClanColors;
import the_fireplace.clans.clan.metadata.ClanDescriptions;
import the_fireplace.clans.clan.metadata.ClanNames;
import the_fireplace.clans.clan.raids.ClanRaidStats;
import the_fireplace.clans.clan.raids.ClanShield;
import the_fireplace.clans.io.Directories;
import the_fireplace.clans.io.FileNames;
import the_fireplace.clans.io.JsonReader;

import java.io.File;
import java.util.UUID;

@Deprecated
public final class LegacyImporter {
    public static void importLegacyClans() {
        for(File file: FileNames.getUUIDJsonFolderContents(Directories.CLAN_DATA_LOCATION))
            importLegacyClan(file);
    }

    private static void importLegacyClan(File file) {
        JsonReader reader = JsonReader.create(file);
        if(reader == null)
            return;
        UUID clanId = reader.readUUID("clanId", null);
        if(clanId == null)
            clanId = getUUIDFromFileName(file);

        ClanIdRegistry.addLegacy(clanId);

        ClanLocks.get(clanId).readFromJson(reader);
        ClanLocks.get(clanId).markChanged();
        ClanPermissions.get(clanId).readFromJson(reader);
        ClanPermissions.get(clanId).markChanged();
        ClanRent.get(clanId).readFromJson(reader);
        ClanRent.get(clanId).markChanged();
        ClanUpkeep.get(clanId).readFromJson(reader);
        ClanUpkeep.get(clanId).markChanged();
        ClanHomes.get(clanId).readFromJson(reader);
        ClanHomes.get(clanId).markChanged();
        ClanClaims.get(clanId).readFromJson(reader);
        ClanClaims.get(clanId).markChanged();
        ClanMembers.get(clanId).readFromJson(reader);
        ClanMembers.get(clanId).markChanged();
        ClanBanners.get(clanId).readFromJson(reader);
        ClanBanners.get(clanId).markChanged();
        ClanNames.get(clanId).readFromJson(reader);
        ClanNames.get(clanId).markChanged();
        ClanDescriptions.get(clanId).readFromJson(reader);
        ClanDescriptions.get(clanId).markChanged();
        ClanColors.get(clanId).readFromJson(reader);
        ClanColors.get(clanId).markChanged();
        ClanRaidStats.get(clanId).readFromJson(reader);
        ClanRaidStats.get(clanId).markChanged();
        ClanShield.get(clanId).readFromJson(reader);
        ClanShield.get(clanId).markChanged();
    }

    private static UUID getUUIDFromFileName(File file) {
        return UUID.fromString(file.getName().substring(0, file.getName().length() - 5));
    }
}
