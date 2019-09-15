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
    /**
     * Get all positions that are irrefutably connected to the one you pass in. Halves of beds, extended pistons, and doors should all have these.
     */
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

        return connected;
    }

    public static Set<BlockPos> getLockingConnectedPositions(World world, BlockPos pos, @Nullable IBlockState state) {
        Set<BlockPos> connected = getConnectedPositions(world, pos, state);
        if(state == null)//This extra call can be avoided by passing in the state, which I imagine most use cases will already have available
            state = world.getBlockState(pos);
        //TODO include connected chests

        return connected;
    }

    public static Set<BlockPos> getDependentPositions(World world, BlockPos pos, @Nullable IBlockState state) {
        Set<BlockPos> dependent = getConnectedPositions(world, pos, state);
        if(state == null)//This extra call can be avoided by passing in the state, which I imagine most use cases will already have available
            state = world.getBlockState(pos);
        //TODO include other dependent positions, such as signs, levers, buttons, pressure plates, torches, crops, etc that will break if the given position is broken.

        return dependent;
    }
}
