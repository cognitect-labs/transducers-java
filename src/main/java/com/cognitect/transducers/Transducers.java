package com.cognitect.transducers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.cognitect.transducers.Base.*;

public class Transducers {

    public static interface IReduced {
        boolean isReduced();
        void set();
    }

    public static interface IStepFunction<R, T> {
        public R apply(R result, T input, IReduced reduced);
    }

    public static interface IReducingFunction<R, T> extends IStepFunction<R, T>{
        R apply();
        R apply(R result);
    }

    public static interface ITransducer<B, C> {
        <R> IReducingFunction<R, C> apply(IReducingFunction<R, B> xf);

        <A> ITransducer<A, C> comp(ITransducer<A, B> right);
    }

    //**** transducible processes

    public static <R, A, B> R transduce(ITransducer<A, B> xf, IReducingFunction<R, A> builder, Iterable<B> input) {
        return transduce(xf, builder, builder.apply(), input);
    }

    public static <R, A, B> R transduce(ITransducer<A, B> xf, IReducingFunction<R, A> builder, R init, Iterable<B> input) {
        IReducingFunction<R, B> _xf = xf.apply(builder);
        return reduce(_xf, init, input);
    }

    public static <R, A, B> R transduce(ITransducer<A, B> xf, IStepFunction<R, A> builder, R init, Iterable<B> input) {
        return transduce(xf, completing(builder), init, input);
    }

    public static <R extends Collection<A>, A, B> R into(R init, ITransducer<A, B> xf, Iterable<B> input) {
        return transduce(xf, new AReducingFunction<R, A>() {
            @Override
            public R apply(R result, A input, IReduced reduced) {
                result.add(input);
                return result;
            }
        }, init, input);
    }

    // *** transducers

    public static interface Predicate<T> {
        boolean test(T t);
    }

    public static interface Function<T, R> {
        R apply(T t);
    }

    public static interface BiFunction<T, U, R> {
        R apply(T t, U u);
    }

    public static <A, B> ITransducer<A, B> map(final Function<B, A> f) {
        return new ATransducer<A, B>() {
            @Override
            public <R> IReducingFunction<R, B> apply(final IReducingFunction<R, A> xf) {
                return new AReducingFunctionOn<R, A, B>(xf) {
                    @Override
                    public R apply(R result, B input, IReduced reduced) {
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
                    public R apply(R result, A input, IReduced reduced) {
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
                    public R apply(R result, B input, IReduced reduced) {
                        return reduce(xf, result, input, reduced);
                    }
                };
            }
        };
    }

    public static <A, B extends Iterable<A>, C> ITransducer<A, C> mapcat(Function<C, B> f) {
        return map(f).comp(Transducers.<A, B>cat());
    }

    public static <A> ITransducer<A, A> remove(final Predicate<A> p) {
        return new ATransducer<A, A>() {
            @Override
            public <R> IReducingFunction<R, A> apply(IReducingFunction<R, A> xf) {
                return new AReducingFunctionOn<R, A, A>(xf) {
                    @Override
                    public R apply(R result, A input, IReduced reduced) {
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
                    public R apply(R result, A input, IReduced reduced) {
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

    public static <A> ITransducer<A, A> takeWhile(final Predicate<A> p) {
        return new ATransducer<A, A>() {
            @Override
            public <R> IReducingFunction<R, A> apply(IReducingFunction<R, A> xf) {
                return new AReducingFunctionOn<R, A, A>(xf) {
                    @Override
                    public R apply(R result, A input, IReduced reduced) {
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

    public static <A> ITransducer<A, A> drop(final long n) {
        return new ATransducer<A, A>() {
            @Override
            public <R> IReducingFunction<R, A> apply(IReducingFunction<R, A> xf) {
                return new AReducingFunctionOn<R, A, A>(xf) {
                    long dropped = 0;
                    @Override
                    public R apply(R result, A input, IReduced reduced) {
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
                    public R apply(R result, A input, IReduced reduced) {
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
                    public R apply(R result, A input, IReduced reduced) {
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
                    public R apply(R result, A input, IReduced reduced) {
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
                    public R apply(R result, A input, IReduced reduced) {
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
                    public R apply(R result, A input, IReduced reduced) {
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
                            ret = xf.apply(result, copy, new Reduced());
                        }
                        return xf.apply(ret);
                    }

                    @Override
                    public R apply(R result, A input, IReduced reduced) {
                        Object val = f.apply(input);
                        prior = val;
                        if ((prior == mark) || (prior.equals(val))) {
                            part.add(input);
                            return result;
                        } else {
                            List<A> copy = new ArrayList<A>(part);
                            part.clear();
                            R ret = xf.apply(result, copy, reduced);
                            if (!reduced.isReduced()) {
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
                            ret = xf.apply(result, copy, new Reduced());
                        }
                        return xf.apply(ret);
                    }

                    @Override
                    public R apply(R result, A input, IReduced reduced) {
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

    //*** composition

    public static <A, B, C> ITransducer<A, C> compose(final ITransducer<B, C> left, final ITransducer<A, B> right) {
        return left.comp(right);
    }

}
