package the_fireplace.clans.sponge;

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.util.math.BlockPos;

public class SpongeHelper {
    public static BlockPos getPos(Vector3i vector3i) {
        return new BlockPos(vector3i.getX(), vector3i.getY(), vector3i.getZ());
    }
}
