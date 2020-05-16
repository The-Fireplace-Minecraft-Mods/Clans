package the_fireplace.clans.model;

import com.google.common.collect.Maps;
import the_fireplace.clans.Clans;

import java.util.*;

public class GroupedChunks {
    private final Map<ChunkPosition, AdjacentChunk> chunkGroupMap = Maps.newHashMap();

    /**
     * Takes the info for the position of the current frame and finds all the chunks that are adjacent to it and
     * then to each other. Eventually building up a list of all chunks that are related to each other in a group.
     *
     * @param chunkPos The starting chunk to processes for this group
     * @param remainingChunksToProcess The list of remaining claim chunks to check for adjacent chunks.
     */
    public void processChunk(ChunkPosition chunkPos, Set<ChunkPosition> remainingChunksToProcess) {
        AdjacentChunk chunk = new AdjacentChunk(chunkPos);

        // Once we process a claim chunk remove it from the getClanID list and add it to the group map
        remainingChunksToProcess.remove(chunkPos);
        chunkGroupMap.put(chunkPos, chunk);

        // Have each chunk find its adjacent chunks and process them recursively
        chunk.processAdjacentChunks(remainingChunksToProcess, chunkGroupMap);
    }

    /**
     * Assembles in memory the group of chunks and edges for each chunk and traces the outer perimeter of the
     * group of chunks in a clockwise pattern.
     *
     * NOTE:
     * Chunks not present in the middle of the group of chunks are just ignored and just rendered as full claim
     * areas in dynmap.
     *
     * @return Returns a list of points in a path to draw the perimeter of the claim chunk.
     */
    public List<CoordinatePair> traceShapePerimeter() {
        ChunkEdge startEdge = null;
        int nTotalEdgeCount = 0;

        Map<CoordinatePair, List<ChunkEdge>> pointSearchMap = new HashMap<>();

        //Loop through all the chunks and get a list of open edges.
        for (Map.Entry<ChunkPosition, AdjacentChunk> chunk : chunkGroupMap.entrySet()) {
            List<ChunkEdge> edges = chunk.getValue().getOpenChunkEdges();
            for (ChunkEdge edge : edges) {
                // Keep track of an edge that is the lowest X position so we can use it as a starting point.
                if (startEdge == null || edge.point1().getX() < startEdge.point1().getX())
                    startEdge = edge;

                nTotalEdgeCount++;

                // Put the edge point 1's in to a map so we can find the point we are after easily
                if (pointSearchMap.containsKey(edge.point1())) {
                    List<ChunkEdge> entry = pointSearchMap.get(edge.point1());
                    entry.add(edge);
                } else {
                    List<ChunkEdge> entry = new ArrayList<>();
                    entry.add(edge);
                    pointSearchMap.put(edge.point1(), entry);
                }
            }
        }

        // Build a list of perimeter points by finding the next edge moving clockwise around the edges.
        // Once we get back to the starting edge we are done.
        List<CoordinatePair> perimeterPoints = new ArrayList<>();

        boolean bTraceError = false;
        if (!pointSearchMap.isEmpty()) {
            ChunkEdge curEdge = startEdge;

            ChunkEdge.Edge lastEdgeType = null;
            int nLoopLimit = nTotalEdgeCount + 5;
            do {
                nLoopLimit--;
                List<ChunkEdge> edges = pointSearchMap.get(curEdge.point2());

                if (edges != null) {
                    // If we found just 1 point that matches the coordinates then just use it.
                    if (edges.size() == 1) {
                        curEdge = edges.get(0);
                    } else {
                        // If we find more than one point that matches the coordinate, then we always want to
                        // turn right, so take the current edge and determine what the next edge to the right would
                        // be.
                        ChunkEdge.Edge nextEdge = ChunkEdge.Edge.LEFT;

                        if (lastEdgeType == null)
                            lastEdgeType = curEdge.edgeType();

                        switch (lastEdgeType) {
                            case LEFT:
                                nextEdge = ChunkEdge.Edge.TOP;
                                break;
                            case RIGHT:
                                nextEdge = ChunkEdge.Edge.BOTTOM;
                                break;
                            case TOP:
                                nextEdge = ChunkEdge.Edge.RIGHT;
                                break;
                            case BOTTOM:
                                break;
                        }

                        // Search the available edges for one that matches the next edge we are looking for.
                        curEdge = null;
                        for (ChunkEdge edge : edges) {
                            if (edge.edgeType() == nextEdge) {
                                curEdge = edge;
                                break;
                            }
                        }

                    }
                } else
                    curEdge = null;

                if (curEdge != null) {
                    // While tracing if this point is on the same axis as the previous point, then just replace the
                    // previous point with this new one, this will end up removing all the redundant points for each
                    // chunk. For example a square box of 9 chunks will produce 4 points for the corners only (assuming
                    // it starts on a corner)
                    if (lastEdgeType != null && lastEdgeType == curEdge.edgeType())
                        perimeterPoints.set(perimeterPoints.size() - 1, curEdge.point2());
                    else
                        perimeterPoints.add(curEdge.point2());

                    lastEdgeType = curEdge.edgeType();
                } else {
                    Clans.getMinecraftHelper().getLogger().error("Unable to successfully trace claim chunk perimeter. This claim will not be visible on dynmap.");
                    bTraceError = true;
                    break;
                }

                // This is extra protection to ensure we don't end up in an infinite loop on some type of error.
                if (nLoopLimit <= 0) {
                    Clans.getMinecraftHelper().getLogger().error("Unable to find starting point in claim trace. This claim will not be visible on dynmap.");
                    bTraceError = true;
                    break;
                }
            } while (curEdge != startEdge);
        }

        if (bTraceError)
            perimeterPoints.clear();

        return perimeterPoints;
    }

    /**
     * Clean up all the cross references so the garbage collector can destroy the objects
     */
    public void cleanup() {
        for (Map.Entry<ChunkPosition, AdjacentChunk> chunk : chunkGroupMap.entrySet())
            chunk.getValue().cleanup();

        chunkGroupMap.clear();
    }
}

