package the_fireplace.clans.util;

import net.minecraftforge.common.util.FakePlayer;
import org.apache.commons.lang3.ArrayUtils;
import the_fireplace.clans.Clans;
import the_fireplace.clans.abstraction.IConfig;

public class FakePlayerUtil {
    public static boolean isAllowedFakePlayer(Object entity) {
        if(entity instanceof FakePlayer) {
            if(Clans.getConfig().isFakePlayerDump())
                Clans.getMinecraftHelper().getLogger().info("Fake Player is being checked: {}", ((FakePlayer) entity).getDisplayNameString());
            return ArrayUtils.contains(Clans.getConfig().getTolerableFakePlayers(), "*") != ArrayUtils.contains(Clans.getConfig().getTolerableFakePlayers(), ((FakePlayer) entity).getDisplayNameString());
        } else
            return false;
    }
}
