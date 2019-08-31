package the_fireplace.clans.forge;

import net.minecraftforge.common.util.FakePlayer;
import the_fireplace.clans.Clans;

import javax.annotation.Nullable;

public class FakePlayerUtil {
    public static boolean isAllowedFakePlayer(@Nullable Object entity, boolean ifNotFakePlayer) {
        if(entity instanceof FakePlayer) {
            if(Clans.getConfig().isFakePlayerDump())
                Clans.getMinecraftHelper().getLogger().info("Fake Player is being checked: {}", ((FakePlayer) entity).getDisplayNameString());
            return Clans.getConfig().getTolerableFakePlayers().contains("*") != Clans.getConfig().getTolerableFakePlayers().contains(((FakePlayer) entity).getDisplayNameString());
        } else
            return ifNotFakePlayer;
    }
}
