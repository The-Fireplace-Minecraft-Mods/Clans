package dev.the_fireplace.clans.raid.model;

import dev.the_fireplace.clans.ClansConstants;
import dev.the_fireplace.clans.domain.datastructure.ChunkedPositionMapFactory;
import dev.the_fireplace.clans.domain.datastructure.chunkedpositionmap.ChunkedPositionMap;
import dev.the_fireplace.lib.api.io.interfaces.access.StorageReadBuffer;
import dev.the_fireplace.lib.api.io.interfaces.access.StorageWriteBuffer;
import dev.the_fireplace.lib.api.lazyio.injectables.SaveDataStateManager;
import dev.the_fireplace.lib.api.lazyio.interfaces.SaveData;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3i;

import java.util.HashMap;
import java.util.Map;

public final class DimensionRecoveryData implements SaveData
{
    private final Identifier dimensionId;
    private final ChunkedPositionMap<String> blocksToAdd;
    private final ChunkedPositionMap<String> blocksToRemove;

    public DimensionRecoveryData(
        SaveDataStateManager saveDataStateManager,
        ChunkedPositionMapFactory chunkedPositionMapFactory,
        Identifier dimensionId
    ) {
        this.dimensionId = dimensionId;
        this.blocksToAdd = chunkedPositionMapFactory.create(ChunkedPositionMapFactory.WORLD_CHUNK_SECTION_WIDTH);
        this.blocksToRemove = chunkedPositionMapFactory.create(ChunkedPositionMapFactory.WORLD_CHUNK_SECTION_WIDTH);
        saveDataStateManager.initializeWithAutosave(this, (short) 2);
    }

    public void setBlockToAdd(Vec3i position, String block) {
        blocksToAdd.put(position, block);
    }

    public void setBlockToRemove(Vec3i position, String block) {
        blocksToRemove.put(position, block);
    }

    @Override
    public void readFrom(StorageReadBuffer buffer) {
        this.blocksToAdd.putAll(
            deserializePositionMap(
                buffer.readStringToStringMap("blocksToAdd", new HashMap<>())
            )
        );
        this.blocksToRemove.putAll(
            deserializePositionMap(
                buffer.readStringToStringMap("blocksToRemove", new HashMap<>())
            )
        );
    }

    @Override
    public void writeTo(StorageWriteBuffer buffer) {
        buffer.writeStringToStringMap(
            "blocksToAdd",
            serializePositionMap(this.blocksToAdd)
        );
        buffer.writeStringToStringMap(
            "blocksToRemove",
            serializePositionMap(this.blocksToRemove)
        );
    }

    private Map<String, String> serializePositionMap(Map<Vec3i, String> positionMap) {
        Map<String, String> serializedMap = new HashMap<>();
        positionMap.forEach((key, value) -> serializedMap.put(key.toShortString(), value));
        return serializedMap;
    }

    private Map<Vec3i, String> deserializePositionMap(Map<String, String> positionMap) {
        Map<Vec3i, String> deserializedMap = new HashMap<>();
        positionMap.forEach((key, value) -> {
            String[] positionParts = key.split(",");
            Vec3i position = new Vec3i(
                Integer.parseInt(positionParts[0]),
                Integer.parseInt(positionParts[1]),
                Integer.parseInt(positionParts[2])
            );
            deserializedMap.put(position, value);
        });
        return deserializedMap;
    }

    @Override
    public String getDatabase() {
        return ClansConstants.MODID;
    }

    @Override
    public String getTable() {
        return "dimension_recovery";
    }

    @Override
    public String getId() {
        return dimensionId.toUnderscoreSeparatedString();
    }
}
