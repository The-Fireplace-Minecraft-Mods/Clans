package dev.the_fireplace.clans.raid.restoration;

import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.clans.api.raid.injectables.ChunkRestorationTracker;
import dev.the_fireplace.clans.api.raid.interfaces.RestorationStep;
import dev.the_fireplace.clans.domain.raid.repository.ChunkRestorationRepository;
import dev.the_fireplace.clans.raid.model.DimensionRecoveryData;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3i;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
@Implementation
public final class ChunkRestorationTrackerImpl implements ChunkRestorationTracker
{
    private final ChunkRestorationRepository chunkRestorationRepository;

    @Inject
    public ChunkRestorationTrackerImpl(ChunkRestorationRepository chunkRestorationRepository) {
        this.chunkRestorationRepository = chunkRestorationRepository;
    }

    @Override
    public void trackAddedBlock(Identifier dimensionId, Vec3i position, String blockId) {
        getBlockMover(dimensionId).trackAddedBlock(position, blockId);
    }

    @Override
    public void trackRemovedBlock(Identifier dimensionId, Vec3i position, String blockId) {
        getBlockMover(dimensionId).trackRemovedBlock(position, blockId);
    }

    @Override
    public void trackShiftedBlock(Identifier dimensionId, Vec3i oldPosition, Vec3i newPosition, String blockId) {
        getBlockMover(dimensionId).trackShiftedBlock(oldPosition, newPosition, blockId);
    }

    @Override
    public boolean hasRestorationData(Identifier dimensionId, int chunkX, int chunkZ) {
        return getRecoveryData(dimensionId).hasChunkRecoveryData(chunkX, chunkZ);
    }

    @Override
    public List<RestorationStep> popRestorationData(Identifier dimensionId, int chunkX, int chunkZ) {
        DimensionRecoveryData recoveryData = getRecoveryData(dimensionId);
        return new ChunkRestorationBuilder(recoveryData, chunkX, chunkZ).build();
    }

    private RepoBlockMover getBlockMover(Identifier dimensionId) {
        return new RepoBlockMover(getRecoveryData(dimensionId));
    }

    private DimensionRecoveryData getRecoveryData(Identifier dimensionId) {
        return chunkRestorationRepository.getOrCreateRecoveryData(dimensionId);
    }
}
