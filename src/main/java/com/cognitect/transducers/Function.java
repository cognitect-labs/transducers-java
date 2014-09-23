package com.cognitect.transducers;

public interface Function<R, T> {
    R apply(T t);
}
