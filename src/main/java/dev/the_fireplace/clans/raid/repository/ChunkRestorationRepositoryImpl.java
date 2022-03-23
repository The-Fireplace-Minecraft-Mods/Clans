package dev.the_fireplace.clans.raid.repository;

import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.clans.domain.datastructure.ChunkedPositionMapFactory;
import dev.the_fireplace.clans.domain.raid.repository.ChunkRestorationRepository;
import dev.the_fireplace.clans.raid.model.DimensionRecoveryData;
import dev.the_fireplace.lib.api.lazyio.injectables.SaveDataStateManager;
import net.minecraft.util.Identifier;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Implementation
@Singleton
public final class ChunkRestorationRepositoryImpl implements ChunkRestorationRepository
{
    private final Map<Identifier, DimensionRecoveryData> dimensionRecoveryDatas = new HashMap<>();
    private final Function<Identifier, DimensionRecoveryData> initializeRecoveryData;

    @Inject
    public ChunkRestorationRepositoryImpl(SaveDataStateManager saveDataStateManager, ChunkedPositionMapFactory chunkedPositionMapFactory) {
        this.initializeRecoveryData = id -> new DimensionRecoveryData(
            saveDataStateManager,
            chunkedPositionMapFactory,
            id
        );
    }

    @Override
    public DimensionRecoveryData getOrCreateRecoveryData(Identifier dimensionId) {
        return dimensionRecoveryDatas.computeIfAbsent(dimensionId, initializeRecoveryData);
    }
}
