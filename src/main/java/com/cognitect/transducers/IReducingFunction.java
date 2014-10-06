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

/**
 * A complete reducing function. Extends a single reducing step
 * function and adds a zero-arity function for initializing a new
 * result and a single-arity function for processing the final
 * result after the reduction process has completed.
 * @param <R> Type of first argument and return value
 * @param <T> Type of input to reduce
 */
public interface IReducingFunction<R, T> extends IStepFunction<R, T> {
    /**
     * Returns a newly initialized result.
     * @return a new result
     */
    public R apply();

    /**
     * Completes processing of a final result.
     * @param result the final reduction result
     * @return the completed result
     */
    public R apply(R result);
}
