package the_fireplace.clans.model;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.Clans;
import the_fireplace.clans.data.ClaimDataManager;
import the_fireplace.clans.util.JsonHelper;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChunkPositionWithData extends ChunkPosition {
    private HashMap<String, Object> addonData = Maps.newHashMap();

    public HashMap<String, Object> getAddonData() {
        return addonData;
    }

    public ChunkPositionWithData(int x, int z, int d) {
        super(x, z, d);
    }

    public ChunkPositionWithData(Chunk c) {
        super(c);
    }

    public ChunkPositionWithData offset(int x, int z) {
        return new ChunkPositionWithData(getPosX() +x, getPosZ() +z, getDim());
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
            Clans.getMinecraftHelper().getLogger().warn("Custom data may not be properly saved and loaded, as it is not assignable from any supported json deserialization. Key: {}, Value: {}", key, value);
        addonData.put(key, value);
        ClaimDataManager.ClanClaimData dat = ClaimDataManager.getChunkClaimData(this);
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

    @Override
    public JsonObject toJsonObject() {
        JsonObject ret = super.toJsonObject();
        JsonHelper.attachAddonData(ret, addonData);

        return ret;
    }

    public ChunkPositionWithData(JsonObject obj){
        super(obj);
        addonData = JsonHelper.getAddonData(obj);
    }
}
