package dev.the_fireplace.clans.compat.dummy;

import dev.the_fireplace.clans.domain.compat.DynmapCompat;
import dev.the_fireplace.clans.legacy.model.ClanDimInfo;

import java.util.UUID;

public class DynmapCompatDummy implements DynmapCompat
{
    @Override
    public void init() {
    }

    @Override
    public void serverStart() {
    }

    @Override
    public void queueClaimEventReceived(ClanDimInfo clanDimInfo) {
    }

    @Override
    public void refreshTooltip(UUID clan) {
    }

    @Override
    public void clearAllClanMarkers(UUID clan) {
    }
}
