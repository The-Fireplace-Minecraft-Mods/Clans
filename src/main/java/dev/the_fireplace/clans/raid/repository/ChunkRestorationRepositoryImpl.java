package dev.the_fireplace.clans.raid.repository;

import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.clans.api.raid.injectables.ChunkRestorationRepository;
import dev.the_fireplace.clans.api.raid.interfaces.RestorationStep;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3i;

import javax.inject.Singleton;
import java.util.List;

@Singleton
@Implementation
public class ChunkRestorationRepositoryImpl implements ChunkRestorationRepository
{
    @Override
    public void trackAddedBlock(Identifier dimensionId, Vec3i position, String blockId) {

    }

    @Override
    public void trackRemovedBlock(Identifier dimensionId, Vec3i position, String blockId) {

    }

    @Override
    public void trackShiftedBlock(Identifier dimensionId, Vec3i oldPosition, Vec3i newPosition, String blockId) {

    }

    @Override
    public boolean hasRestorationData(Identifier dimensionId, int chunkX, int chunkZ) {
        return false;
    }

    @Override
    public List<RestorationStep> popRestorationData(Identifier dimensionId, int chunkX, int chunkZ) {
        return null;
    }
}
