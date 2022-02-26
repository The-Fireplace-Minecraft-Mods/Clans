package dev.the_fireplace.clans.domain.datastructure;

import dev.the_fireplace.clans.domain.datastructure.chunkedpositionmap.ChunkedPositionMap;

public interface ChunkedPositionMapFactory
{
    byte WORLD_CHUNK_SECTION_WIDTH = 16;

    /**
     * @param sectionWidth Powers of 2 are recommended for optimal performance
     */
    <T> ChunkedPositionMap<T> create(int sectionWidth);

    /**
     * @param sectionWidth Powers of 2 are recommended for optimal performance
     */
    <T> ChunkedPositionMap<T> createThreadSafe(int sectionWidth);
}
