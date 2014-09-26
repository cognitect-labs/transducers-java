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

    public static interface Transducer<OUT, IN> {
        <R> ReducingFunction<R, IN> apply(ReducingFunction<R, OUT> xf);

        <OUT2> Transducer<OUT2, IN> comp(Transducer<OUT2, OUT> right);
    }

    public static abstract class ATransducer<OUT, IN> implements Transducer<OUT, IN> {
        @Override
        public <OUT2> Transducer<OUT2, IN> comp(Transducer<OUT2, OUT> right) {
            return compose(this, right);
        }
    }

    //*** composition

    private static class Comp<OUT2, OUT, IN> extends ATransducer<OUT2, IN> {

        private Transducer<OUT, IN> left;
        private Transducer<OUT2, OUT> right;


        public Comp(Transducer<OUT, IN> left, Transducer<OUT2, OUT> right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public <R> ReducingFunction<R, IN> apply(ReducingFunction<R, OUT2> xf) {
            return left.apply(right.apply(xf));
        }
    }

    public static <OUT2, OUT, IN> Transducer<OUT2, IN> compose(Transducer<OUT, IN> left, Transducer<OUT2, OUT> right) {
        return new Comp<OUT2, OUT, IN>(left, right);
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

    // Function<in, out>
    // Map<in, out>
    // ATransducer<out, in>
    private static class Map<IN, OUT> extends ATransducer<OUT, IN> {

        Function<IN, OUT> f;

        public Map(Function<IN, OUT> f) {
            this.f = f;
        }

        @Override
        public <R> ReducingFunction<R, IN> apply(final ReducingFunction<R, OUT> xf) {
            return new AReducingFunctionOn<R, OUT, IN>(xf) {
                @Override
                public R apply(R result, IN input) {
                    System.out.println("mapping!");
                    return xf.apply(result, (f.apply(input)));
                }
            };
        }
    }

    public static <OUT, IN> Transducer<OUT, IN> map(Function<IN, OUT> f) {
        return new Map<IN, OUT>(f);
    }



    private static class Filter<IN> extends ATransducer<IN, IN> {
        Predicate<IN> p;

        public Filter(Predicate<IN> p) {
            this.p = p;
        }

        @Override
        public <R> ReducingFunction<R, IN> apply(final ReducingFunction<R, IN> xf) {
            return new AReducingFunctionOn<R, IN, IN>(xf) {
                @Override
                public R apply(R result, IN input) {
                    System.out.println("filtering!");
                    if (p.test(input))
                        return xf.apply(result, input);
                    return result;
                }
            };
        }
    }

    public static <IN> Transducer<IN, IN> filter(Predicate<IN> p) {
        return new Filter<IN>(p);
    }



    private static class Cat<IN extends Iterable<OUT>, OUT> extends ATransducer<OUT, IN> {

        public Cat() {}

        @Override
        public <R> ReducingFunction<R, IN> apply(final ReducingFunction<R, OUT> xf) {
            return new AReducingFunctionOn<R, OUT, IN>(xf) {
                @Override
                public R apply(R result, IN input) {
                    System.out.println("catting!");
                    return reduce(xf, result, input);
                }
            };
        }
    }

    public static <OUT, IN extends Iterable<OUT>> Transducer<OUT, IN> cat() { return new Cat<IN, OUT>(); }


    public static <IN, OUT extends Iterable<OUT2>, OUT2> Transducer<OUT2, IN> mapcat(Function<IN, OUT> f) {
        // need to store cat in a local var to get correct type params
        // OR need to pass Class<T> argument to cat, i.e., cat(bType) where
        // mapcat takes a second arg Class<B> btype that caller must provide
        // OR need to cast and break typing
        // unchecked cast required to coerce Catting<Object, Iterable<Object>
        //return map(f).comp((Transducer<B, Iterable<B>>) cat());
        Transducer<OUT, IN> m = map(f);
        Transducer<OUT2, OUT> c = cat();
        return m.comp(c);
    }


    public static <IN, OUT extends Iterable<OUT2>, OUT2> Transducer<OUT2, IN> mapcat2(Function<IN, OUT> f) {
        Transducer<OUT, IN> m = map(f);
        Transducer<OUT2, OUT> c = cat();
        return m.comp(c);
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
