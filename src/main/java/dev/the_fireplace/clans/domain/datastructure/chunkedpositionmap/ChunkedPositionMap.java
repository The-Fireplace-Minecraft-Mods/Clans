package dev.the_fireplace.clans.domain.datastructure.chunkedpositionmap;

import com.google.common.collect.Maps;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class ChunkedPositionMap<T> implements Map<Vec3i, T>
{
    public Map<Vec3i, T> getChunkValues(int chunkX, int chunkZ) {
        return Maps.newHashMap(getChunkMap(chunkX, chunkZ));
    }

    protected abstract Map<Vec3i, T> getOrCreateChunkMap(Vec3i blockPosition);

    protected abstract Map<Vec3i, T> getOrCreateChunkMap(int chunkX, int chunkZ);

    protected abstract Map<Vec3i, T> getChunkMap(Vec3i blockPosition);

    protected abstract Map<Vec3i, T> getChunkMap(int chunkX, int chunkZ);

    protected abstract Map<Integer, Map<Integer, Map<Vec3i, T>>> getChunkedMap();

    @Override
    public int size() {
        return getChunkedMap().values().stream().mapToInt(
            zMap -> zMap.values().stream().mapToInt(
                Map::size
            ).sum()
        ).sum();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        if (key instanceof Vec3i blockPosition) {
            return getChunkMap(blockPosition).containsKey(blockPosition);
        }

        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        if (value instanceof Vec3i blockPosition) {
            return getChunkMap(blockPosition).containsValue(blockPosition);
        }

        return false;
    }

    @Override
    public T get(Object key) {
        if (key instanceof Vec3i blockPosition) {
            return getChunkMap(blockPosition).get(blockPosition);
        }

        return null;
    }

    @Override
    public T put(Vec3i key, T value) {
        return getOrCreateChunkMap(key).put(key, value);
    }

    @Override
    public T remove(Object key) {
        if (key instanceof Vec3i blockPosition) {
            return getChunkMap(blockPosition).remove(blockPosition);
        }

        return null;
    }

    @Override
    public void putAll(@NotNull Map<? extends Vec3i, ? extends T> m) {
        m.forEach(
            (key, value) -> getOrCreateChunkMap(key).put(key, value)
        );
    }

    @Override
    public void clear() {
        getChunkedMap().clear();
    }

    @NotNull
    @Override
    public Set<Vec3i> keySet() {
        return getChunkedMap().values().stream().flatMap(
            zMap -> zMap.values().stream().flatMap(
                chunkContainedPositions -> chunkContainedPositions.keySet().stream()
            )
        ).collect(Collectors.toSet());
    }

    @NotNull
    @Override
    public Collection<T> values() {
        return getChunkedMap().values().stream().flatMap(
            zMap -> zMap.values().stream().flatMap(
                chunkContainedPositions -> chunkContainedPositions.values().stream()
            )
        ).collect(Collectors.toSet());
    }

    @NotNull
    @Override
    public Set<Entry<Vec3i, T>> entrySet() {
        return getChunkedMap().values().stream().flatMap(
            zMap -> zMap.values().stream().flatMap(
                chunkContainedPositions -> chunkContainedPositions.entrySet().stream()
            )
        ).collect(Collectors.toSet());
    }
}
