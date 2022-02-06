package dev.the_fireplace.clans.domain.compat;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.UUID;

public interface ProtectionCompat
{
    void init();

    /**
     * Is the entity able to be owned by a player? Should ONLY be used for mobs that don't extend {@link net.minecraft.entity.passive.TameableEntity}
     * See {@link LandProtectionLogic#isOwnable(Entity)} for things that get checked before this.
     */
    boolean isOwnable(Entity entity);

    /**
     * Get the id of the entity's owner. Should ONLY be used for mobs that don't extend {@link net.minecraft.entity.passive.TameableEntity}
     * See {@link LandProtectionLogic#getOwnerId(Entity)} for things that get checked before this.
     */
    @Nullable
    UUID getOwnerId(Entity entity);

    /**
     * Is the entity a hostile mob? Should ONLY be used for mobs that don't extend {@link net.minecraft.entity.mob.Monster}
     * See {@link LandProtectionLogic#isMob(Entity)} for things that get checked before this.
     */
    boolean isMob(Entity entity);

    /**
     * Is the target block a container? Should ONLY be used for containers that don't extend {@link net.minecraft.block.AbstractChestBlock} and whose TileEntities don't implement {@link net.minecraft.inventory.Inventory}.
     * See {@link LandProtectionLogic#isContainer(World, BlockPos, BlockState, BlockEntity)} for things that get checked before this.
     */
    boolean isContainer(World world, BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity tileEntity);
}
