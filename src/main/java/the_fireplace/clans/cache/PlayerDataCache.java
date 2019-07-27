package the_fireplace.clans.cache;

import com.google.common.collect.Maps;
import net.minecraft.entity.player.EntityPlayerMP;
import the_fireplace.clans.model.OrderedPair;

import java.util.HashMap;

public class PlayerDataCache {
    public static HashMap<EntityPlayerMP, OrderedPair<Integer, Integer>> clanHomeWarmups = Maps.newHashMap();
}
