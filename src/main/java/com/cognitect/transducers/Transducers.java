package com.cognitect.transducers;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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

    private static class Mapping<A, B> implements Transducer<A, B> {

        Function<A, B> f;
        Transducer<B, ?> td;

        public Mapping(Function<A, B> f) {
            this.f = f;
        }

        @Override
        public <R> ReducingFunction<R, B> apply(final ReducingFunction<R, A> xf) {
            //??? how to apply comp?

            return new ReducingFunction<R, B>() {
                @Override
                public R apply() {
                    return xf.apply();
                }

                @Override
                public R apply(R result) {
                    return xf.apply(result);
                }

                @Override
                public R apply(R result, B input) {
                    return xf.apply(result, f.apply(input));
                }
            };
        }

        @Override
        public <C> void comp(Transducer<B, C> td) {
            this.td = td;
        }
    }

    static <A, B> Transducer<A, B> map(Function<A, B> f) {
        return new Mapping<A, B>(f);
    }

    private static class Filtering<A> implements Transducer<A, A> {
        Predicate<A> p;

        public Filtering(Predicate<A> p) {
            this.p = p;
        }

        @Override
        public <R> ReducingFunction<R, A> apply(final ReducingFunction<R, A> xf) {
            return new ReducingFunction<R, A>() {
                @Override
                public R apply() {
                    return xf.apply();
                }

                @Override
                public R apply(R result) {
                    return xf.apply(result);
                }

                @Override
                public R apply(R result, A input) {
                    if (p.test(input))
                        return xf.apply(result, input);
                    return result;
                }
            };
        }

        @Override
        public <B> void comp(Transducer<A, B> td) {
            //this.td = td;
        }
    }

    static <A> Transducer<A, A> filter(Predicate<A> p) {
        return new Filtering<A>(p);
    }

    private static class Catting<A> implements Transducer<A, Iterable<A>> {

        Class<A> aType;

        public Catting(Class<A> aType) {
            this.aType = aType;
        }

        @Override
        public <R> ReducingFunction<R, Iterable<A>> apply(final ReducingFunction<R, A> xf) {
            return new ReducingFunction<R, Iterable<A>>() {
                @Override
                public R apply() {
                    return xf.apply();
                }

                @Override
                public R apply(R result) {
                    return xf.apply(result);
                }

                @Override
                public R apply(R result, Iterable<A> input) {
                    return reduce(preservingReduced(xf), result, input);
                }
            };
        }

        @Override
        public <B> void comp(Transducer<Iterable<A>, B> td) {
            //this.td = td;
        }
    }

    public static <A> Transducer<A, Iterable<A>> cat(Class<A> aType) {
        return new Catting<A>(aType);
    }

    public static <A, B> Transducer<A, Iterable<B>> mapcat(Function<A, B> f, Class<B> bType) {
        Transducer<A, B> m = map(f);
        m.comp(cat(bType));
        return m;
    }
}
