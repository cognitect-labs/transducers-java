package com.cognitect.transducers;

import static com.cognitect.transducers.Transducers.*;

public class Base {

    //*** Reducing logic, with short-circuit

    public static class Reduced implements Transducers.IReduced {
        boolean reduced = false;
        @Override
        public boolean isReduced() { return reduced; }
        @Override
        public void set() { reduced = true; }
    }

    public static <R, T> R reduce(Transducers.IReducingFunction<R, T> f, R result, Iterable<T> input) {
        return Base.reduce(f, result, input, new Reduced());
    }

    public static <R, T> R reduce(Transducers.IReducingFunction<R, T> f, R result, Iterable<T> input, IReduced reduced) {
        R ret = result;
        for(T t : input) {
            ret = f.apply(ret, t, reduced);
            if (reduced.isReduced())
                return ret;
        }
        return ret;
    }

    //*** Abstract base helper classes

    public static abstract class AReducingFunction<R, T> implements Transducers.IReducingFunction<R, T> {
        @Override
        public R apply() {
            return null;
        }

        @Override
        public R apply(R result) {
            return result;
        }
    }

    public static abstract class AReducingFunctionOn<R, A, B> implements Transducers.IReducingFunction<R, B> {

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

    public static abstract class ATransducer<B, C> implements Transducers.ITransducer<B, C> {
        @Override
        public <A> ITransducer<A, C> comp(final ITransducer<A, B> right) {
            return new ATransducer<A, C>() {
                @Override
                public <R> IReducingFunction<R, C> apply(IReducingFunction<R, A> xf) {
                    return ATransducer.this.apply(right.apply(xf));
                }
            };
        }
    }

    public static <R, T> IReducingFunction<R, T> completing(final IStepFunction<R, T> stepFunction) {
        return new AReducingFunction<R, T>() {
            @Override
            public R apply(R result, T input, IReduced reduced) {
                return stepFunction.apply(result, input, reduced);
            }
        };
    }

}
