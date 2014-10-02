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

/**
 * Helper functions used to implement transducers.
 */
public class Impl {

    /**
     * Applies given reducing function to current result and each T in input, using
     * the result returned from each reduction step as input to the next step. Returns
     * final result.
     * @param f a reducing function
     * @param result an initial result value
     * @param input the input to process
     * @param <R> the type of the result
     * @param <T> the type of each item in input
     * @return the final reduced result
     */
    public static <R, T> R reduce(IReducingFunction<R, T> f, R result, Iterable<T> input) {
        return reduce(f, result, input, new AtomicBoolean());
    }

    /**
     * Applies given reducing function to current result and each T in input, using
     * the result returned from each reduction step as input to the next step. Returns
     * final result.
     * @param f a reducing function
     * @param result an initial result value
     * @param input the input to process
     * @param reduced a boolean flag that can be set to indicate that the reducing process
     *                should stop, even though there is still input to process
     * @param <R> the type of the result
     * @param <T> the type of each item in input
     * @return the final reduced result
     */
    public static <R, T> R reduce(IReducingFunction<R, T> f, R result, Iterable<T> input, AtomicBoolean reduced) {
        R ret = result;
        for(T t : input) {
            ret = f.apply(ret, t, reduced);
            if (reduced.get())
                return ret;
        }
        return f.apply(ret);
    }
}
