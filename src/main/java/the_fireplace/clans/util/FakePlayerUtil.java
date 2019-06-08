package the_fireplace.clans.util;

import net.minecraftforge.common.util.FakePlayer;
import org.apache.commons.lang3.ArrayUtils;
import the_fireplace.clans.Clans;

public class FakePlayerUtil {
    public static boolean isAllowedFakePlayer(Object entity) {
        if(entity instanceof FakePlayer) {
            if(Clans.cfg.fakePlayerDump)
                Clans.LOGGER.info("Fake Player is being checked: %s", ((FakePlayer) entity).getName());
            return ArrayUtils.contains(Clans.cfg.tolerableFakePlayers, "*") != ArrayUtils.contains(Clans.cfg.tolerableFakePlayers, ((FakePlayer) entity).getName());
        } else
            return false;
    }
}
