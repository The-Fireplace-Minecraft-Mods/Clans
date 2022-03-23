package dev.the_fireplace.clans.api.raid.injectables;

import dev.the_fireplace.clans.api.raid.interfaces.RestorationStep;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3i;

import java.util.List;

public interface ChunkRestorationTracker
{
    void trackAddedBlock(Identifier dimensionId, Vec3i position, String blockId);

    void trackRemovedBlock(Identifier dimensionId, Vec3i position, String blockId);

    void trackShiftedBlock(Identifier dimensionId, Vec3i oldPosition, Vec3i newPosition, String blockId);

    void trackReplacedBlock(Identifier dimensionId, Vec3i position, String oldBlockId, String newBlockId);

    boolean hasRestorationData(Identifier dimensionId, int chunkX, int chunkZ);

    List<RestorationStep> popRestorationData(Identifier dimensionId, int chunkX, int chunkZ);
}
