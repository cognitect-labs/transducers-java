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
 * Abstract base class for implementing a reducing function. Zero-arity
 * overload of apply used for initialization throws an exception if called.
 * Single-arity overload of apply used for completing reduction is the
 * identity function. Derived classes must implement the three-arity overload
 * of apply, and may implement either of the other two overloads as required.
 * @param <R> Type of first argument and return value
 * @param <T> Type of input to reduce
 */
public abstract class AReducingFunction<R, T> implements IReducingFunction<R, T> {

    /**
     * Throws IllegalStateException.
     * @return
     */
    @Override
    public R apply() {
        throw new IllegalStateException();
    }

    /**
     * Returns the given result without alteration.
     * @param result The final reduction result
     * @return
     */
    @Override
    public R apply(R result) {
        return result;
    }
}