package dev.the_fireplace.clans.domain.compat;

import dev.the_fireplace.clans.legacy.model.ClanDimInfo;

import java.util.UUID;

public interface DynmapCompat
{
    void init();

    void serverStart();

    void queueClaimEventReceived(ClanDimInfo clanDimInfo);

    void refreshTooltip(UUID clan);

    void clearAllClanMarkers(UUID clan);
}
