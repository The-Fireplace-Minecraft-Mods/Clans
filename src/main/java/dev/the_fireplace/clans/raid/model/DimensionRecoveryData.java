package dev.the_fireplace.clans.raid.model;

import dev.the_fireplace.clans.ClansConstants;
import dev.the_fireplace.clans.domain.datastructure.ChunkedPositionMapFactory;
import dev.the_fireplace.clans.domain.datastructure.chunkedpositionmap.ChunkedPositionMap;
import dev.the_fireplace.lib.api.io.interfaces.access.StorageReadBuffer;
import dev.the_fireplace.lib.api.io.interfaces.access.StorageWriteBuffer;
import dev.the_fireplace.lib.api.lazyio.interfaces.SaveData;
import net.minecraft.util.Identifier;

public final class DimensionRecoveryData implements SaveData
{
    private final Identifier dimensionId;
    private final ChunkedPositionMap<String> blocksToAdd;
    private final ChunkedPositionMap<String> blocksToRemove;

    public DimensionRecoveryData(ChunkedPositionMapFactory chunkedPositionMapFactory, Identifier dimensionId) {
        this.dimensionId = dimensionId;
        this.blocksToAdd = chunkedPositionMapFactory.create(ChunkedPositionMapFactory.WORLD_CHUNK_SECTION_WIDTH);
        this.blocksToRemove = chunkedPositionMapFactory.create(ChunkedPositionMapFactory.WORLD_CHUNK_SECTION_WIDTH);
    }

    @Override
    public void readFrom(StorageReadBuffer buffer) {

    }

    @Override
    public void writeTo(StorageWriteBuffer buffer) {

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
        return null;
    }
}
