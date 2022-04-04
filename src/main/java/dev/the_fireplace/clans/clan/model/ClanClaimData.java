package dev.the_fireplace.clans.clan.model;

import com.google.common.collect.Maps;
import dev.the_fireplace.clans.ClansConstants;
import dev.the_fireplace.lib.api.io.interfaces.access.StorageReadBuffer;
import dev.the_fireplace.lib.api.io.interfaces.access.StorageWriteBuffer;
import dev.the_fireplace.lib.api.lazyio.injectables.SaveDataStateManager;
import dev.the_fireplace.lib.api.lazyio.interfaces.SaveData;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3i;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Function;

public final class ClanClaimData implements SaveData
{
    private static final Function<Identifier, Collection<Vec3i>> CREATE_POSITION_SET = k -> new ConcurrentSkipListSet<>();
    private final SaveDataStateManager saveDataStateManager;
    private final Map<Identifier, Collection<Vec3i>> claimsByDimension;
    private final UUID clanId;

    public ClanClaimData(SaveDataStateManager saveDataStateManager, UUID clanId) {
        this.saveDataStateManager = saveDataStateManager;
        this.clanId = clanId;
        this.claimsByDimension = new ConcurrentHashMap<>();
        this.saveDataStateManager.initializeWithAutosave(this, (short) 3);
    }

    public void addClaim(Identifier dimension, Vec3i position) {
        boolean addedClaim = this.claimsByDimension.computeIfAbsent(dimension, CREATE_POSITION_SET).add(position);
        if (addedClaim) {
            markChanged();
        }
    }

    public void removeClaim(Identifier dimension, Vec3i position) {
        if (!this.claimsByDimension.containsKey(dimension)) {
            return;
        }
        boolean removedClaim = this.claimsByDimension.get(dimension).remove(position);
        if (removedClaim) {
            markChanged();
        }
    }

    public boolean hasClaim(Identifier dimension, Vec3i position) {
        if (!this.claimsByDimension.containsKey(dimension)) {
            return false;
        }
        return this.claimsByDimension.get(dimension).contains(position);
    }

    public Map<Identifier, Collection<Vec3i>> getClaimsByDimension() {
        return Maps.newHashMap(claimsByDimension);
    }

    public long getClaimCount() {
        return this.claimsByDimension.values().stream().mapToLong(Collection::size).sum();
    }

    @Override
    public String getDatabase() {
        return ClansConstants.MODID;
    }

    @Override
    public String getTable() {
        return "clan_claims";
    }

    @Override
    public String getId() {
        return clanId.toString();
    }

    @Override
    public void readFrom(StorageReadBuffer buffer) {
        List<String> serializedClaims = buffer.readStringList("claims", Collections.emptyList());
        this.claimsByDimension.clear();
        this.claimsByDimension.putAll(deserializeClaims(serializedClaims));
    }

    @Override
    public void writeTo(StorageWriteBuffer buffer) {
        buffer.writeStringList("claims", serializeClaims(claimsByDimension));
    }

    //TODO replace this once FL has a relational database
    private List<String> serializeClaims(Map<Identifier, Collection<Vec3i>> claims) {
        List<String> serializedClaims = new ArrayList<>();
        claims.forEach((key, value) -> {
            value.forEach(position -> {
                String serializedPosition = String.format("%s,%d,%d,%d", key.toString(), position.getX(), position.getY(), position.getZ());
                serializedClaims.add(serializedPosition);
            });
        });
        return serializedClaims;
    }

    private Map<Identifier, Collection<Vec3i>> deserializeClaims(List<String> positionMap) {
        Map<Identifier, Collection<Vec3i>> deserializedMap = new HashMap<>();
        positionMap.forEach((value) -> {
            String[] positionParts = value.split(",");
            Identifier dimensionId = new Identifier(positionParts[0]);
            Vec3i position = new Vec3i(
                Integer.parseInt(positionParts[1]),
                Integer.parseInt(positionParts[2]),
                Integer.parseInt(positionParts[3])
            );
            deserializedMap.computeIfAbsent(dimensionId, CREATE_POSITION_SET).add(position);
        });
        return deserializedMap;
    }

    private void markChanged() {
        saveDataStateManager.markChanged(this);
    }
}
