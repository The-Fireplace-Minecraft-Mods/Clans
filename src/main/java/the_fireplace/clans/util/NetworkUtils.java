package the_fireplace.clans.util;

import net.minecraft.block.state.IBlockState;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NetworkUtils {

    public static SPacketBlockChange createFakeBlockChange(World w, BlockPos pos, IBlockState state) {
        SPacketBlockChange pkt = new SPacketBlockChange(w, pos);
        pkt.blockState = state;
        return pkt;
    }
}
