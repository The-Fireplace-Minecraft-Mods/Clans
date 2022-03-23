package dev.the_fireplace.clans.raid.restoration.step;

import dev.the_fireplace.clans.api.raid.enums.WorldOperation;
import dev.the_fireplace.clans.api.raid.interfaces.RestorationStep;
import net.minecraft.util.math.Vec3i;

import java.util.Map;

public class DestroyStep implements RestorationStep
{
    private final Map<Vec3i, String> positions;

    public DestroyStep(Map<Vec3i, String> positions) {
        this.positions = positions;
    }

    @Override
    public WorldOperation getOperation() {
        return WorldOperation.DESTROY_BLOCK;
    }

    @Override
    public Map<Vec3i, String> getPositions() {
        return positions;
    }
}
