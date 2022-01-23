package dev.the_fireplace.clans.legacy.model;

public class CoordinatePair extends OrderedPair<Integer, Integer>
{

    public CoordinatePair() {
        super(0, 0);
    }

    public CoordinatePair(int x, int y) {
        super(x, y);
    }

    public void setX(int x) {
        setValue1(x);
    }

    public void setY(int y) {
        setValue2(y);
    }

    public void setPos(int x, int y) {
        setX(x);
        setY(y);
    }

    public int getX() {
        return getValue1();
    }

    public int getY() {
        return getValue2();
    }
}

