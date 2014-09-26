package com.cognitect.transducers;

import java.util.Collection;

public class Transducers {


    // can we get rid of local in mapcat? doesn't look like it
    // ??? make map params match function
    // comp works right
    // reduced short-circuit
    // implement take
    // implement partition
    // comparing / testing w/ 8
    // cleaning up experience

    public static class Reduced {
        boolean reduced = false;
        public boolean isReduced() { return reduced; }
        public void set() { reduced = true; }
    }

    private static <R, T> R reduce(IReducingFunction<R, T> f, R result, Iterable<T> input) {
        return reduce(f, result, input, new Reduced());
    }

    private static <R, T> R reduce(IReducingFunction<R, T> f, R result, Iterable<T> input, Reduced reduced) {
        R ret = result;
        for(T t : input) {
            ret = f.apply(ret, t, reduced);
            if (reduced.isReduced())
                return ret;
        }
        return ret;
    }

    //*** Core reducing function types

    public static interface IReducingFunction<R, T> {
        R apply();
        R apply(R result);
        R apply(R result, T input, Reduced reduced);
    }

    public static interface IStepFunction<R, T> {
        public R apply(R result, T input, Reduced reduced);
    }

    // Add step function
    // and complete api
    // that makes this thing w/ delegation
    // returns a ReducingFunction
    public static abstract class AReducingFunction<R, T> implements IReducingFunction<R, T> {
        @Override
        public R apply() {
            return null;
        }

        @Override
        public R apply(R result) {
            return result;
        }
    }

    public static <R, T> AReducingFunction<R, T> completing(final IStepFunction<R, T> stepFunction) {
        return new AReducingFunction<R, T>() {
            @Override
            public R apply(R result, T input, Reduced reduced) {
                return stepFunction.apply(result, input, reduced);
            }
        };
    }


    //*** Core transducer type

    public static interface Transducer<B, C> {
        <R> IReducingFunction<R, C> apply(IReducingFunction<R, B> xf);

        <A> Transducer<A, C> comp(Transducer<A, B> right);
    }

    public static abstract class ATransducer<B, C> implements Transducer<B, C> {
        @Override
        public <A> Transducer<A, C> comp(Transducer<A, B> right) {
            return compose(this, right);
        }
    }

    //*** composition

    public static <A, B, C> Transducer<A, C> compose(final Transducer<B, C> left, final Transducer<A, B> right) {
        return new ATransducer<A, C>() {
            @Override
            public <R> IReducingFunction<R, C> apply(IReducingFunction<R, A> xf) {
                return left.apply(right.apply(xf));
            }
        };
    }

    //*** abstract base helper types

    public static abstract class AReducingFunctionOn<R, A, B> implements IReducingFunction<R, B> {

        IReducingFunction<R, A> xf;

        public AReducingFunctionOn(IReducingFunction<R, A> xf) {
            this.xf = xf;
        }

        @Override
        public R apply() {
            return xf.apply();
        }

        @Override
        public R apply(R result) {
            return xf.apply(result);
        }
    }

    // *** transducers

    public static interface Predicate<T> {
        boolean test(T t);
    }

    public static interface Function<T, R> {
        R apply(T t);
    }

    public static <A, B> Transducer<A, B> map(final Function<B, A> f) {
        return new ATransducer<A, B>() {
            @Override
            public <R> IReducingFunction<R, B> apply(final IReducingFunction<R, A> xf) {
                return new AReducingFunctionOn<R, A, B>(xf) {
                    @Override
                    public R apply(R result, B input, Reduced reduced) {
                        return xf.apply(result, f.apply(input), reduced);
                    }
                };
            }
        };
    }


    public static <A> Transducer<A, A> filter(final Predicate<A> p) {
        return new ATransducer<A, A>() {
            @Override
            public <R> IReducingFunction<R, A> apply(IReducingFunction<R, A> xf) {
                return new AReducingFunctionOn<R, A, A>(xf) {
                    @Override
                    public R apply(R result, A input, Reduced reduced) {
                        if (p.test(input))
                            return xf.apply(result, input, reduced);
                        return result;
                    }
                };
            }
        };
    }


    public static <A, B extends Iterable<A>> Transducer<A, B> cat() {
        return new ATransducer<A, B>() {
            @Override
            public <R> IReducingFunction<R, B> apply(final IReducingFunction<R, A> xf) {
                return new AReducingFunctionOn<R, A, B>(xf) {
                    @Override
                    public R apply(R result, B input, Reduced reduced) {
                        return reduce(xf, result, input, reduced);
                    }
                };
            }
        };
    }

    public static <A, B extends Iterable<A>, C> Transducer<A, C> mapcat(Function<C, B> f) {
        return map(f).comp(Transducers.<A, B>cat());
    }

    public static <A> Transducer<A, A> remove(final Predicate<A> p) {
        return new ATransducer<A, A>() {
            @Override
            public <R> IReducingFunction<R, A> apply(IReducingFunction<R, A> xf) {
                return new AReducingFunctionOn<R, A, A>(xf) {
                    @Override
                    public R apply(R result, A input, Reduced reduced) {
                        if (!p.test(input))
                            return xf.apply(result, input, reduced);
                        return result;
                    }
                };
            }
        };
    }

    public static <A> Transducer<A, A> take(final long n) {
        return new ATransducer<A, A>() {
            @Override
            public <R> IReducingFunction<R, A> apply(IReducingFunction<R, A> xf) {
                return new AReducingFunctionOn<R, A, A>(xf) {
                    long taken = 0;
                    @Override
                    public R apply(R result, A input, Reduced reduced) {
                        R ret = result;
                        if (taken < n) {
                            ret = xf.apply(result, input, reduced);
                            taken++;
                        } else {
                            reduced.set();
                        }
                        return ret;
                    }
                };
            }
        };
    }

    public static <A> Transducer<A, A> takeWhile(final Predicate<A> p) {
        return new ATransducer<A, A>() {
            @Override
            public <R> IReducingFunction<R, A> apply(IReducingFunction<R, A> xf) {
                return new AReducingFunctionOn<R, A, A>(xf) {
                    @Override
                    public R apply(R result, A input, Reduced reduced) {
                        R ret = result;
                        if (p.test(input)) {
                            ret = xf.apply(result, input, reduced);
                        } else {
                            reduced.set();
                        }
                        return ret;
                    }
                };
            }
        };
    }

    public static <A> Transducer<A, A> drop(final long n) {
        return new ATransducer<A, A>() {
            @Override
            public <R> IReducingFunction<R, A> apply(IReducingFunction<R, A> xf) {
                return new AReducingFunctionOn<R, A, A>(xf) {
                    long dropped = 0;
                    @Override
                    public R apply(R result, A input, Reduced reduced) {
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

    public static <A> Transducer<A, A> dropWhile(final Predicate<A> p) {
        return new ATransducer<A, A>() {
            @Override
            public <R> IReducingFunction<R, A> apply(IReducingFunction<R, A> xf) {
                return new AReducingFunctionOn<R, A, A>(xf) {
                    boolean drop = true;
                    @Override
                    public R apply(R result, A input, Reduced reduced) {
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

    public static <A> Transducer<A, A> takeNth(final long n) {
        return new ATransducer<A, A>() {
            @Override
            public <R> IReducingFunction<R, A> apply(IReducingFunction<R, A> xf) {
                return new AReducingFunctionOn<R, A, A>(xf) {
                    long nth = 0;
                    @Override
                    public R apply(R result, A input, Reduced reduced) {
                        return ((++nth % n) == 0) ? xf.apply(result, input, reduced) : result;
                    }
                };
            }
        };
    }


    /*
replace -- HARD UNLESS RESTRICTED TO SAME TYPE
keep
keep-indexed
partition-by
partition-all
dedupe
random-sample
     */

    //**** transducible processes

    public static <R, A, B> R transduce(Transducer<A, B> xf, IReducingFunction<R, A> builder, Iterable<B> input) {
        return transduce(xf, builder, builder.apply(), input);
    }

    public static <R, A, B> R transduce(Transducer<A, B> xf, IReducingFunction<R, A> builder, R init, Iterable<B> input) {
        IReducingFunction<R, B> _xf = xf.apply(builder);
        return reduce(_xf, init, input, new Reduced());
    }

    public static <R, A, B> R transduce(Transducer<A, B> xf, IStepFunction<R, A> builder, R init, Iterable<B> input) {
        return transduce(xf, completing(builder), init, input);
    }

    public static <R extends Collection<A>, A, B> R into(R init, Transducer<A, B> xf, Iterable<B> input) {
        return transduce(xf, new AReducingFunction<R, A>() {
            @Override
            public R apply(R result, A input, Reduced reduced) {
                result.add(input);
                return result;
            }
        }, init, input);
    }
}
