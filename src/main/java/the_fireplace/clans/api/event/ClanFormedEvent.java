package the_fireplace.clans.api.event;

import javax.annotation.Nullable;
import java.util.UUID;

public class ClanFormedEvent
{
    @Nullable
    private final UUID formingPlayer;
    private final UUID newClan;

    public ClanFormedEvent(@Nullable UUID formingPlayer, UUID newClan) {
        this.formingPlayer = formingPlayer;
        this.newClan = newClan;
    }

    /**
     * The player that is forming the clan, if any. This will be null if a player isn't the one forming the clan, or the clan is a server clan.
     */
    @Nullable
    public UUID getFormingPlayer() {
        return formingPlayer;
    }

    /**
     * The newly formed clan ID
     */
    public UUID getNewClan() {
        return newClan;
    }
}
