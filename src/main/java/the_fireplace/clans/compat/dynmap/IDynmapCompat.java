package the_fireplace.clans.compat.dynmap;

import the_fireplace.clans.compat.dynmap.data.ClanDimInfo;

public interface IDynmapCompat {
    void init();
    void serverStart();
    void queueClaimEventReceived(ClanDimInfo clanDimInfo);
}
