package dev.the_fireplace.clans.raid.restoration.step;

import dev.the_fireplace.clans.api.raid.enums.WorldOperation;
import dev.the_fireplace.clans.api.raid.interfaces.RestorationStep;
import net.minecraft.util.math.Vec3i;

import java.util.Map;

public class CreateStep implements RestorationStep
{
    private final Map<Vec3i, String> positions;

    public CreateStep(Map<Vec3i, String> positions) {
        this.positions = positions;
    }

    @Override
    public WorldOperation getOperation() {
        return WorldOperation.CREATE_BLOCK;
    }

    @Override
    public Map<Vec3i, String> getPositions() {
        return positions;
    }
}
