package dev.the_fireplace.clans.legacy.logic;

import dev.the_fireplace.clans.legacy.model.OrderedPair;
import net.minecraft.util.math.ChunkPos;

public abstract class VirtualClaimMap
{
    // This should be divisible by 7 and not exceed 53.
    // Divisible by 7 so the smaller map can take exactly a seventh of the section.
    // 53 map-width characters is all the chat window can fit before going to a new line.
    // 49 is ideal because it is the largest number that fits those conditions.
    public static final byte MAP_SIZE = 49;
    public static final String CACHE_SEGMENT_SEPARATOR = "|";
    private ChunkPos centerChunk;
    protected int width;
    protected int height;

    public VirtualClaimMap() {
        this.height = this.width = MAP_SIZE;
    }

    protected ChunkPos calculateCenter() {//TODO something isn't right here
        int centerOffsetX = getCenterOffsetX();
        int centerOffsetZ = getCenterOffsetZ();
        int chunkX = getMapSegment().getValue1() * MAP_SIZE + centerOffsetX;
        int chunkZ = getMapSegment().getValue2() * MAP_SIZE + centerOffsetZ;
        return new ChunkPos(chunkX, chunkZ);
    }

    protected int getCenterOffsetX() {
        return getCenterOffset(width) * getQuadrant().getValue1();
    }

    protected int getCenterOffsetZ() {
        return getCenterOffset(height) * getQuadrant().getValue2();
    }

    protected int getCenterOffset(int size) {
        return size / 2;
    }

    protected abstract OrderedPair<Integer, Integer> getMapSegment();

    protected OrderedPair<Byte, Byte> getQuadrant() {
        return new OrderedPair<>((byte) Math.copySign(1, getMapSegment().getValue1()), (byte) Math.copySign(1, getMapSegment().getValue2()));
    }

    public ChunkPos getCenterChunk() {
        if (centerChunk == null) {
            centerChunk = calculateCenter();
        }
        return centerChunk;
    }

    protected int getMinX() {
        byte offset = getQuadrantXOffset();
        return getCenterChunk().x - width / 2 + offset;
    }

    protected int getMaxX() {
        byte xOff = getQuadrantXOffset();
        return getCenterChunk().x + width / 2 + xOff;
    }

    protected byte getQuadrantXOffset() {
        return (byte) (getQuadrant().getValue1() < 0 ? -1 : 0);
    }

    protected int getMinZ() {
        return getCenterChunk().z - height / 2 + getQuadrantZOffset();
    }

    protected int getMaxZ() {
        return getCenterChunk().z + height / 2 + getQuadrantZOffset();
    }

    protected byte getQuadrantZOffset() {
        return (byte) (getQuadrant().getValue2() < 0 ? -1 : 0);
    }
}
