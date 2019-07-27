package the_fireplace.clans.model;

import java.util.Objects;

public class ChunkEdge {
    public enum Edge {
        LEFT, RIGHT, TOP, BOTTOM
    }

    private static final int CHUNK_SIZE = 16;

    private Edge m_edgeType = Edge.LEFT;
    private CoordinatePair m_p1 = new CoordinatePair();
    private CoordinatePair m_p2 = new CoordinatePair();

    /**
     * Construct an edge based on the chunk and edge position provided.
     *
     * @param chunk Contains the position details of the chunk
     * @param edge Indicates which edge of the chunk this edge is to represent.
     */
    public ChunkEdge (AdjacentChunk chunk, Edge edge) {
        if (chunk != null && edge != null) {
            // Get the position of the chunk which is the upper left corner
            int xPos = chunk.getPos().getRealPosX();
            int zPos = chunk.getPos().getRealPosZ();
            m_edgeType = edge;

            switch (edge) {
                case LEFT:
                    m_p1.setPos(xPos, zPos + CHUNK_SIZE);
                    m_p2.setPos(xPos, zPos);
                    break;

                case RIGHT:
                    m_p1.setPos(xPos + CHUNK_SIZE, zPos);
                    m_p2.setPos(xPos + CHUNK_SIZE, zPos + CHUNK_SIZE);
                    break;

                case TOP:
                    m_p1.setPos(xPos, zPos);
                    m_p2.setPos(xPos + CHUNK_SIZE, zPos);
                    break;

                case BOTTOM:
                    m_p1.setPos(xPos + CHUNK_SIZE, zPos + CHUNK_SIZE);
                    m_p2.setPos(xPos, zPos + CHUNK_SIZE);
                    break;
            }
        }
    }

    /**
     * @return Returns the type of edge this is.
     */

    public Edge edgeType() {
        return m_edgeType;
    }

    /**
     * @return Returns the first point of the edge.
     */

    public CoordinatePair point1() {
        return m_p1;
    }

    /**
     * @return Returns the second point of the edge.
     */

    public CoordinatePair point2() {
        return m_p2;
    }

    /**
     * Compares this object to another edge and returns true if they are the same coordinates.
     *
     * @param obj The edge to compare against.
     * @return Returns true if the edges match with the same coordinates.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        else if (obj == this)
            return true;
        else
            return obj instanceof ChunkEdge && this.equals((ChunkEdge) obj);
    }

    /**
     * Compares this object to another edge and returns true if they are the same coordinates.
     *
     * @param edge The edge to compare against.
     * @return Returns true if the edges match with the same coordinates.
     */
    private boolean equals(ChunkEdge edge) {
        if (edge == null)
            return false;
        else
            return m_p1 == edge.m_p1 && m_p2 == edge.m_p2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_p1, m_p2);
    }
}

