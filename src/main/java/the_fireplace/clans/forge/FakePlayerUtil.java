package the_fireplace.clans.forge;

import net.minecraftforge.common.util.FakePlayer;
import the_fireplace.clans.ClansHelper;
import the_fireplace.clans.Clans;

import javax.annotation.Nullable;

public class FakePlayerUtil {
    public static boolean isAllowedFakePlayer(@Nullable Object entity, boolean ifNotFakePlayer) {
        if(entity instanceof FakePlayer) {
            if(ClansHelper.getConfig().isFakePlayerDump())
                Clans.getMinecraftHelper().getLogger().info("Fake Player is being checked: {}", ((FakePlayer) entity).getDisplayNameString());
            return ClansHelper.getConfig().getTolerableFakePlayers().contains("*") != ClansHelper.getConfig().getTolerableFakePlayers().contains(((FakePlayer) entity).getDisplayNameString());
        } else
            return ifNotFakePlayer;
    }
}
