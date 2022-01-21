package the_fireplace.clans.legacy.forge;

import net.minecraftforge.common.util.FakePlayer;
import the_fireplace.clans.legacy.ClansModContainer;

import javax.annotation.Nullable;

public class FakePlayerUtil
{
    public static boolean isAllowedFakePlayer(@Nullable Object entity, boolean ifNotFakePlayer) {
        if (entity instanceof FakePlayer) {
            if (ClansModContainer.getConfig().isFakePlayerDump()) {
                ClansModContainer.getMinecraftHelper().getLogger().info("Fake Player is being checked: {}", ((FakePlayer) entity).getDisplayNameString());
            }
            return ClansModContainer.getConfig().getTolerableFakePlayers().contains("*") != ClansModContainer.getConfig().getTolerableFakePlayers().contains(((FakePlayer) entity).getDisplayNameString());
        } else {
            return ifNotFakePlayer;
        }
    }
}
