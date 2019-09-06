package the_fireplace.clans.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

public class EntityUtil {

    @Nullable
    public static RayTraceResult getLookRayTrace(Entity rayTraceEntity, int distance) {
        Vec3d vec3d = rayTraceEntity.getPositionEyes(1);
        Vec3d vec3d1 = rayTraceEntity.getLook(1);
        Vec3d vec3d2 = vec3d.add(vec3d1.x * distance, vec3d1.y * distance, vec3d1.z * distance);
        return rayTraceEntity.world.rayTraceBlocks(vec3d, vec3d2, false, false, true);
    }
}
