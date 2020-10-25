package the_fireplace.clans.legacy.cache;

import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WorldTrackingCache {
    private static final Map<BlockPos, Boolean> pistonPhases = new ConcurrentHashMap<>();

    public static boolean isTrackingPistonPhaseAt(BlockPos pos) {
        return pistonPhases.containsKey(pos);
    }

    public static void setPistonPhase(BlockPos pos, Boolean state) {
        pistonPhases.put(pos, state);
    }

    public static void invertPistonPhase(BlockPos pos) {
        pistonPhases.put(pos, !pistonPhases.get(pos));
    }

    public static Boolean getPistonPhase(BlockPos pos) {
        return pistonPhases.get(pos);
    }

    public static boolean removePistonPhase(BlockPos pos) {
        return pistonPhases.remove(pos);
    }
}
