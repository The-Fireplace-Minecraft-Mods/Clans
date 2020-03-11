package the_fireplace.clans.compat;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import the_fireplace.clans.abstraction.IProtectionCompat;

import javax.annotation.Nullable;

public class IC2ProtectionCompat implements IProtectionCompat {
    @Override
    public void init() {

    }

    @Override
    public boolean isMob(Entity entity) {
        return false;
    }

    @Override
    public boolean isContainer(World world, BlockPos pos, @Nullable IBlockState state, @Nullable TileEntity tileEntity) {
        if(state == null)
            state = world.getBlockState(pos);
        if(tileEntity == null)
            tileEntity = world.getTileEntity(pos);
        return false;
    }
}
