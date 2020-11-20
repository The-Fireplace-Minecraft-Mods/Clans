package the_fireplace.clans.client.mapprocessing;

import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import the_fireplace.clans.legacy.logic.VirtualClaimMap;
import the_fireplace.clans.legacy.model.ChunkPosition;
import the_fireplace.clans.legacy.model.OrderedPair;

import java.util.Map;

public class ContentLineProcessor extends VirtualClaimMap {

    private final int cacheX, cacheZ;
    private final String line;
    private final byte lineNumber;

    public ContentLineProcessor(int cacheX, int cacheZ, String line, byte lineNumber) {
        super();
        this.cacheX = cacheX;
        this.cacheZ = cacheZ;
        this.line = line;
        this.lineNumber = lineNumber;
    }

    @Override
    protected OrderedPair<Integer, Integer> getCacheSegment() {
        return new OrderedPair<>(cacheX, cacheZ);
    }

    public Map<ChunkPosition, Character> getCharacterPositions() {
        Map<ChunkPosition, Character> positionCharacters = Maps.newHashMap();

        int dim = Minecraft.getMinecraft().player.dimension;
        int topZ = Math.max(getMinZ(), getMaxZ());
        int z = getLineZ(topZ);
        int leftX = Math.min(getMinX(), getMaxX());

        for (int x=0; x<line.length(); x++) {
            positionCharacters.put(new ChunkPosition(leftX+x, z, dim), line.toCharArray()[x]);
        }

        return positionCharacters;
    }

    private int getLineZ(int topZ) {
        return topZ - lineNumber;
    }
}
