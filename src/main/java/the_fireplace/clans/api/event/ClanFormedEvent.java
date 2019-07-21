package the_fireplace.clans.api.event;

import the_fireplace.clans.model.Clan;

import javax.annotation.Nullable;
import java.util.UUID;

public class ClanFormedEvent {
    @Nullable
    private UUID formingPlayer;
    private Clan newClan;

    public ClanFormedEvent(@Nullable UUID formingPlayer, Clan newClan) {
        this.formingPlayer = formingPlayer;
        this.newClan = newClan;
    }

    /**
     * The player that is forming the clan, if any. This will be null if a player isn't the one forming the clan.
     */
    @Nullable
    public UUID getFormingPlayer() {
        return formingPlayer;
    }

    /**
     * The newly formed clan
     */
    public Clan getNewClan() {
        return newClan;
    }
}
