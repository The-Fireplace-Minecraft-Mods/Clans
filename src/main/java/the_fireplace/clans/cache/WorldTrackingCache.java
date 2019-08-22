package the_fireplace.clans.cache;

import com.google.common.collect.Maps;
import net.minecraft.util.math.BlockPos;

import java.util.Map;

public class WorldTrackingCache {
    public static Map<BlockPos, Boolean> pistonPhases = Maps.newHashMap();
}
