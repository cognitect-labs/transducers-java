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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.cognitect.transducers.Impl.reduce;

public class Fns {

    //**** Helper functions

    public static <R, T> IReducingFunction<R, T> completing(final IStepFunction<R, T> stepFunction) {
        if (stepFunction instanceof IReducingFunction)
            return (IReducingFunction<R, T>) stepFunction;
        else
            return new AReducingFunction<R, T>() {
                @Override
                public R apply(R result, T input, AtomicBoolean reduced) {
                    return stepFunction.apply(result, input, reduced);
                }
            };
    }

    public static <A, B, C> ITransducer<A, C> compose(final ITransducer<B, C> left, final ITransducer<A, B> right) {
        return left.comp(right);
    }

    //**** transducible processes

    public static <R, A, B> R transduce(ITransducer<A, B> xf, IStepFunction<R, A> builder, Iterable<B> input) {
        IReducingFunction<R, A> _builder = completing(builder);
        IReducingFunction<R, B> _xf = xf.apply(_builder);
        return reduce(_xf, _builder.apply(), input);
    }

    public static <R, A, B> R transduce(ITransducer<A, B> xf, IStepFunction<R, A> builder, R init, Iterable<B> input) {
        IReducingFunction<R, A> _builder = completing(builder);
        IReducingFunction<R, B> _xf = xf.apply(_builder);
        return reduce(_xf, init, input);
    }

    public static <R extends Collection<A>, A, B> R into(R init, ITransducer<A, B> xf, Iterable<B> input) {
        return transduce(xf, new AReducingFunction<R, A>() {
            @Override
            public R apply(R result, A input, AtomicBoolean reduced) {
                result.add(input);
                return result;
            }
        }, init, input);
    }


    // *** transducers

    public static <A, B> ITransducer<A, B> map(final Function<B, A> f) {
        return new ATransducer<A, B>() {
            @Override
            public <R> IReducingFunction<R, B> apply(final IReducingFunction<R, A> xf) {
                return new AReducingFunctionOn<R, A, B>(xf) {
                    @Override
                    public R apply(R result, B input, AtomicBoolean reduced) {
                        return xf.apply(result, f.apply(input), reduced);
                    }
                };
            }
        };
    }


    public static <A> ITransducer<A, A> filter(final Predicate<A> p) {
        return new ATransducer<A, A>() {
            @Override
            public <R> IReducingFunction<R, A> apply(IReducingFunction<R, A> xf) {
                return new AReducingFunctionOn<R, A, A>(xf) {
                    @Override
                    public R apply(R result, A input, AtomicBoolean reduced) {
                        if (p.test(input))
                            return xf.apply(result, input, reduced);
                        return result;
                    }
                };
            }
        };
    }


    public static <A, B extends Iterable<A>> ITransducer<A, B> cat() {
        return new ATransducer<A, B>() {
            @Override
            public <R> IReducingFunction<R, B> apply(final IReducingFunction<R, A> xf) {
                return new AReducingFunctionOn<R, A, B>(xf) {
                    @Override
                    public R apply(R result, B input, AtomicBoolean reduced) {
                        return reduce(xf, result, input, reduced);
                    }
                };
            }
        };
    }

    public static <A, B extends Iterable<A>, C> ITransducer<A, C> mapcat(Function<C, B> f) {
        return map(f).comp(Fns.<A, B>cat());
    }

    public static <A> ITransducer<A, A> remove(final Predicate<A> p) {
        return new ATransducer<A, A>() {
            @Override
            public <R> IReducingFunction<R, A> apply(IReducingFunction<R, A> xf) {
                return new AReducingFunctionOn<R, A, A>(xf) {
                    @Override
                    public R apply(R result, A input, AtomicBoolean reduced) {
                        if (!p.test(input))
                            return xf.apply(result, input, reduced);
                        return result;
                    }
                };
            }
        };
    }

    public static <A> ITransducer<A, A> take(final long n) {
        return new ATransducer<A, A>() {
            @Override
            public <R> IReducingFunction<R, A> apply(IReducingFunction<R, A> xf) {
                return new AReducingFunctionOn<R, A, A>(xf) {
                    long taken = 0;
                    @Override
                    public R apply(R result, A input, AtomicBoolean reduced) {
                        R ret = result;
                        if (taken < n) {
                            ret = xf.apply(result, input, reduced);
                            taken++;
                        } else {
                            reduced.set(true);
                        }
                        return ret;
                    }
                };
            }
        };
    }

    public static <A> ITransducer<A, A> takeWhile(final Predicate<A> p) {
        return new ATransducer<A, A>() {
            @Override
            public <R> IReducingFunction<R, A> apply(IReducingFunction<R, A> xf) {
                return new AReducingFunctionOn<R, A, A>(xf) {
                    @Override
                    public R apply(R result, A input, AtomicBoolean reduced) {
                        R ret = result;
                        if (p.test(input)) {
                            ret = xf.apply(result, input, reduced);
                        } else {
                            reduced.set(true);
                        }
                        return ret;
                    }
                };
            }
        };
    }

    public static <A> ITransducer<A, A> drop(final long n) {
        return new ATransducer<A, A>() {
            @Override
            public <R> IReducingFunction<R, A> apply(IReducingFunction<R, A> xf) {
                return new AReducingFunctionOn<R, A, A>(xf) {
                    long dropped = 0;
                    @Override
                    public R apply(R result, A input, AtomicBoolean reduced) {
                        R ret = result;
                        if (dropped < n) {
                            dropped++;
                        } else {
                            ret = xf.apply(result, input, reduced);
                        }
                        return ret;
                    }
                };
            }
        };
    }

    public static <A> ITransducer<A, A> dropWhile(final Predicate<A> p) {
        return new ATransducer<A, A>() {
            @Override
            public <R> IReducingFunction<R, A> apply(IReducingFunction<R, A> xf) {
                return new AReducingFunctionOn<R, A, A>(xf) {
                    boolean drop = true;
                    @Override
                    public R apply(R result, A input, AtomicBoolean reduced) {
                        if (drop && p.test(input)) {
                            return result;
                        }
                        drop = false;
                        return xf.apply(result, input, reduced);
                    }
                };
            }
        };
    }

    public static <A> ITransducer<A, A> takeNth(final long n) {
        return new ATransducer<A, A>() {
            @Override
            public <R> IReducingFunction<R, A> apply(IReducingFunction<R, A> xf) {
                return new AReducingFunctionOn<R, A, A>(xf) {
                    long nth = 0;
                    @Override
                    public R apply(R result, A input, AtomicBoolean reduced) {
                        return ((++nth % n) == 0) ? xf.apply(result, input, reduced) : result;
                    }
                };
            }
        };
    }

    public static <A> ITransducer<A, A> replace(final Map<A, A> smap) {
        return map(new Function<A, A>() {
            @Override
            public A apply(A a) {
                if (smap.containsKey(a))
                    return smap.get(a);
                return a;
            }
        });
    }

    public static <A> ITransducer<A, A> keep(final Function<A, A> f) {
        return new ATransducer<A, A>() {
            @Override
            public <R> IReducingFunction<R, A> apply(IReducingFunction<R, A> xf) {
                return new AReducingFunctionOn<R, A, A>(xf) {
                    @Override
                    public R apply(R result, A input, AtomicBoolean reduced) {
                        A _input = f.apply(input);
                        if (_input != null)
                            return xf.apply(result, _input, reduced);
                        return result;
                    }
                };
            }
        };
    }

    public static <A> ITransducer<A, A> keep(final BiFunction<Long, A, A> f) {
        return new ATransducer<A, A>() {
            @Override
            public <R> IReducingFunction<R, A> apply(IReducingFunction<R, A> xf) {
                return new AReducingFunctionOn<R, A, A>(xf) {
                    long n = 0;
                    @Override
                    public R apply(R result, A input, AtomicBoolean reduced) {
                        n++;
                        A _input = f.apply(n, input);
                        if (_input != null)
                            return xf.apply(result, _input, reduced);
                        return result;
                    }
                };
            }
        };
    }

    public static <A> ITransducer<A, A> dedupe() {
        return new ATransducer<A, A>() {
            @Override
            public <R> IReducingFunction<R, A> apply(IReducingFunction<R, A> xf) {
                return new AReducingFunctionOn<R, A, A>(xf) {
                    A prior = null;
                    @Override
                    public R apply(R result, A input, AtomicBoolean reduced) {
                        R ret = result;
                        if (prior != input) {
                            prior = input;
                            ret = xf.apply(result, input, reduced);
                        }
                        return ret;
                    }
                };
            }
        };
    }

    public static <A> ITransducer<A, A> randomSample(final Double prob) {
        return filter(new Predicate<A>() {
            @Override
            public boolean test(A a) {
                return (Math.random() < prob);
            }
        });
    }

     public static <A, B> ITransducer<Iterable<A>, A> partitionBy(final Function<A, B> f) {

        return new ATransducer<Iterable<A>, A>() {
            @Override
            public <R> IReducingFunction<R, A> apply(final IReducingFunction<R, Iterable<A>> xf) {
                return new IReducingFunction<R, A>() {
                    List<A> part = new ArrayList<A>();
                    Object mark = new Object();
                    Object prior = mark;

                    @Override
                    public R apply() {
                        return xf.apply();
                    }

                    @Override
                    public R apply(R result) {
                        R ret = result;
                        if (!part.isEmpty()) {
                            List<A> copy = new ArrayList<A>(part);
                            part.clear();
                            ret = xf.apply(result, copy, new AtomicBoolean());
                        }
                        return xf.apply(ret);
                    }

                    @Override
                    public R apply(R result, A input, AtomicBoolean reduced) {
                        Object val = f.apply(input);
                        prior = val;
                        if ((prior == mark) || (prior.equals(val))) {
                            part.add(input);
                            return result;
                        } else {
                            List<A> copy = new ArrayList<A>(part);
                            part.clear();
                            R ret = xf.apply(result, copy, reduced);
                            if (!reduced.get()) {
                                part.add(input);
                            }
                            return ret;
                        }
                    }
                };
            }
        };
    }

    public static <A, B> ITransducer<Iterable<A>, A> partitionAll(final int n) {

        return new ATransducer<Iterable<A>, A>() {
            @Override
            public <R> IReducingFunction<R, A> apply(final IReducingFunction<R, Iterable<A>> xf) {
                return new IReducingFunction<R, A>() {
                    List<A> part = new ArrayList<A>(n);

                    @Override
                    public R apply() {
                        return xf.apply();
                    }

                    @Override
                    public R apply(R result) {
                        R ret = result;
                        if (!part.isEmpty()) {
                            List<A> copy = new ArrayList<A>(part);
                            part.clear();
                            ret = xf.apply(result, copy, new AtomicBoolean());
                        }
                        return xf.apply(ret);
                    }

                    @Override
                    public R apply(R result, A input, AtomicBoolean reduced) {
                        part.add(input);
                        if (n == part.size()) {
                            List<A> copy = new ArrayList<A>(part);
                            part.clear();
                            return xf.apply(result, copy, reduced);
                        }
                        return result;
                    }
                };
            }
        };
    }
}
