package the_fireplace.clans.legacy.abstraction;

import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.legacy.model.ClanDimInfo;

public interface IDynmapCompat {
    void init();
    void serverStart();
    void queueClaimEventReceived(ClanDimInfo clanDimInfo);
    void refreshTooltip(Clan clan);
    void clearAllClanMarkers(Clan clan);
}
