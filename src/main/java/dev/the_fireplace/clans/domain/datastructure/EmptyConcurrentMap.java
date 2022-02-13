package dev.the_fireplace.clans.domain.datastructure;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public final class EmptyConcurrentMap<K, V> implements ConcurrentMap<K, V>
{
    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean containsKey(Object o) {
        return false;
    }

    @Override
    public boolean containsValue(Object o) {
        return false;
    }

    @Nullable
    @Override
    public V get(Object o) {
        return null;
    }

    @Nullable
    @Override
    public V put(K k, V v) {
        return null;
    }

    @Nullable
    @Override
    public V remove(Object o) {
        return null;
    }

    @Override
    public void putAll(@Nonnull Map<? extends K, ? extends V> map) {

    }

    @Override
    public void clear() {

    }

    @Nonnull
    @Override
    public Set<K> keySet() {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Collection<V> values() {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<Entry<K, V>> entrySet() {
        return Collections.emptySet();
    }

    @Override
    public V getOrDefault(Object o, V v) {
        return v;
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> biConsumer) {

    }

    @Override
    public V putIfAbsent(@Nonnull K k, V v) {
        return null;
    }

    @Override
    public boolean remove(@Nonnull Object o, Object o1) {
        return false;
    }

    @Override
    public boolean replace(@Nonnull K k, @Nonnull V v, @Nonnull V v1) {
        return false;
    }

    @Override
    public V replace(@Nonnull K k, @Nonnull V v) {
        return null;
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> biFunction) {

    }
}
