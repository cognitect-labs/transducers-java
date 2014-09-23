package com.cognitect.transducers;

public interface Transducer<A, B> {
    <R> ReducingFunction<R, B> apply(ReducingFunction<R, A> xf);
}