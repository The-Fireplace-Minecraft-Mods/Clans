package dev.the_fireplace.clans.api.clan.injectables;

import dev.the_fireplace.clans.api.clan.interfaces.ClaimResponse;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3i;

import java.util.UUID;

public interface ClaimOperations
{
    ClaimResponse attemptBuyClaim(UUID clanId, Identifier dimensionId, Vec3i chunkSectionPos);

    ClaimResponse attemptSellClaim(UUID clanId, Identifier dimensionId, Vec3i chunkSectionPos);

    void sellAllClaims(UUID clanId);

    void forceClaim(UUID clanId, Identifier dimensionId, Vec3i chunkSectionPos);

    void forceOverwrite(UUID newClanId, Identifier dimensionId, Vec3i chunkSectionPos);

    void forceAbandonClaim(UUID clanId, Identifier dimensionId, Vec3i chunkSectionPos);
}
