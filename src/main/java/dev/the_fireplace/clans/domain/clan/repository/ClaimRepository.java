package dev.the_fireplace.clans.domain.clan.repository;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3i;

import java.util.UUID;

public interface ClaimRepository
{
    long getCount(UUID clanId);

    void store(UUID clanId, Identifier dimensionId, Vec3i chunkSectionPos);

    void discard(UUID clanId, Identifier dimensionId, Vec3i chunkSectionPos);

    UUID getClan(Identifier dimensionId, Vec3i chunkSectionPos);

    boolean contains(Identifier dimensionId, Vec3i chunkSectionPos);

    void discardAllForClan(UUID clanId);
}
