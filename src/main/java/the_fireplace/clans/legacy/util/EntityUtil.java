package the_fireplace.clans.legacy.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.apache.commons.lang3.ArrayUtils;
import the_fireplace.clans.clan.membership.ClanMembers;
import the_fireplace.clans.legacy.ClansModContainer;
import the_fireplace.clans.legacy.api.ClaimAccessor;
import the_fireplace.clans.legacy.model.ChunkPosition;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;
import the_fireplace.clans.player.PlayerHomeCooldown;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

public class EntityUtil
{

    @Nullable
    public static RayTraceResult getLookRayTrace(Entity rayTraceEntity, int distance) {
        Vec3d vec3d = rayTraceEntity.getPositionEyes(1);
        Vec3d vec3d1 = rayTraceEntity.getLook(1);
        Vec3d vec3d2 = vec3d.add(vec3d1.x * distance, vec3d1.y * distance, vec3d1.z * distance);
        return rayTraceEntity.world.rayTraceBlocks(vec3d, vec3d2, false, false, true);
    }

    @Nullable
    public static BlockPos getSafeLocation(World worldIn, BlockPos pos) {
        int posX = pos.getX();
        int posY = pos.getY();
        int posZ = pos.getZ();

        for (int xOffset = -2; xOffset <= 2; ++xOffset) {
            for (int yOffset = -2; yOffset <= 2; ++yOffset) {
                for (int zOffset = -2; zOffset <= 2; ++zOffset) {
                    BlockPos blockpos = new BlockPos(posX + xOffset, posY + yOffset, posZ + zOffset);

                    if (canPlayerTeleportTo(worldIn, blockpos)) {
                        return blockpos;
                    }
                }
            }
        }

        return null;
    }

    public static boolean canPlayerTeleportTo(World worldIn, BlockPos pos) {
        return (canPlayerStandOn(worldIn, pos.down()) || canPlayerStandOn(worldIn, pos.down(2)) || canPlayerStandOn(worldIn, pos.down(3)))
            && hasRoomForPlayer(worldIn, pos);
    }

    private static boolean canPlayerStandOn(World worldIn, BlockPos pos) {
        return worldIn.getBlockState(pos).isSideSolid(worldIn, pos, EnumFacing.UP);
    }

    private static boolean hasRoomForPlayer(World worldIn, BlockPos pos) {
        return !worldIn.getBlockState(pos).getMaterial().isSolid()
            && !worldIn.getBlockState(pos.up()).getMaterial().isSolid();
    }

    public static void teleportHome(EntityPlayer player, BlockPos home, int homeDim, int playerDim, boolean noCooldown) {
        home = getSafeLocation(Objects.requireNonNull(player.getServer()).getWorld(homeDim), home);
        if (playerDim == homeDim) {
            completeTeleportHome(player, home, playerDim, noCooldown);
        } else {
            if (home != null) {
                player.setPortal(home);
            } else {
                player.setPortal(player.getPosition());
            }
            try {//Use try/catch because the teleporter occasionally throws a NPE when going to/from dimensions without a portal. If this becomes too frequent, try to make a custom teleporter that won't throw NPEs.
                if (player.changeDimension(homeDim) != null) {
                    completeTeleportHome(player, home, playerDim, noCooldown);
                    return;
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "commands.clan.home.dim_error").setStyle(TextStyles.RED));
        }
    }

    private static void completeTeleportHome(EntityPlayer player, @Nullable BlockPos home, int originDim, boolean noCooldown) {
        if (home == null || !player.attemptTeleport(home.getX(), home.getY(), home.getZ())) {
            player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "commands.clan.home.blocked").setStyle(TextStyles.RED));
            if (originDim != player.dimension && player.changeDimension(originDim) == null) {
                player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "commands.clan.home.return_dim").setStyle(TextStyles.RED));
            }
        } else if (!noCooldown) {
            PlayerHomeCooldown.setCooldown(player.getUniqueID(), ClansModContainer.getConfig().getClanHomeCooldownTime());
        }
    }

    public static boolean teleportSafelyToChunk(EntityPlayer player, Chunk chunk) {
        BlockPos center = new BlockPos((chunk.getPos().getXStart() + chunk.getPos().getXEnd()) / 2f, chunk.getHeight(new BlockPos((chunk.getPos().getXStart() + chunk.getPos().getXEnd()) / 2f, 0, (chunk.getPos().getZStart() + chunk.getPos().getZEnd()) / 2f)), (chunk.getPos().getZStart() + chunk.getPos().getZEnd()) / 2f);
        center = getSafeLocation(chunk.getWorld(), center);
        if (center == null) {
            return false;
        }
        int chunkDim = chunk.getWorld().provider.getDimension();
        if (player.dimension != chunkDim) {
            player.setPortal(player.getPosition());
            if (player.changeDimension(chunkDim) == null) {
                return false;
            }
        }
        return player.attemptTeleport(center.getX(), center.getY(), center.getZ());
    }

    public static Chunk findSafeChunkFor(EntityPlayerMP player, ChunkPosition origin, boolean excludeOrigin) {
        int x = 0, z = 0, tmp, dx = 0, dz = -1;
        while (true) {//Spiral out until a player friendly chunk is found
            ChunkPosition test = new ChunkPosition(origin.getPosX() + x, origin.getPosZ() + z, origin.getDim());
            UUID testChunkOwner = ClaimAccessor.getInstance().getChunkClan(test);
            if ((testChunkOwner == null || ClanMembers.get(testChunkOwner).isMember(player.getUniqueID())) && (!excludeOrigin || !test.equals(origin))) {
                return ClansModContainer.getMinecraftHelper().getServer().getWorld(origin.getDim()).getChunk(test.getPosX(), test.getPosZ());
            }
            if (x == z || (x < 0 && x == -z) || (x > 0 && x == 1 - z)) {
                tmp = dx;
                dx = -dz;
                dz = tmp;
            }
            x += dx;
            z += dz;
        }
    }

    public static Chunk findSafeChunkFor(EntityPlayerMP player, ChunkPosition origin) {
        return findSafeChunkFor(player, origin, false);
    }

    /**
     * Try to find the source entity for an entity. Intended to be run on projectiles to find out who launched/shot/threw them
     *
     * @return the source of the entity or null if a source was not found
     */
    @Nullable
    public static Entity tryFindSource(Entity entity) {
        if (entity instanceof EntityArrow) {
            return ((EntityArrow) entity).shootingEntity;
        }
        if (entity instanceof EntityThrowable) {
            return ((EntityThrowable) entity).getThrower();
        }
        if (entity instanceof EntityFireball) {
            return ((EntityFireball) entity).shootingEntity;
        }
        if (entity instanceof EntityLlamaSpit) {
            try {
                return ((EntityLlamaSpit) entity).owner;
            } catch (NoSuchFieldError e) {//Work around the field being missing sometimes, which appears to be caused by Mohist and Magma.
                ClansModContainer.getLogger().error("Llama spit owner field missing! EntityLlamaSpit's fields:");
                ClansModContainer.getLogger().error(ArrayUtils.toString(EntityLlamaSpit.class.getFields()));
                ClansModContainer.getLogger().error(e.getMessage());
                return null;
            }
        }
        if (entity instanceof EntityFishHook) {
            return ((EntityFishHook) entity).getAngler();
        }
        return null;
    }
}
