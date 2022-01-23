package dev.the_fireplace.clans.legacy.raid;

import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PistonPhaseTracker
{
    private static final Map<BlockPos, Boolean> PISTON_PHASES = new ConcurrentHashMap<>();

    public static boolean isTrackingPistonPhaseAt(BlockPos pos) {
        return PISTON_PHASES.containsKey(pos);
    }

    public static void setPistonPhase(BlockPos pos, Boolean state) {
        PISTON_PHASES.put(pos, state);
    }

    public static void invertPistonPhase(BlockPos pos) {
        PISTON_PHASES.put(pos, !PISTON_PHASES.get(pos));
    }

    public static Boolean getPistonPhase(BlockPos pos) {
        return PISTON_PHASES.get(pos);
    }

    public static boolean removePistonPhase(BlockPos pos) {
        return PISTON_PHASES.remove(pos);
    }
}
