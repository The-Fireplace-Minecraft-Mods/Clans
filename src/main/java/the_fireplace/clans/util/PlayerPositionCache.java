package the_fireplace.clans.util;

import com.google.common.collect.Maps;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.HashMap;

public class PlayerPositionCache {
    //These three are used for the chunk claim warning
    public static HashMap<EntityPlayer, Integer> prevYs = Maps.newHashMap();
    public static HashMap<EntityPlayer, Integer> prevChunkXs = Maps.newHashMap();
    public static HashMap<EntityPlayer, Integer> prevChunkZs = Maps.newHashMap();

    public static HashMap<EntityPlayerMP, Pair<Integer, Integer>> clanHomeWarmups = Maps.newHashMap();
}
