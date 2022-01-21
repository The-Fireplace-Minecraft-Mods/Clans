package the_fireplace.clans.legacy.model;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Simple OrderedPair system with 2 variables.
 *
 * @param <V1> First variable
 * @param <V2> Second variable
 * @author MRebhan
 * @author The_Fireplace
 */

@SuppressWarnings("WeakerAccess")
public class OrderedPair<V1, V2>
{
    private V1 obj1;
    private V2 obj2;

    public OrderedPair(V1 obj1, V2 obj2) {
        this.obj1 = obj1;
        this.obj2 = obj2;
    }

    public V1 getValue1() {
        return this.obj1;
    }

    public V2 getValue2() {
        return this.obj2;
    }

    public void setValue1(V1 obj1) {
        this.obj1 = obj1;
    }

    public void setValue2(V2 obj2) {
        this.obj2 = obj2;
    }

    @Override
    public String toString() {
        return OrderedPair.class.getName() + "@" + Integer.toHexString(this.hashCode()) + " [" + this.obj1.toString() + ", " + this.obj2.toString() + "]";
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        } else if (obj == this) {
            return true;
        } else {
            return obj instanceof OrderedPair && equals((OrderedPair) obj);
        }
    }

    public boolean equals(@Nullable OrderedPair pair) {
        if (pair == null) {
            return false;
        }
        return pair.obj1.equals(obj1) && pair.obj2.equals(obj2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(obj1, obj2);
    }
}