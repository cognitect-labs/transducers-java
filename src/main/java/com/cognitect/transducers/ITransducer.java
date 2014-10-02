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
 * A Transducer transforms a reducing function of one type into a
 * reducing function of another (possibly the same) type, applying
 * mapping, filtering, flattening, etc. logic as desired.
 * @param <B> The type of data processed by an input process
 * @param <C> The type of data processed by the transduced process
 */
public interface ITransducer<B, C> {
    /**
     * Transforms a reducing function of B into a reducing function
     * of C.
     * @param xf The input reducing function
     * @param <R> The result type of both the input and the output
     *           reducing functions
     * @return The transformed reducing function
     */
    <R> IReducingFunction<R, C> apply(IReducingFunction<R, B> xf);

    /**
     * Composes a transducer with another transducer, yielding
     * a new transducer.
     * @param right the transducer to compose with this transducer
     * @param <A> the type of input processed by the reducing function
     *           the composed transducer returns when applied
     * @return A new composite transducer
     */
    <A> ITransducer<A, C> comp(ITransducer<A, B> right);
}
