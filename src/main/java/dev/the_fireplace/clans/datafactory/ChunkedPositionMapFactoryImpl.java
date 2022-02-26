package dev.the_fireplace.clans.datafactory;

import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.clans.domain.datastructure.ChunkedPositionMapFactory;
import dev.the_fireplace.clans.domain.datastructure.chunkedpositionmap.*;

@Implementation
public final class ChunkedPositionMapFactoryImpl implements ChunkedPositionMapFactory
{
    @Override
    public <T> ChunkedPositionMap<T> create(int sectionWidth) {
        validateWidth(sectionWidth);

        if (isPowerOf2(sectionWidth)) {
            return new FastBitShiftingChunkedPositionMap<>(getPowerOf2(sectionWidth));
        } else {
            return new FastDividingChunkedPositionMap<>(sectionWidth);
        }
    }

    @Override
    public <T> ChunkedPositionMap<T> createThreadSafe(int sectionWidth) {
        validateWidth(sectionWidth);

        if (isPowerOf2(sectionWidth)) {
            return new ThreadSafeBitShiftingChunkedPositionMap<>(getPowerOf2(sectionWidth));
        } else {
            return new ThreadSafeDividingChunkedPositionMap<>(sectionWidth);
        }
    }

    private void validateWidth(int sectionWidth) {
        if (sectionWidth <= 1) {
            throw new IllegalArgumentException("Section Width must be greater than 1!");
        }
    }

    private boolean isPowerOf2(int sectionWidth) {
        double logBase2 = logBase2(sectionWidth);

        return (int) Math.floor(logBase2) == (int) Math.ceil(logBase2);
    }

    private int getPowerOf2(int sectionWidth) {
        return (int) logBase2(sectionWidth);
    }

    private double logBase2(int sectionWidth) {
        return Math.log(sectionWidth) / Math.log(2);
    }
}
