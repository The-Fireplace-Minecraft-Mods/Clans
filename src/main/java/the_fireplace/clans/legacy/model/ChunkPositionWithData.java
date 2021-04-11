package the_fireplace.clans.legacy.model;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.legacy.ClansModContainer;
import the_fireplace.clans.legacy.api.ClaimAccessor;
import the_fireplace.clans.legacy.data.ClaimData;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * A ChunkPosition with data attached. Note that because of the way these are stored and loaded, the attached data is wiped when a chunk is abandoned or changes owners. This is intentional.
 * Two ChunkPositions may have different data but the same x,z,d coordinates, and they will still be considered equal. This is also intentional.
 */
public class ChunkPositionWithData extends ChunkPosition {
    private boolean isBorderland;
    private Map<String, Object> addonData = Maps.newHashMap();

    public Map<String, Object> getAddonData() {
        return addonData;
    }

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
     * Sets addon data for this chunk
     * @param key
     * The key you are giving this data. It should be unique
     * @param value
     * The data itself. This should be a primitive, string, a list or map containg only lists/maps/primitives/strings, or a JsonElement. If not, your data may not save/load properly. All lists will be loaded as ArrayLists. All maps will be loaded as HashMaps.
     */
    public void setCustomData(String key, Object value) {
        if(!value.getClass().isPrimitive() && !value.getClass().isAssignableFrom(BigDecimal.class) && !value.getClass().isAssignableFrom(List.class) && !value.getClass().isAssignableFrom(Map.class) && !value.getClass().isAssignableFrom(JsonElement.class))
            ClansModContainer.getMinecraftHelper().getLogger().warn("Custom data may not be properly saved and loaded, as it is not assignable from any supported json deserialization. Key: {}, Value: {}", key, value);
        addonData.put(key, value);
        ClaimData.ClaimStoredData dat = ClaimData.INSTANCE.getChunkClaimData(this);
        if(dat != null)
            dat.markChanged();
    }

    @Nullable
    public Object getCustomData(String key) {
        return addonData.get(key);
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
            return getPosX() == pos.getPosX() && getPosZ() == pos.getPosZ() && getDim() == pos.getDim();
    }

    /**
     * @return Returns a unique hashcode for this object when comparing against other objects of this type.
     */
    @Override
    public int hashCode() {
        return 31 * (31 * this.getPosX() + this.getPosZ()) + this.getDim();
    }

    public ChunkPositionWithData(JsonObject obj){
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
     * @return this, for easy chaining
     */
    public ChunkPositionWithData retrieveCentralData() {
        ChunkPositionWithData data = ClaimAccessor.getInstance().getChunkPositionData(this);
        if(data != null) {
            this.isBorderland = data.isBorderland;
            this.addonData = data.addonData;
        }
        return this;
    }
}
