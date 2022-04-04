package dev.the_fireplace.clans.legacy.model;

import com.google.gson.JsonObject;
import dev.the_fireplace.clans.legacy.api.ClaimAccessor;
import net.minecraft.world.chunk.Chunk;

/**
 * A ChunkPosition with data attached. Note that because of the way these are stored and loaded, the attached data is wiped when a chunk is abandoned or changes owners. This is intentional.
 * Two ChunkPositions may have different data but the same x,z,d coordinates, and they will still be considered equal. This is also intentional.
 */
public class ChunkPositionWithData extends ChunkPosition
{
    private boolean isBorderland;

    public ChunkPositionWithData(int x, int z, int d) {
        super(x, z, d);
    }

    public ChunkPositionWithData(ChunkPosition position) {
        super(position.getPosX(), position.getPosZ(), position.getDim());
    }

    public ChunkPositionWithData(Chunk c) {
        super(c);
    }

    public ChunkPositionWithData offset(int x, int z) {
        return new ChunkPositionWithData(getPosX() + x, getPosZ() + z, getDim());
    }

    /**
     * Compares the object to this object
     *
     * @param obj The object to compare this object too
     * @return Returns true if the object is the same type and the contents match.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj == this) {
            return true;
        } else {
            return obj instanceof ChunkPosition && this.equals((ChunkPosition) obj);
        }
    }

    /**
     * Compares another ChunkPosition with this one.
     *
     * @param pos The other ChunkPosition to compare against
     * @return Returns true if the Chunk is in the same position and dimension.
     */
    private boolean equals(ChunkPosition pos) {
        if (pos == null) {
            return false;
        } else {
            return getPosX() == pos.getPosX() && getPosZ() == pos.getPosZ() && getDim() == pos.getDim();
        }
    }

    /**
     * @return Returns a unique hashcode for this object when comparing against other objects of this type.
     */
    @Override
    public int hashCode() {
        return 31 * (31 * this.getPosX() + this.getPosZ()) + this.getDim();
    }

    public ChunkPositionWithData(JsonObject obj) {
        super(obj);
    }

    public boolean isBorderland() {
        return isBorderland;
    }

    public void setBorderland(boolean borderland) {
        isBorderland = borderland;
    }

    /**
     * Marks the chunk as borderland. Designed for easy daisy chaining.
     */
    public ChunkPositionWithData setIsBorderland() {
        isBorderland = true;
        return this;
    }

    /**
     * Check if data is in the ClaimDataManager for this position, and copy it over if it is
     *
     * @return this, for easy chaining
     */
    public ChunkPositionWithData retrieveCentralData() {
        ChunkPositionWithData data = ClaimAccessor.getInstance().getChunkPositionData(this);
        if (data != null) {
            this.isBorderland = data.isBorderland;
        }
        return this;
    }
}
