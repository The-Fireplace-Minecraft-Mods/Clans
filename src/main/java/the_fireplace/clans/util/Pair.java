package the_fireplace.clans.util;

import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * Simple Pair system with 2 variables.
 * @author MRebhan
 * @author The_Fireplace
 *
 * @param <V1> First variable
 * @param <V2> Second variable
 */

public class Pair<V1, V2> implements Serializable {
    private static final long serialVersionUID = 2586850598481149380L;

    private V1 obj1;
    private V2 obj2;

    public Pair(V1 obj1, V2 obj2) {
        this.obj1 = obj1;
        this.obj2 = obj2;
    }

    public V1 getValue1() {
        return this.obj1;
    }

    public V2 getValue2() {
        return this.obj2;
    }

    @Override public String toString() {
        return Pair.class.getName() + "@" + Integer.toHexString(this.hashCode()) + " [" + this.obj1.toString() + ", " + this.obj2.toString() + "]";
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null)
            return false;
        else if (obj == this)
            return true;
        else
            return obj instanceof Pair && equals((Pair) obj);
    }

    public boolean equals(@Nullable Pair pair) {
        if (pair == null)
            return false;
        return pair.obj1.equals(obj1) && pair.obj2.equals(obj2);
    }
}