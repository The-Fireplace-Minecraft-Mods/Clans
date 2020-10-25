package the_fireplace.clans.legacy.abstraction.dummy;

import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.legacy.abstraction.IDynmapCompat;
import the_fireplace.clans.legacy.model.ClanDimInfo;

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
    public void clearAllClanMarkers(Clan clan) {}
}
