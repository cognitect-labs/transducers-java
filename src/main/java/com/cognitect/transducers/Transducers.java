package com.cognitect.transducers;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Collection;

public class Transducers {

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

    private static <R, T> ReducingFunction<R, T> preservingReduced(final ReducingFunction<R, T> xf) {
        return new ReducingFunction<R, T>() {
            @Override
            public R apply() {
                throw new NotImplementedException();
            }

            @Override
            public R apply(R result) {
                throw new NotImplementedException();
            }

            @Override
            public R apply(R result, T input) {
                R ret = xf.apply(result, input);
//                if (isReduced(ret))
//                    return reduced(ret);
                return ret;
            }
        };
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

    public static interface Transducer<A, B> {
        <R> ReducingFunction<R, B> apply(ReducingFunction<R, A> xf);

        <C> Transducer<A, C> comp(Transducer<B, C> td);
    }

    public static abstract class AAbstractTransducer<A, B> implements Transducer<A, B> {
        @Override
        public <C> Transducer<A, C> comp(Transducer<B, C> td) {
            return compose(this, td);
        }
    }

    //*** composition

    private static class Comp<A, B, C> extends AAbstractTransducer<A, C> {

        private Transducer<A, B> left;
        private Transducer<B, C> right;

        public Comp(Transducer<A, B> left, Transducer<B, C> right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public <R> ReducingFunction<R, C> apply(ReducingFunction<R, A> xf) {
            return left.apply(right.apply(xf));
        }
    }

    public static <A, B, C> Transducer<A, C> compose(Transducer<A, B> left, Transducer<B, C> right) {
        return new Comp<A, B, C>(left, right);
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

    private static class Map<A, B> extends AAbstractTransducer<A, B> {

        Function<B, A> f;

        public Map(Function<B, A> f) {
            this.f = f;
        }

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
    }

    public static <A, B> Transducer<A, B> map(Function<B, A> f) {
        return new Map<A, B>(f);
    }



    private static class Filter<A> extends AAbstractTransducer<A, A> {
        Predicate<A> p;

        public Filter(Predicate<A> p) {
            this.p = p;
        }

        @Override
        public <R> ReducingFunction<R, A> apply(final ReducingFunction<R, A> xf) {
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
    }

    public static <A> Transducer<A, A> filter(Predicate<A> p) {
        return new Filter<A>(p);
    }



    private static class Cat<A> extends AAbstractTransducer<A, Iterable<A>> {

        public Cat() {}

        @Override
        public <R> ReducingFunction<R, Iterable<A>> apply(final ReducingFunction<R, A> xf) {
            return new AReducingFunctionOn<R, A, Iterable<A>>(xf) {
                @Override
                public R apply(R result, Iterable<A> input) {
                    System.out.println("catting!");
                    return reduce(xf, result, input);
                }
            };
        }
    }

    public static <A> Transducer<A, Iterable<A>> cat() { return new Cat(); }



    public static <A, B> Transducer<A, Iterable<B>> mapcat(Function<B, A> f) {
        // need to store cat in a local var to get correct type params
        // OR need to pass Class<T> argument to cat, i.e., cat(bType) where
        // mapcat takes a second arg Class<B> btype that caller must provide
        // OR need to cast and break typing
        Transducer<B, Iterable<B>> c = cat();
        return map(f).comp(c);
        // unchecked cast required to coerce Catting<Object, Iterable<Object>
        //return map(f).comp((Transducer<B, Iterable<B>>) cat());
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
