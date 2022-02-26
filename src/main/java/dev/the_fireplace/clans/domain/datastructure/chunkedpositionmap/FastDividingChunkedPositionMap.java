package dev.the_fireplace.clans.domain.datastructure.chunkedpositionmap;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.minecraft.util.math.Vec3i;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@NotThreadSafe
public class FastDividingChunkedPositionMap<T> extends DividingChunkedPositionMap<T>
{
    private final Map<Integer, Map<Integer, Map<Vec3i, T>>> chunkedMap = new Int2ObjectArrayMap<>();

    /**
     * @inheritDoc
     */
    public FastDividingChunkedPositionMap(int chunkSize) {
        super(chunkSize);
    }

    protected Map<Vec3i, T> getOrCreateChunkMap(int chunkX, int chunkZ) {
        return getChunkedMap()
            .computeIfAbsent(chunkX, unused -> new Int2ObjectArrayMap<>())
            .computeIfAbsent(chunkZ, unused -> new HashMap<>());
    }


    protected Map<Vec3i, T> getChunkMap(int chunkX, int chunkZ) {
        return getChunkedMap()
            .getOrDefault(chunkX, Int2ObjectMaps.emptyMap())
            .getOrDefault(chunkZ, Collections.emptyMap());
    }

    protected Map<Integer, Map<Integer, Map<Vec3i, T>>> getChunkedMap() {
        return chunkedMap;
    }
}
