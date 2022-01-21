package the_fireplace.clans.api.event;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import the_fireplace.clans.legacy.model.ChunkPosition;

import javax.annotation.Nullable;
import java.util.UUID;

public class PreLandClaimEvent
{
    public boolean isCancelled = false;
    public ITextComponent cancelledMessage;
    private final World world;
    private final ChunkPosition chunkPosition;
    @Nullable
    private final UUID claimingPlayer;
    private final UUID claimingClan;

    public PreLandClaimEvent(World world, ChunkPosition chunkPosition, @Nullable UUID claimingPlayer, UUID claimingClan) {
        this.world = world;
        this.chunkPosition = chunkPosition;
        this.claimingPlayer = claimingPlayer;
        this.claimingClan = claimingClan;
    }

    /**
     * The world that the claim is being made in.
     */
    public World getWorld() {
        return world;
    }

    /**
     * The player that is claiming the chunk, if any. This will be null if for some reason a claim isn't initiated by a specific player.
     */
    @Nullable
    public UUID getClaimingPlayer() {
        return claimingPlayer;
    }

    /**
     * The clan that is claiming the chunk.
     */
    public UUID getClaimingClan() {
        return claimingClan;
    }

    /**
     * The position of the chunk. This contains the chunk's x, z, and dimension.
     */
    public ChunkPosition getChunkPosition() {
        return chunkPosition;
    }
}
