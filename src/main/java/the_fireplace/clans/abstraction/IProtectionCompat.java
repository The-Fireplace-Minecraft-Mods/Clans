package the_fireplace.clans.abstraction;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public interface IProtectionCompat {
    void init();
    boolean isMob(Entity entity);

    /**
     * Is the target block a container? Should ONLY be used for containers that don't extend BlockContainer and whose TileEntities don't implement IInventory.
     * See {@link the_fireplace.clans.logic.LandProtectionEventLogic#isContainer(World, BlockPos, IBlockState, TileEntity)} for things that get checked before this.
     */
    boolean isContainer(World world, BlockPos pos, @Nullable IBlockState state, @Nullable TileEntity tileEntity);
}
