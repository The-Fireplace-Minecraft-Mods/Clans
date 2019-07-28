package the_fireplace.clans.forge;

import net.minecraftforge.common.util.FakePlayer;
import org.apache.commons.lang3.ArrayUtils;
import the_fireplace.clans.Clans;

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
