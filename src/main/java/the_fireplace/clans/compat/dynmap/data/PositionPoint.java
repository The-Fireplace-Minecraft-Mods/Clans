package the_fireplace.clans.compat.dynmap.data;

public class PositionPoint {
    private int m_X;
    private int m_Y;

    public PositionPoint() {
        setPos(0, 0);
    }

    public PositionPoint(int x, int y) {
        setPos(x, y);
    }

    public void setX(int x) {
        m_X = x;
    }

    public void setY(int y) {
        m_Y = y;
    }

    public void setPos(int x, int y) {
        m_X = x;
        m_Y = y;
    }

    public int getX() {
        return m_X;
    }

    public int getY() {
        return m_Y;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        else if (obj == this)
            return true;
        else
            return obj instanceof PositionPoint && this.equals((PositionPoint) obj);
    }

    private boolean equals(PositionPoint point) {
        if (point == null)
            return false;
        else
            return m_X == point.m_X && m_Y == point.m_Y;
    }

    @Override
    public int hashCode() {
        int sum = m_X + m_Y;
        return sum * (sum + 1) / 2 + m_X;
    }
}

