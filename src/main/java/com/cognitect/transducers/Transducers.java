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

    public static class Reduced<T> {
        T t;
        public Reduced(T t) { this.t = t; }
    }

    public static <T> Reduced<T> reduced(T t) {
        return new Reduced(t);
    }

    public static <T> boolean isReduced(T t) {
        return t instanceof Reduced;
    }

    private static <R, T> R reduce(ReducingFunction<R, T> f, R result, Iterable<T> input) {
        R ret = result;
        for(T t : input) {
            ret = f.apply(ret, t);
           // ??? if reduced, return ret - no more preserving reduced!
        }
        return ret;
    }

    //*** Core reducing function types

    public static interface ReducingFunction<R, T> {
        R apply();
        R apply(R result);
        R apply(R result, T input);
    }

    // Add step function
    // and complete api
    // that makes this thing w/ delegation
    // returns a ReducingFunction
    public static abstract class ReducingStepFunction<R, T> implements ReducingFunction<R, T> {
        @Override
        public R apply() {
            return null;
        }

        @Override
        public R apply(R result) {
            return result;
        }
    }

    //*** Core transducer type

    public static interface Transducer<B, C> {
        <R> ReducingFunction<R, C> apply(ReducingFunction<R, B> xf);

        <A> Transducer<A, C> comp(Transducer<A, B> right);
    }

    public static abstract class ATransducer<B, C> implements Transducer<B, C> {
        @Override
        public <A> Transducer<A, C> comp(Transducer<A, B> right) {
            return compose(this, right);
        }
    }

    //*** composition

    private static class Comp<B, A, C> extends ATransducer<A, C> {

        private Transducer<B, C> left;
        private Transducer<A, B> right;


        public Comp(Transducer<B, C> left, Transducer<A, B> right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public <R> ReducingFunction<R, C> apply(ReducingFunction<R, A> xf) {
            return left.apply(right.apply(xf));
        }
    }

    public static <A, B, C> Transducer<A, C> compose(Transducer<B, C> left, Transducer<A, B> right) {
        return new Comp<B, A, C>(left, right);
    }

    //*** abstract base helper types

    public static abstract class AReducingFunctionOn<R, A, B> implements ReducingFunction<R, B> {

        ReducingFunction<R, A> xf;

        public AReducingFunctionOn(ReducingFunction<R, A> xf) {
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

    public static <A, B> Transducer<A, B> map(final Function<B, A> f) {
        return new ATransducer<A, B>() {
            @Override
            public <R> ReducingFunction<R, B> apply(final ReducingFunction<R, A> xf) {
                return new AReducingFunctionOn<R, A, B>(xf) {
                    @Override
                    public R apply(R result, B input) {
                        System.out.println("mapping!");
                        return xf.apply(result, (f.apply(input)));
                    }
                };
            }
        };
    }


    public static <A> Transducer<A, A> filter(final Predicate<A> p) {
        return new ATransducer<A, A>() {
            @Override
            public <R> ReducingFunction<R, A> apply(ReducingFunction<R, A> xf) {
                return new AReducingFunctionOn<R, A, A>(xf) {
                    @Override
                    public R apply(R result, A input) {
                        System.out.println("filtering!");
                        if (p.test(input))
                            return xf.apply(result, input);
                        return result;
                    }
                };
            }
        };
    }


    public static <A, B extends Iterable<A>> Transducer<A, B> cat() {
        return new ATransducer<A, B>() {
            @Override
            public <R> ReducingFunction<R, B> apply(final ReducingFunction<R, A> xf) {
                return new AReducingFunctionOn<R, A, B>(xf) {
                    @Override
                    public R apply(R result, B input) {
                        System.out.println("catting!");
                        return reduce(xf, result, input);
                    }
                };
            }
        };
    }

    // number, string (coll of char)
    // takes a iterable of ? and reduces it

    public static <A, B extends Iterable<A>, C> Transducer<A, C> mapcat(Function<C, B> f) {
        return map(f).comp(Transducers.<A, B>cat());
    }


    //**** transducible processes

    public static <R, A, B> R transduce(Transducer<A, B> xf, ReducingFunction<R, A> builder, Iterable<B> input) {
        return transduce(xf, builder, builder.apply(), input);
    }

    public static <R, A, B> R transduce(Transducer<A, B> xf, ReducingFunction<R, A> builder, R init, Iterable<B> input) {
        ReducingFunction<R, B> _xf = xf.apply(builder);
        return reduce(_xf, init, input);
    }

    public static <R extends Collection<A>, A, B> R into(R init, Transducer<A, B> xf, Iterable<B> input) {
        return transduce(xf, new ReducingStepFunction<R, A>() {
            @Override
            public R apply(R result, A input) {
                result.add(input);
                return result;
            }
        }, init, input);
    }
}
