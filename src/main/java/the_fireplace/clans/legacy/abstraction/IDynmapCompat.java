package the_fireplace.clans.legacy.abstraction;

import the_fireplace.clans.legacy.model.ClanDimInfo;

import java.util.UUID;

public interface IDynmapCompat
{
    void init();

    void serverStart();

    void queueClaimEventReceived(ClanDimInfo clanDimInfo);

    void refreshTooltip(UUID clan);

    void clearAllClanMarkers(UUID clan);
}
