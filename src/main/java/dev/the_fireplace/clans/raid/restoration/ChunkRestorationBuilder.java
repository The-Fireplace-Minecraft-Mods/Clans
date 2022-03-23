package dev.the_fireplace.clans.raid.restoration;

import dev.the_fireplace.clans.api.raid.interfaces.RestorationStep;
import dev.the_fireplace.clans.raid.model.DimensionRecoveryData;
import dev.the_fireplace.clans.raid.restoration.step.CreateStep;
import dev.the_fireplace.clans.raid.restoration.step.DestroyStep;
import net.minecraft.util.math.Vec3i;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ChunkRestorationBuilder
{
    private final DimensionRecoveryData recoveryData;
    private final int chunkX;
    private final int chunkZ;
    private final List<RestorationStep> restorationSteps = new ArrayList<>();

    public ChunkRestorationBuilder(DimensionRecoveryData recoveryData, int chunkX, int chunkZ) {
        this.recoveryData = recoveryData;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    public List<RestorationStep> build() {
        restorationSteps.clear();
        //TODO Move players out of the chunks before we start restoring
        //TODO Clear primed TNT
        //TODO Clear falling blocks?
        addDestructionStep();
        addCreationStep();
        return restorationSteps;
    }

    private void addDestructionStep() {
        Map<Vec3i, String> blocksToRemove = recoveryData.popChunkBlocksToRemove(chunkX, chunkZ);
        if (!blocksToRemove.isEmpty()) {
            restorationSteps.add(new DestroyStep(blocksToRemove));
        }
    }

    private void addCreationStep() {
        Map<Vec3i, String> blocksToAdd = recoveryData.popChunkBlocksToAdd(chunkX, chunkZ);
        if (!blocksToAdd.isEmpty()) {
            restorationSteps.add(new CreateStep(blocksToAdd));
        }
    }
}
