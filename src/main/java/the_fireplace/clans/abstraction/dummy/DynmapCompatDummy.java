package the_fireplace.clans.abstraction.dummy;

import the_fireplace.clans.abstraction.IDynmapCompat;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.model.ClanDimInfo;

public class DynmapCompatDummy implements IDynmapCompat {
    @Override
    public void init() {}

    @Override
    public void serverStart() {}

    @Override
    public void queueClaimEventReceived(ClanDimInfo clanDimInfo) {}

    @Override
    public void refreshTooltip(Clan clan) {}

    @Override
    public void clearAllTeamMarkers(Clan clan) {}
}
