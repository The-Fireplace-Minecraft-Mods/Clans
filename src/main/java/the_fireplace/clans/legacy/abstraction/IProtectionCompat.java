package the_fireplace.clans.legacy.abstraction;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import the_fireplace.clans.legacy.logic.LandProtectionLogic;

import javax.annotation.Nullable;
import java.util.UUID;

public interface IProtectionCompat {
    void init();

    /**
     * Is the entity able to be owned by a player? Should ONLY be used for mobs that don't extend {@link net.minecraft.entity.passive.EntityTameable}
     * See {@link LandProtectionLogic#isOwnable(Entity)} for things that get checked before this.
     */
    boolean isOwnable(Entity entity);

    /**
     * Get the id of the entity's owner. Should ONLY be used for mobs that don't extend {@link net.minecraft.entity.passive.EntityTameable}
     * See {@link LandProtectionLogic#getOwnerId(Entity)} for things that get checked before this.
     */
    @Nullable
    UUID getOwnerId(Entity entity);

    /**
     * Is the entity a hostile mob? Should ONLY be used for mobs that don't implement {@link net.minecraft.entity.monster.IMob}
     * See {@link LandProtectionLogic#isMob(Entity)} for things that get checked before this.
     */
    boolean isMob(Entity entity);

    /**
     * Is the target block a container? Should ONLY be used for containers that don't extend {@link net.minecraft.block.BlockContainer} and whose TileEntities don't implement {@link net.minecraft.inventory.IInventory}.
     * See {@link LandProtectionLogic#isContainer(World, BlockPos, IBlockState, TileEntity)} for things that get checked before this.
     */
    boolean isContainer(World world, BlockPos pos, @Nullable IBlockState state, @Nullable TileEntity tileEntity);
}
