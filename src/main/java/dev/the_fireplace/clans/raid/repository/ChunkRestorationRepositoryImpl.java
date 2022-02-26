package dev.the_fireplace.clans.raid.repository;

import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.clans.api.raid.injectables.ChunkRestorationRepository;
import dev.the_fireplace.clans.api.raid.interfaces.RestorationStep;
import dev.the_fireplace.clans.domain.datastructure.ChunkedPositionMapFactory;
import dev.the_fireplace.clans.raid.model.DimensionRecoveryData;
import dev.the_fireplace.lib.api.lazyio.injectables.SaveDataStateManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3i;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Singleton
@Implementation
public final class ChunkRestorationRepositoryImpl implements ChunkRestorationRepository
{
    private final Map<Identifier, DimensionRecoveryData> dimensionRecoveryDatas = new HashMap<>();
    private final SaveDataStateManager saveDataStateManager;
    private final ChunkedPositionMapFactory chunkedPositionMapFactory;
    private final Function<Identifier, DimensionRecoveryData> initializeRecoveryData;

    @Inject
    public ChunkRestorationRepositoryImpl(SaveDataStateManager saveDataStateManager, ChunkedPositionMapFactory chunkedPositionMapFactory) {
        this.saveDataStateManager = saveDataStateManager;
        this.chunkedPositionMapFactory = chunkedPositionMapFactory;
        this.initializeRecoveryData = id -> new DimensionRecoveryData(
            saveDataStateManager,
            chunkedPositionMapFactory,
            id
        );
    }

    @Override
    public void trackAddedBlock(Identifier dimensionId, Vec3i position, String blockId) {
        getOrCreateRecoveryData(dimensionId).setBlockToRemove(position, blockId);
    }

    @Override
    public void trackRemovedBlock(Identifier dimensionId, Vec3i position, String blockId) {
        //TODO clear block to remove?
        getOrCreateRecoveryData(dimensionId).setBlockToAdd(position, blockId);
    }

    @Override
    public void trackShiftedBlock(Identifier dimensionId, Vec3i oldPosition, Vec3i newPosition, String blockId) {
        //TODO where is the best place to handle logic for this? Probably not the repo.
    }

    @Override
    public boolean hasRestorationData(Identifier dimensionId, int chunkX, int chunkZ) {
        return false;
    }

    @Override
    public List<RestorationStep> popRestorationData(Identifier dimensionId, int chunkX, int chunkZ) {
        return null;
    }

    private DimensionRecoveryData getOrCreateRecoveryData(Identifier dimensionId) {
        return dimensionRecoveryDatas.computeIfAbsent(dimensionId, initializeRecoveryData);
    }
}
