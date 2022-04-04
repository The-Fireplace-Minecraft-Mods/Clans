package dev.the_fireplace.clans.api.clan.injectables;

import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3i;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface ChunkDetails
{
    Optional<UUID> getOwner(Identifier dimensionId, Vec3i chunkSectionPos);

    boolean isClaimableBy(UUID clanId, Identifier dimensionId, Vec3i chunkSectionPos);

    //TODO good candidate for caching the results of the last few referenced chunks
    boolean allowsEntityType(EntityType<?> entityType, Identifier dimensionId, Vec3i chunkSectionPos);

    Collection<Vec3i> getClaims(UUID clanId, Identifier dimensionId);
}
