package the_fireplace.clans.model;

import com.google.common.collect.Sets;
import the_fireplace.clans.data.ClaimData;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public class CoordNodeTree {
    private Set<OrderedPair<Integer, Integer>> initCoordNodes;
    private Set<OrderedPair<Integer, Integer>> coordNodes = Sets.newConcurrentHashSet();
    private Set<ChunkPositionWithData> borderChunks = Sets.newHashSet();
    private int dim;

    public CoordNodeTree(int excludeCoordX, int excludeCoordZ, int dim, UUID checkOwner) {
        for(ChunkPositionWithData pos: ClaimData.getClaimedChunks(checkOwner))
            if((pos.getPosX() != excludeCoordX || pos.getPosZ() != excludeCoordZ) && pos.getDim() == dim && !pos.isBorderland())
                coordNodes.add(new OrderedPair<>(pos.getPosX(), pos.getPosZ()));
        initCoordNodes = Collections.unmodifiableSet(coordNodes);
        this.dim = dim;
    }

    public CoordNodeTree(int dim, UUID checkOwner) {
        for(ChunkPositionWithData pos: ClaimData.getClaimedChunks(checkOwner))
            if(pos.getDim() == dim && !pos.isBorderland())
                coordNodes.add(new OrderedPair<>(pos.getPosX(), pos.getPosZ()));
        initCoordNodes = Collections.unmodifiableSet(coordNodes);
        this.dim = dim;
    }

    public CoordNodeTree forDisconnectionCheck() {
        if(!coordNodes.isEmpty())
            //noinspection unchecked
            removeAllConnected((OrderedPair<Integer, Integer>) coordNodes.toArray()[0]);
        return this;
    }

    public CoordNodeTree forBorderlandRetrieval() {
        for(OrderedPair<Integer, Integer> node: coordNodes)
            checkNodeBorders(node, dim);
        return this;
    }

    public Set<ChunkPositionWithData> getBorderChunks() {
        return borderChunks;
    }

    public boolean hasDetachedNodes() {
        return !coordNodes.isEmpty();
    }

    private void removeAllConnected(OrderedPair<Integer, Integer> node) {
        coordNodes.remove(node);
        for(OrderedPair<Integer, Integer> node2: coordNodes)
            if(isAdjacent(node, node2))
                removeAllConnected(node2);
    }

    private void checkNodeBorders(OrderedPair<Integer, Integer> node, int dim) {
        if(!initCoordNodes.contains(new OrderedPair<>(node.getValue1() + 1, node.getValue2())))
            borderChunks.add(new ChunkPositionWithData(node.getValue1() + 1, node.getValue2(), dim).setIsBorderland());
        if(!initCoordNodes.contains(new OrderedPair<>(node.getValue1() - 1, node.getValue2())))
            borderChunks.add(new ChunkPositionWithData(node.getValue1() - 1, node.getValue2(), dim).setIsBorderland());
        if(!initCoordNodes.contains(new OrderedPair<>(node.getValue1(), node.getValue2() + 1)))
            borderChunks.add(new ChunkPositionWithData(node.getValue1(), node.getValue2() + 1, dim).setIsBorderland());
        if(!initCoordNodes.contains(new OrderedPair<>(node.getValue1(), node.getValue2() - 1)))
            borderChunks.add(new ChunkPositionWithData(node.getValue1(), node.getValue2() - 1, dim).setIsBorderland());
    }

    private boolean isAdjacent(OrderedPair<Integer, Integer> home, OrderedPair<Integer, Integer> checkAdjacentToHome) {
        return checkAdjacentToHome.getValue1() == home.getValue1() + 1 && checkAdjacentToHome.getValue2().equals(home.getValue2())
                || checkAdjacentToHome.getValue1() == home.getValue1() - 1 && checkAdjacentToHome.getValue2().equals(home.getValue2())
                || checkAdjacentToHome.getValue1().equals(home.getValue1()) && checkAdjacentToHome.getValue2() == home.getValue2() + 1
                || checkAdjacentToHome.getValue1().equals(home.getValue1()) && checkAdjacentToHome.getValue2() == home.getValue2() - 1;
    }
}
