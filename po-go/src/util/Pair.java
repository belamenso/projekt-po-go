package util;

import java.io.Serializable;

public class Pair<T, S> implements Serializable {
    public T x;
    public S y;

    public Pair() {}

    public Pair(T f, S s) {
        x = f;
        y = s;
    }

    public boolean equals(Object object) {
        if(object instanceof Pair) {
            Pair<?,?> p = (Pair<?,?>) object;
            return p.x.equals(x) && p.y.equals(y);
        }
        return false;
    }

    public int hashCode() {
        return x.hashCode() * 33 + y.hashCode();
    }

}
