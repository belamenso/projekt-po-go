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
}
