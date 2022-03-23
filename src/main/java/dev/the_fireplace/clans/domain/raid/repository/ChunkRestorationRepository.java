package dev.the_fireplace.clans.domain.raid.repository;

import dev.the_fireplace.clans.raid.model.DimensionRecoveryData;
import net.minecraft.util.Identifier;

public interface ChunkRestorationRepository
{
    DimensionRecoveryData getOrCreateRecoveryData(Identifier dimensionId);
}
