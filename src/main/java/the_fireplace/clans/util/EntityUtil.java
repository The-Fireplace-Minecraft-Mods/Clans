package the_fireplace.clans.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntityUtil {

    @Nullable
    public static RayTraceResult getLookRayTrace(Entity rayTraceEntity, int distance) {
        Vec3d vec3d = rayTraceEntity.getPositionEyes(1);
        Vec3d vec3d1 = rayTraceEntity.getLook(1);
        Vec3d vec3d2 = vec3d.add(vec3d1.x * distance, vec3d1.y * distance, vec3d1.z * distance);
        return rayTraceEntity.world.rayTraceBlocks(vec3d, vec3d2, false, false, true);
    }

    @Nullable
    public static BlockPos getSafeLocation(World worldIn, BlockPos pos, int tries) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();

        for (int l = 0; l <= 1; ++l) {
            int i1 = i - Integer.compare(pos.getX(), 0) * l - 1;
            int j1 = k - Integer.compare(pos.getZ(), 0) * l - 1;
            int k1 = i1 + 2;
            int l1 = j1 + 2;

            for (int i2 = i1; i2 <= k1; ++i2) {
                for (int j2 = j1; j2 <= l1; ++j2) {
                    BlockPos blockpos = new BlockPos(i2, j, j2);

                    if (hasRoomForPlayer(worldIn, blockpos))
                        return blockpos;
                    else if(--tries <= 0)
                        return null;
                }
            }
        }

        return null;
    }

    public static boolean hasRoomForPlayer(World worldIn, BlockPos pos) {
        return worldIn.getBlockState(pos.down()).isSideSolid(worldIn, pos.down(), EnumFacing.UP) && !worldIn.getBlockState(pos).getMaterial().isSolid() && !worldIn.getBlockState(pos.up()).getMaterial().isSolid();
    }
}
