package the_fireplace.clans.forge;

import net.minecraftforge.common.util.FakePlayer;
import the_fireplace.clans.Clans;

public class FakePlayerUtil {
    public static boolean isAllowedFakePlayer(Object entity) {
        if(entity instanceof FakePlayer) {
            if(Clans.getConfig().isFakePlayerDump())
                Clans.getMinecraftHelper().getLogger().info("Fake Player is being checked: {}", ((FakePlayer) entity).getDisplayNameString());
            return Clans.getConfig().getTolerableFakePlayers().contains("*") != Clans.getConfig().getTolerableFakePlayers().contains(((FakePlayer) entity).getDisplayNameString());
        } else
            return false;
    }
}
