package dev.the_fireplace.clans.domain.datastructure.chunkedpositionmap;

import dev.the_fireplace.clans.domain.datastructure.EmptyConcurrentMap;
import net.minecraft.util.math.Vec3i;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ThreadSafe
public class ThreadSafeBitShiftingChunkedPositionMap<T> extends BitShiftingChunkedPositionMap<T>
{
    private final Map<Integer, Map<Integer, Map<Vec3i, T>>> chunkedMap = new ConcurrentHashMap<>();

    /**
     * @inheritDoc
     */
    public ThreadSafeBitShiftingChunkedPositionMap(int chunkSize) {
        super(chunkSize);
    }

    protected Map<Vec3i, T> getOrCreateChunkMap(int chunkX, int chunkZ) {
        return getChunkedMap()
            .computeIfAbsent(chunkX, unused -> new ConcurrentHashMap<>())
            .computeIfAbsent(chunkZ, unused -> new ConcurrentHashMap<>());
    }


    protected Map<Vec3i, T> getChunkMap(int chunkX, int chunkZ) {
        return getChunkedMap()
            .getOrDefault(chunkX, new EmptyConcurrentMap<>())
            .getOrDefault(chunkZ, new EmptyConcurrentMap<>());
    }

    protected Map<Integer, Map<Integer, Map<Vec3i, T>>> getChunkedMap() {
        return chunkedMap;
    }
}
