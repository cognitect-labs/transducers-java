package com.cognitect.transducers;

public interface ReducingFunction<R, T> {
    R apply();
    R apply(R result);
    R apply(R result, T input);

    //<B> ReducingFunction<R, B> comp(ReducingFunction<R, T> fx);
}
