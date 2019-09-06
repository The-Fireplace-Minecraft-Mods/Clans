package the_fireplace.clans.util;

import com.google.common.collect.Sets;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Set;

public class MultiblockUtil {
    public static Set<BlockPos> getConnectedPositions(World world, BlockPos pos, @Nullable IBlockState state) {
        Set<BlockPos> connected = Sets.newHashSet();
        if(state == null)//This extra call can be avoided by passing in the state, which I imagine most use cases will already have available
            state = world.getBlockState(pos);
        if(state.getProperties().containsKey(BlockBed.PART) && state.getProperties().containsKey(BlockBed.FACING))
            connected.add(state.getValue(BlockBed.PART) == BlockBed.EnumPartType.HEAD ? pos.offset(state.getValue(BlockBed.FACING).getOpposite()) : pos.offset(state.getValue(BlockBed.FACING)));
        if(state.getProperties().containsKey(BlockDoor.HALF))
            connected.add(state.getValue(BlockDoor.HALF) == BlockDoor.EnumDoorHalf.LOWER ? pos.offset(EnumFacing.UP) : pos.offset(EnumFacing.DOWN));
        if(state.getProperties().containsKey(BlockPistonBase.EXTENDED) && state.getProperties().containsKey(BlockDirectional.FACING) && state.getValue(BlockPistonBase.EXTENDED))
            connected.add(pos.offset(state.getValue(BlockPistonBase.FACING)));
        if(state.getBlock() instanceof BlockPistonExtension && state.getProperties().containsKey(BlockDirectional.FACING))
            connected.add(pos.offset(state.getValue(BlockDirectional.FACING).getOpposite()));
        //TODO should connected chests be included? In some scenarios, such as locking, the answer is yes because you want both parts to lock. In others, such as raid rollback calculations, the answer is no because they are not dependent on each other (If you break one, the other stays)
        //TODO a method is also needed to get dependent positions, such as if you break a block that has a sign or torch attached to it. How does that impact the above TODO?

        return connected;
    }
}
