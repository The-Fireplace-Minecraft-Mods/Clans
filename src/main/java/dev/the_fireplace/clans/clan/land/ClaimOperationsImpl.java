package dev.the_fireplace.clans.clan.land;

import com.google.common.collect.Lists;
import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.clans.api.clan.injectables.ClaimOperations;
import dev.the_fireplace.clans.api.clan.interfaces.ClaimResponse;
import dev.the_fireplace.clans.clan.land.response.ClaimFailure;
import dev.the_fireplace.clans.domain.clan.repository.ClaimRepository;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3i;

import javax.inject.Inject;
import java.util.UUID;

@Implementation
public final class ClaimOperationsImpl implements ClaimOperations
{
    private final ClaimRepository claimRepository;

    @Inject
    public ClaimOperationsImpl(ClaimRepository claimRepository) {
        this.claimRepository = claimRepository;
    }

    @Override
    public ClaimResponse attemptBuyClaim(UUID clanId, Identifier dimensionId, Vec3i chunkSectionPos) {
        return new ClaimFailure(Lists.newArrayList(Text.of("not implemented")));
    }

    @Override
    public ClaimResponse attemptSellClaim(UUID clanId, Identifier dimensionId, Vec3i chunkSectionPos) {
        return new ClaimFailure(Lists.newArrayList(Text.of("not implemented")));
    }

    @Override
    public void sellAllClaims(UUID clanId) {

    }

    @Override
    public void forceClaim(UUID clanId, Identifier dimensionId, Vec3i chunkSectionPos) {
        claimRepository.store(clanId, dimensionId, chunkSectionPos);
    }

    @Override
    public void forceOverwrite(UUID newClanId, Identifier dimensionId, Vec3i chunkSectionPos) {
        UUID oldClanId = claimRepository.getClan(dimensionId, chunkSectionPos);
        if (oldClanId != null) {
            ClaimResponse response = attemptSellClaim(oldClanId, dimensionId, chunkSectionPos);
            if (!response.isSuccess()) {
                forceAbandonClaim(oldClanId, dimensionId, chunkSectionPos);
            }
        }
        forceClaim(newClanId, dimensionId, chunkSectionPos);
    }

    @Override
    public void forceAbandonClaim(UUID clanId, Identifier dimensionId, Vec3i chunkSectionPos) {
        claimRepository.discard(clanId, dimensionId, chunkSectionPos);
    }
}
