package the_fireplace.clans.util;

import com.google.common.collect.Sets;
import the_fireplace.clans.clan.ClanChunkData;

import java.util.Set;
import java.util.UUID;

public class CoordNodeTree {
    private Set<Pair<Integer, Integer>> coordNodes = Sets.newConcurrentHashSet();

    public CoordNodeTree(int x, int z, UUID checkOwner) {
        for(ChunkPosition pos: ClanChunkData.getChunks(checkOwner))
            if(pos.posX != x || pos.posZ != z)
                coordNodes.add(new Pair<>(pos.posX, pos.posZ));
        if(!coordNodes.isEmpty())
            //noinspection unchecked
            activateNodeTree((Pair<Integer, Integer>) coordNodes.toArray()[0]);
    }

    public boolean hasDetachedNodes() {
        return !coordNodes.isEmpty();
    }

    private void activateNodeTree(Pair<Integer, Integer> node) {
        coordNodes.remove(node);
        for(Pair<Integer, Integer> node2: coordNodes)
            if(node2.getValue1() == node.getValue1() + 1 && node2.getValue2().equals(node.getValue2())
                    || node2.getValue1() == node.getValue1() - 1 && node2.getValue2().equals(node.getValue2())
                    || node2.getValue1().equals(node.getValue1()) && node2.getValue2() == node.getValue2() + 1
                    || node2.getValue1().equals(node.getValue1()) && node2.getValue2() == node.getValue2() - 1)
                activateNodeTree(node2);
    }
}
