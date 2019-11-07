package the_fireplace.clans.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import the_fireplace.clans.Clans;
import the_fireplace.clans.data.PlayerData;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import java.util.Objects;

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

    public static void teleportHome(EntityPlayer player, BlockPos home, int homeDim, int playerDim, boolean noCooldown) {
        home = getSafeLocation(Objects.requireNonNull(player.getServer()).getWorld(homeDim), home, 5);
        if (playerDim == homeDim) {
            completeTeleportHome(player, home, playerDim, noCooldown);
        } else {
            player.setPortal(player.getPosition());
            if (player.changeDimension(homeDim) != null) {
                completeTeleportHome(player, home, playerDim, noCooldown);
            } else {
                player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "commands.clan.home.dim_error").setStyle(TextStyles.RED));
            }
        }
    }

    private static void completeTeleportHome(EntityPlayer player, @Nullable BlockPos home, int originDim, boolean noCooldown) {
        if (home == null || !player.attemptTeleport(home.getX(), home.getY(), home.getZ())) {
            player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "commands.clan.home.blocked").setStyle(TextStyles.RED));
            if (originDim != player.dimension && player.changeDimension(originDim) == null)
                player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "commands.clan.home.return_dim").setStyle(TextStyles.RED));
        } else if(!noCooldown)
            PlayerData.setCooldown(player.getUniqueID(), Clans.getConfig().getClanHomeCooldownTime());
    }
}
