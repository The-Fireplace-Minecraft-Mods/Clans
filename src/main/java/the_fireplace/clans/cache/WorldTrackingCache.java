package the_fireplace.clans.cache;

import com.google.common.collect.Maps;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;

public class WorldTrackingCache {
    public static HashMap<BlockPos, Boolean> pistonPhases = Maps.newHashMap();
}
