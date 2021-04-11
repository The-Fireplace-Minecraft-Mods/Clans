package the_fireplace.clans.legacy.api;

import net.minecraft.entity.Entity;
import the_fireplace.clans.legacy.data.ClaimData;
import the_fireplace.clans.legacy.model.ChunkPosition;
import the_fireplace.clans.legacy.model.ChunkPositionWithData;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;

public interface ClaimAccessor {
    static ClaimAccessor getInstance() {
        //noinspection deprecation
        return ClaimData.INSTANCE;
    }

    long getClaimedChunkCount(UUID clan);

    Set<ChunkPositionWithData> getBorderlandChunks(UUID clan);

    Set<UUID> getClansWithClaims();

    void addChunk(UUID clan, ChunkPositionWithData pos);

    void delChunk(UUID clan, ChunkPositionWithData pos);

    Set<Integer> getClaimDims(UUID clanId);

    boolean isBorderland(int x, int z, int d);

    @Nullable
    ChunkPositionWithData getChunkPositionData(int x, int z, int d);

    @Nullable
    ChunkPositionWithData getChunkPositionData(Entity entity);

    @Nullable
    ChunkPositionWithData getChunkPositionData(ChunkPosition pos);

    @Nullable
    UUID getChunkClan(Entity entity);

    @Nullable
    UUID getChunkClan(int x, int z, int dim);

    @Nullable
    UUID getChunkClan(@Nullable ChunkPosition position);

    boolean deleteClanClaims(@Nullable UUID clan);

    void updateChunkOwner(ChunkPositionWithData pos, @Nullable UUID oldOwner, UUID newOwner);

    boolean chunkDataExists(ChunkPositionWithData position);

    Set<ChunkPositionWithData> getClaimedChunks(UUID clan);
}
