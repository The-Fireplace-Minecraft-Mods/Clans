package the_fireplace.clans.abstraction;

import the_fireplace.clans.model.Clan;
import the_fireplace.clans.model.ClanDimInfo;

public interface IDynmapCompat {
    void init();
    void serverStart();
    void queueClaimEventReceived(ClanDimInfo clanDimInfo);
    void refreshTooltip(Clan clan);
    void clearAllTeamMarkers(Clan clan);
}
