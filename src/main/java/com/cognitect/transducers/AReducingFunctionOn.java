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
 * Abstract base class for implementing a reducing function that chains to
 * another reducing function. Zero-arity and single-arity overloads of apply
 * delegate to the chained reducing function. Derived classes must implement
 * the three-arity overload of apply, and may implement either of the other
 * two overloads as required.
 * @param <R> Type of first argument and return value of the reducing functions
 * @param <A> Input type of reducing function being chained to
 * @param <B> Input type of this reducing function
 */
public abstract class AReducingFunctionOn<R, A, B> implements IReducingFunction<R, B> {

    protected IReducingFunction<R, ? super A> xf;

    /**
     * Constructs a reducing function that chains to the given
     * reducing function.
     * @param xf a reducing function to chain to
     */
    public AReducingFunctionOn(IReducingFunction<R, ? super A> xf) {
        this.xf = xf;
    }

    /**
     * Forwards to chained reducing function.
     * @return a new result
     */
    @Override
    public R apply() {
        return xf.apply();
    }

    /**
     * Forwards to chained reducing function.
     * @param result The final reduction result
     * @return the completed result
     */
    @Override
    public R apply(R result) {
        return xf.apply(result);
    }
}