package dev.the_fireplace.clans.raid.restoration;

import dev.the_fireplace.clans.raid.model.DimensionRecoveryData;
import net.minecraft.util.math.Vec3i;

public final class RepoBlockMover
{
    private final DimensionRecoveryData recoveryData;

    public RepoBlockMover(DimensionRecoveryData recoveryData) {
        this.recoveryData = recoveryData;
    }

    public void trackAddedBlock(Vec3i position, String blockId) {
        if (this.recoveryData.hasBlockToAdd(position, blockId)) {
            this.recoveryData.clearBlockToAdd(position);
        } else {
            this.recoveryData.setBlockToRemove(position, blockId);
        }
    }

    public void trackRemovedBlock(Vec3i position, String blockId) {
        if (this.recoveryData.hasBlockToRemove(position)) {
            this.recoveryData.clearBlockToRemove(position);
        } else {
            this.recoveryData.setBlockToAdd(position, blockId);
        }
    }

    public void trackShiftedBlock(Vec3i oldPosition, Vec3i newPosition, String blockId) {
        if (this.recoveryData.hasBlockToRemove(oldPosition)) {
            this.recoveryData.clearBlockToRemove(oldPosition);
        } else {
            this.recoveryData.setBlockToAdd(oldPosition, blockId);
        }
        this.recoveryData.setBlockToRemove(newPosition, blockId);
    }

    public void trackReplacedBlock(Vec3i position, String oldBlockId, String newBlockId) {
        if (this.recoveryData.hasBlockToRemove(position)) {
            this.recoveryData.clearBlockToRemove(position);
        } else {
            this.recoveryData.setBlockToAdd(position, oldBlockId);
        }
        this.recoveryData.setBlockToRemove(position, newBlockId);
    }
}
