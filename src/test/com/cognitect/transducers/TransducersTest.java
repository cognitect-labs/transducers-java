package com.cognitect.transducers;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

import static com.cognitect.transducers.Transducers.*;

public class TransducersTest extends TestCase {

    private List<Integer> ints(final int n) {
        return new ArrayList<Integer>(n) {{
            for(int i = 0; i < n; i++) {
                add(i);
            }
        }};
    }

    public void testMap() throws Exception {
        Transducer<String, Integer> xf = map(new Function<Integer, String>() {
            @Override
            public String apply(Integer i) {
                return i.toString();
            }
        });

        String s = transduce(xf, new ReducingStepFunction<String, String>() {
            @Override
            public String apply(String result, String input, Reduced reduced) {
                return result + input + " ";
            }
        }, "", ints(10));

        System.out.println("map: " + s);
    }

    public void testFilter() throws Exception {

        Transducer<Integer, Integer> xf = filter(new Predicate<Integer>() {
            @Override
            public boolean test(Integer integer) {
                return integer.intValue() % 2 != 0;
            }
        });

        List<Integer> odds = transduce(xf, new ReducingStepFunction<ArrayList<Integer>, Integer>() {
            @Override
            public ArrayList<Integer> apply(ArrayList<Integer> result, Integer input, Reduced reduced) {
                result.add(input);
                return result;
            }
        }, new ArrayList<Integer>(), ints(10));

        System.out.println("filter:");
        for(Integer i : odds) {
            System.out.println(i);
        }
    }

    public void testCat() throws Exception {
        Transducer<Integer, Iterable<Integer>> xf = cat();
        List<Iterable<Integer>> data = new ArrayList<Iterable<Integer>>() {{
            add(ints(10));
            add(ints(20));
        }};

        List<Integer> vals = transduce(xf, new ReducingStepFunction<List<Integer>, Integer>() {
                    @Override
                    public List<Integer> apply(List<Integer> result, Integer input, Reduced reduced) {
                        result.add(input);
                        return result;
                    }
                }, new ArrayList<Integer>(), data);

        System.out.println("cat: " + vals.toString());
    }

    public void testMapcat() throws Exception {

        Transducer<Character, Integer> xf = mapcat(new Function<Integer, Iterable<Character>>() {
            @Override
            public Iterable<Character> apply(Integer integer) {
                final String s = integer.toString();
                return new ArrayList<Character>(s.length()) {{
                    for(char c : s.toCharArray())
                    add(c);
                }};
            }
        });

        List<Character> vals = transduce(xf, new ReducingStepFunction<List<Character>, Character>() {
            @Override
            public List<Character> apply(List<Character> result, Character input, Reduced reduced) {
                result.add(input);
                return result;
            }
        }, new ArrayList<Character>(), ints(10));

        System.out.println("mapcat: " + vals);
    }

    public void testComp() throws Exception {
        Transducer<Integer, Integer> f = filter(new Predicate<Integer>() {
            @Override
            public boolean test(Integer integer) {
                return integer.intValue() % 2 != 0;
            }
        });

        Transducer<String, Integer> m = map(new Function<Integer, String>() {
            @Override
            public String apply(Integer i) {
                return i.toString();
            }
        });

        Transducer<String, Integer> xf = f.comp(m);

        List<String> odds = transduce(xf, new ReducingStepFunction<List<String>, String>() {
            @Override
            public List<String> apply(List<String> result, String input, Reduced reduced) {
                result.add(input);
                return result;
            }
        }, new ArrayList<String>(), ints(10));

        for(String s : odds) {
            System.out.println(s);
        }
    }

    public void testTake() throws Exception {
        Transducer<Integer, Integer> xf = take(5);
        List<Integer> five = transduce(xf, new ReducingStepFunction<List<Integer>, Integer>() {
            @Override
            public List<Integer> apply(List<Integer> result, Integer input, Reduced reduced) {
                result.add(input);
                return result;
            }
        }, new ArrayList<Integer>(), ints(20));

        System.out.println(five);
    }

    public void testTakeWhile() throws Exception {
        Transducer<Integer, Integer> xf = takeWhile(new Predicate<Integer>() {
            @Override
            public boolean test(Integer integer) {
                return integer < 10;
            }
        });
        List<Integer> ten = transduce(xf, new ReducingStepFunction<List<Integer>, Integer>() {
            @Override
            public List<Integer> apply(List<Integer> result, Integer input, Reduced reduced) {
                result.add(input);
                return result;
            }
        }, new ArrayList<Integer>(), ints(20));

        System.out.println(ten);
    }
}