package com.cognitect.transducers;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A reducing step function.
 * @param <R> Type of first argument and return value
 * @param <T> Type of input to reduce
 */
@FunctionalInterface
public interface IStepFunction<R, T> {
    /**
     * Applies the reducing function to the current result and
     * the new input, returning a new result.
     *
     * A reducing function can indicate that no more input
     * should be processed by setting the value of reduced to
     * true. This causes the reduction process to complete,
     * returning the most recent result.
     * @param result The current result value
     * @param input New input to process
     * @param reduced A boolean value which can be set to true
     *                to stop the reduction process
     * @return A new result value
     */
    public R apply(R result, T input, AtomicBoolean reduced);
}
