package the_fireplace.clans.player;

import the_fireplace.clans.clan.membership.PlayerClans;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public final class PlayerClanSettings {
    @Nullable
    public static UUID getDefaultClan(UUID player) {
        return PlayerDataStorage.getPlayerData(player).getDefaultClan();
    }

    public static void setDefaultClan(UUID player, @Nullable UUID defaultClan) {
        PlayerDataStorage.getPlayerData(player).setDefaultClan(defaultClan);
    }

    /**
     * Check if a clan is the player's default clan, and if it is, update the player's default clan to something else.
     * @param player
     * The player to check and update (if needed)
     * @param checkForDefaultClan
     * The clan the player is being removed from. Use null to forcibly change the player's default clan, regardless of what it currently is.
     */
    public static void updateDefaultClanIfNeeded(UUID player, @Nullable UUID checkForDefaultClan) {
        UUID currentDefaultClan = getDefaultClan(player);
        if(checkForDefaultClan == null || checkForDefaultClan.equals(currentDefaultClan)) {
            Optional<UUID> newDefaultClan = PlayerClans.getClansPlayerIsIn(player).stream().findAny();

            setDefaultClan(player, newDefaultClan.orElse(null));
        }
    }
}
