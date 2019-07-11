package the_fireplace.clans.model;

import com.google.gson.JsonObject;

public class ChunkPosition {
    public final int posX;
    public final int posZ;
    public final int dim;

    public ChunkPosition(int x, int z, int d) {
        this.posX = x;
        this.posZ = z;
        this.dim = d;
    }

    /**
     * @return Return the real X position of the chunk in blocks
     */
    public int getRealPosX() {
        return (this.posX << 4);
    }

    /**
     * @return Return the real Z position of the chunk in blocks
     */
    public int getRealPosZ() {
        return (this.posZ << 4);
    }

    /**
     * Compares the object to this object
     * @param obj The object to compare this object too
     * @return Returns true if the object is the same type and the contents match.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        else if (obj == this)
            return true;
        else
            return obj instanceof ChunkPosition && this.equals((ChunkPosition) obj);
    }

    /**
     * Compares another ChunkPosition with this one.
     * @param pos The other ChunkPosition to compare against
     * @return Returns true if the Chunk is in the same position and dimension.
     */
    private boolean equals(ChunkPosition pos) {
        if (pos == null)
            return false;
        else
            return posX == pos.posX && posZ == pos.posZ && dim == pos.dim;
    }

    /**
     * @return Returns a unique hashcode for this object when comparing against other objects of this type.
     */
    @Override
    public int hashCode() {
        return 31 * (31 * this.posX + this.posZ) + this.dim;
    }

    public JsonObject toJsonObject() {
        JsonObject ret = new JsonObject();
        ret.addProperty("posX", posX);
        ret.addProperty("posZ", posZ);
        ret.addProperty("dim", dim);

        return ret;
    }

    public ChunkPosition(JsonObject obj){
        this.dim = obj.get("dim").getAsInt();
        this.posX = obj.get("posX").getAsInt();
        this.posZ = obj.get("posZ").getAsInt();
    }
}
