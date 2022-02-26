package dev.the_fireplace.clans.domain.datastructure.chunkedpositionmap;

import net.minecraft.util.math.Vec3i;

import java.util.Map;

public abstract class DividingChunkedPositionMap<T> extends ChunkedPositionMap<T>
{
    protected final int chunkSize;

    /**
     * Create a ChunkedPositionMap with chunks of size chunkSize x chunkSize
     */
    protected DividingChunkedPositionMap(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    protected Map<Vec3i, T> getOrCreateChunkMap(Vec3i blockPosition) {
        int chunkX = blockPosition.getX() / chunkSize;
        int chunkZ = blockPosition.getZ() / chunkSize;

        return getOrCreateChunkMap(chunkX, chunkZ);
    }

    protected Map<Vec3i, T> getChunkMap(Vec3i blockPosition) {
        int chunkX = blockPosition.getX() / chunkSize;
        int chunkZ = blockPosition.getZ() / chunkSize;

        return getChunkMap(chunkX, chunkZ);
    }
}
