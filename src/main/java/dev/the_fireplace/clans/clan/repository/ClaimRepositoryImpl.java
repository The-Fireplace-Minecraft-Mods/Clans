package dev.the_fireplace.clans.clan.repository;

import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.clans.clan.model.ClanClaimData;
import dev.the_fireplace.clans.domain.clan.repository.ClaimRepository;
import dev.the_fireplace.clans.domain.datastructure.ChunkedPositionMapFactory;
import dev.the_fireplace.clans.domain.datastructure.chunkedpositionmap.ChunkedPositionMap;
import dev.the_fireplace.lib.api.lazyio.injectables.SaveDataStateManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3i;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Singleton
@Implementation
public final class ClaimRepositoryImpl implements ClaimRepository
{
    public static final Function<Vec3i, Map<Identifier, UUID>> NEW_CLANS_BY_DIMENSION = unused -> new ConcurrentHashMap<>();
    private final SaveDataStateManager saveDataStateManager;
    private final Map<UUID, ClanClaimData> claimDataByClan;
    private final ChunkedPositionMap<Map<Identifier, UUID>> clansByClaim;

    @Inject
    public ClaimRepositoryImpl(SaveDataStateManager saveDataStateManager, ChunkedPositionMapFactory chunkedPositionMapFactory) {
        this.saveDataStateManager = saveDataStateManager;
        this.claimDataByClan = new ConcurrentHashMap<>();
        this.clansByClaim = chunkedPositionMapFactory.createThreadSafe(32);
    }

    @Override
    public long getCount(UUID clanId) {
        return getClaimData(clanId).getClaimCount();
    }

    @Override
    public void store(UUID clanId, Identifier dimensionId, Vec3i chunkSectionPos) {
        Vec3i flatPosition = flattenPosition(chunkSectionPos);
        getClaimData(clanId).addClaim(dimensionId, flatPosition);
        clansByClaim.computeIfAbsent(flatPosition, NEW_CLANS_BY_DIMENSION).put(dimensionId, clanId);
    }

    @Override
    public void discard(UUID clanId, Identifier dimensionId, Vec3i chunkSectionPos) {
        Vec3i flatPosition = flattenPosition(chunkSectionPos);
        getClaimData(clanId).removeClaim(dimensionId, flatPosition);
        if (clansByClaim.containsKey(flatPosition)) {
            clansByClaim.get(flatPosition).remove(dimensionId, clanId);
        }
    }

    @Override
    public UUID getClan(Identifier dimensionId, Vec3i chunkSectionPos) {
        chunkSectionPos = flattenPosition(chunkSectionPos);
        if (clansByClaim.containsKey(chunkSectionPos)) {
            return clansByClaim.get(chunkSectionPos).get(dimensionId);
        }
        return null;
    }

    @Override
    public boolean contains(Identifier dimensionId, Vec3i chunkSectionPos) {
        Vec3i flattenedPos = flattenPosition(chunkSectionPos);
        return clansByClaim.containsKey(flattenedPos) && clansByClaim.get(flattenedPos).containsKey(dimensionId);
    }

    @Override
    public void discardAllForClan(UUID clanId) {
        ClanClaimData claimData = getClaimData(clanId);
        claimDataByClan.remove(clanId);
        claimData.getClaimsByDimension().forEach((dimensionId, flatPositions) ->
            flatPositions.forEach(flatPosition -> {
                    if (clansByClaim.containsKey(flatPosition)) {
                        clansByClaim.get(flatPosition).remove(dimensionId, clanId);
                    }
                }
            )
        );
        saveDataStateManager.delete(claimData);
    }

    private ClanClaimData getClaimData(UUID clanId) {
        return claimDataByClan.computeIfAbsent(clanId, this::loadData);
    }

    private synchronized ClanClaimData loadData(UUID clanId) {
        if (claimDataByClan.containsKey(clanId)) {
            return claimDataByClan.get(clanId);
        }
        ClanClaimData claimData = new ClanClaimData(saveDataStateManager, clanId);
        claimData.getClaimsByDimension().forEach((dimensionId, chunkSectionPosMap) ->
            chunkSectionPosMap.forEach(chunkSectionPos ->
                clansByClaim.get(chunkSectionPos).put(dimensionId, clanId)
            )
        );

        return claimData;
    }

    private Vec3i flattenPosition(Vec3i position) {
        //TODO handle Y value when 3D claims are implemented
        return new Vec3i(position.getX(), 0, position.getZ());
    }
}
