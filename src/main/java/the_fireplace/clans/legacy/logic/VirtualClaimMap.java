package the_fireplace.clans.legacy.logic;

import net.minecraft.util.math.ChunkPos;
import the_fireplace.clans.legacy.data.ClaimData;
import the_fireplace.clans.legacy.model.OrderedPair;

public abstract class VirtualClaimMap {
    public static final String CACHE_SEGMENT_SEPARATOR = "|";
    private ChunkPos centerChunk;
    protected int width;
    protected int height;

    public VirtualClaimMap() {
        this.height = this.width = ClaimData.CACHE_SECTION_SIZE;
    }

    protected ChunkPos calculateCenter() {
        int centerOffsetX = getCenterOffsetX();
        int centerOffsetZ = getCenterOffsetZ();
        return new ChunkPos(getCacheSegment().getValue1() * ClaimData.CACHE_SECTION_SIZE + centerOffsetX, getCacheSegment().getValue2() * ClaimData.CACHE_SECTION_SIZE + centerOffsetZ);
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

    protected abstract OrderedPair<Integer, Integer> getCacheSegment();

    protected OrderedPair<Byte, Byte> getQuadrant() {
        return new OrderedPair<>((byte) Math.copySign(1, getCacheSegment().getValue1()), (byte) Math.copySign(1, getCacheSegment().getValue2()));
    }

    public ChunkPos getCenterChunk() {
        if (centerChunk == null)
            centerChunk = calculateCenter();
        return centerChunk;
    }

    protected int getMinX() {
        byte offset = getQuadrantXOffset();
        return getCenterChunk().x - width/2 + offset;
    }

    protected int getMaxX() {
        byte xOff = getQuadrantXOffset();
        return getCenterChunk().x + width/2 + xOff;
    }

    protected byte getQuadrantXOffset() {
        return (byte) (getQuadrant().getValue1() < 0 ? -1 : 0);
    }

    protected int getMinZ() {
        return getCenterChunk().z - height/2 + getQuadrantZOffset();
    }

    protected int getMaxZ() {
        return getCenterChunk().z + height/2 + getQuadrantZOffset();
    }

    protected byte getQuadrantZOffset() {
        return (byte) (getQuadrant().getValue2() < 0 ? -1 : 0);
    }
}
