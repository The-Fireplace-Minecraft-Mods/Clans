package dev.the_fireplace.clans.api.raid.interfaces;

import dev.the_fireplace.clans.api.raid.enums.WorldOperation;
import net.minecraft.util.math.Vec3i;

import java.util.Map;

public interface RestorationStep
{
    WorldOperation getOperation();

    Map<Vec3i, String> getPositions();
}
