package the_fireplace.clans.model;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import net.minecraft.world.chunk.Chunk;

import java.util.HashMap;

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

    public ChunkPositionWithData(JsonObject obj) {
        super(obj);
    }

    public ChunkPositionWithData offset(int x, int z) {
        return new ChunkPositionWithData(posX+x, posZ+z, dim);
    }
}
