// Copyright 2014 Cognitect. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS-IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.cognitect.transducers;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A complete reducing function. Extends a single reducing step function and
 * adds a zero-arity function for initializing a new result and a single-arity
 * function for processing the final result after the reduction process has
 * completed.
 *
 * @param <R> Type of first argument and return value
 * @param <T> Type of input to reduce
 */
public interface IReducingFunction<R, T> extends IStepFunction<R, T>, Supplier<R>, Function<R, R> {

    /**
     * Returns a newly initialized result.
     *
     * @return a new result
     */
    @Override
    default R get() {
        throw new IllegalStateException();
    }

    /**
     * Completes processing of a final result.
     *
     * @param result the final reduction result
     * @return the completed result
     */
    @Override
    default R apply(R result) {
        return result;
    }

    /**
     * Abstract base class for implementing a reducing function that chains to
     * another reducing function. Zero-arity and single-arity overloads of apply
     * delegate to the chained reducing function. Derived classes must implement
     * the three-arity overload of apply, and may implement either of the other
     * two overloads as required.
     *
     * @param <R> Type of first argument and return value of the reducing
     * functions
     * @param <A> Input type of reducing function being chained to
     * @param <B> Input type of this reducing function
     * @param rd  Reducing input function
     * @param step the step function
     * @return The reducing function
     */
    static <R, A, B> IReducingFunction<R, ? super B>IReducingFunction 
        (IReducingFunction<R, ? super A> rd, IStepFunction<R, ? super B> step) {

        return new IReducingFunction<R, B>() {

            @Override
            public R apply(R result, B input, AtomicBoolean reduced) {
                return step.apply(result, input, reduced);
            }

            /**
             * Forwards to chained reducing function.
             *
             * @return a new result
             */
            @Override
            public R get() {
                return rd.get();
            }

            /**
             * Forwards to chained reducing function.
             *
             * @param result The final reduction result
             * @return the completed result
             */
            @Override
            public R apply(R t) {
                return rd.apply(t);
            }

        };
    }

    /**
     * Applies given reducing function to current result and each T in input,
     * using the result returned from each reduction step as input to the next
     * step. Returns final result.
     *
     * @param f a reducing function
     * @param result an initial result value
     * @param input the input to process
     * @param <R> the type of the result
     * @param <T> the type of each item in input
     * @return the final reduced result
     */
    public static <R, T> R reduce(IReducingFunction<R, ?super T> f, R result, Iterable<T> input) {
        return reduce(f, result, input, new AtomicBoolean());
    }

    /**
     * Applies given reducing function to current result and each T in input,
     * using the result returned from each reduction step as input to the next
     * step. Returns final result.
     *
     * @param f a reducing function
     * @param result an initial result value
     * @param input the input to process
     * @param reduced a boolean flag that can be set to indicate that the
     * reducing process should stop, even though there is still input to process
     * @param <R> the type of the result
     * @param <T> the type of each item in input
     * @return the final reduced result
     */
    public static <R, T> R reduce(IReducingFunction<R, ? super T> f, R result, Iterable<T> input, AtomicBoolean reduced) {
        R ret = result;
        for (T t : input) {
            ret = f.apply(ret, t, reduced);
            if (reduced.get()) {
                break;
            }
        }
        return f.apply(ret);
    }

    /**
     * Converts an step function into a complete reducing function. If passed an
     * IReducingFunction, which is an IStepFunction, returns it; otherwise
     * returns a new instance of IReducingFunction with a step function that
     * forwards to the given IStepFunction, a single-arity apply method (used
     * for completing reduction) that returns its argument (it's an identity
     * function), and a zero-arity apply method (used for initializing a new
     * return value for a reduction if one is not provided) that throws an
     * IllegalStateException.
     *
     * 
     * @param sf The step function to convert to an IReducingFunction,
     * if it is not one already
     * @param <R> the return type of the step function and reducing function
     * @param <T> the input type of the step function and the reducing function
     * @return a new reducing function, or the input step function if it is
     * already a reducing function
     */
    public static <R, T> IReducingFunction<R, ? super T> completing(final IStepFunction<R, ? super T> sf) {

        if (sf instanceof IReducingFunction) {
            return (IReducingFunction<R, ? super T>) sf;
        } else {
            return sf::apply;
        }
    }
}
