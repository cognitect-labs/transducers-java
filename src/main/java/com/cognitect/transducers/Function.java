package com.cognitect.transducers;

public interface Function<T, R> {
    R apply(T t);
}
