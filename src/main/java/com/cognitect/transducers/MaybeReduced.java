package com.cognitect.transducers;

public class MaybeReduced<T> {
    T t;
    boolean reduced;

    MaybeReduced(T t, boolean reduced) {
        this.t = t;
        this.reduced = reduced;
    }

    public T value() { return t; }
    public boolean isReduced() { return reduced; }
}
