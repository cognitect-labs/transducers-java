package com.cognitect.transducers;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.cognitect.transducers.Fns.*;

//import static com.cognitect.transducers.Base.*;

public class FnsTest extends TestCase {

    private List<Integer> ints(final int n) {
        return new ArrayList<Integer>(n) {{
            for(int i = 0; i < n; i++) {
                add(i);
            }
        }};
    }

    public void testMap() throws Exception {
        ITransducer<String, Integer> xf = map(new Function<Integer, String>() {
            @Override
            public String apply(Integer i) {
                return i.toString();
            }
        });

        String s = transduce(xf, new IStepFunction<String, String>() {
            @Override
            public String apply(String result, String input, AtomicBoolean reduced) {
                return result + input + " ";
            }
        }, "", ints(10));

        assertEquals(s, "0 1 2 3 4 5 6 7 8 9 ");

        ITransducer<Integer, Integer> xn = map(new Function<Integer, Integer>() {
            @Override
            public Integer apply(Integer i) {
                return i;
            }
        });

        List<Integer> nums = transduce(xn, new IStepFunction<List<Integer>, Integer>() {
            @Override
            public List<Integer> apply(List<Integer> result, Integer input, AtomicBoolean reduced) {
                result.add(input + 1);
                return result;
            }
        }, new ArrayList<Integer>(), ints(10));

        Integer[] expected = {1,2,3,4,5,6,7,8,9,10};

        assertTrue(nums.equals(Arrays.asList(expected)));

    }

    public void testFilter() throws Exception {

        ITransducer<Integer, Integer> xf = filter(new Predicate<Integer>() {
            @Override
            public boolean test(Integer integer) {
                return integer.intValue() % 2 != 0;
            }
        });

        List<Integer> odds = transduce(xf, new IStepFunction<ArrayList<Integer>, Integer>() {
            @Override
            public ArrayList<Integer> apply(ArrayList<Integer> result, Integer input, AtomicBoolean reduced) {
                result.add(input);
                return result;
            }
        }, new ArrayList<Integer>(), ints(10));

        Integer[] expected = {1,3,5,7,9};

        assertTrue(odds.equals(Arrays.asList(expected)));
    }

    public void testCat() throws Exception {
        ITransducer<Integer, Iterable<Integer>> xf = cat();
        List<Iterable<Integer>> data = new ArrayList<Iterable<Integer>>() {{
            add(ints(10));
            add(ints(20));
        }};

        List<Integer> vals = transduce(xf, new IStepFunction<List<Integer>, Integer>() {
                    @Override
                    public List<Integer> apply(List<Integer> result, Integer input, AtomicBoolean reduced) {
                        result.add(input);
                        return result;
                    }
                }, new ArrayList<Integer>(), data);

        int i=0;
        List<Integer> nums = ints(10);

        for(int j=0; j<nums.size(); j++) {
            assertEquals((int)nums.get(j), (int)vals.get(i));
            i += 1;
        }

        nums = ints(20);

        for(int j=0; j<nums.size(); j++) {
            assertEquals((int)nums.get(j), (int)vals.get(i));
            i += 1;
        }
    }

    public void testMapcat() throws Exception {

        ITransducer<Character, Integer> xf = mapcat(new Function<Integer, Iterable<Character>>() {
            @Override
            public Iterable<Character> apply(Integer integer) {
                final String s = integer.toString();
                return new ArrayList<Character>(s.length()) {{
                    for (char c : s.toCharArray())
                        add(c);
                }};
            }
        });

        List<Character> vals = transduce(xf, new IStepFunction<List<Character>, Character>() {
            @Override
            public List<Character> apply(List<Character> result, Character input, AtomicBoolean reduced) {
                result.add(input);
                return result;
            }
        }, new ArrayList<Character>(), ints(10));

        Character[] expected = {'0','1','2','3','4','5','6','7','8','9'};

        assertTrue(vals.equals(Arrays.asList(expected)));
    }

    public void testComp() throws Exception {
        ITransducer<Integer, Integer> f = filter(new Predicate<Integer>() {
            @Override
            public boolean test(Integer integer) {
                return integer.intValue() % 2 != 0;
            }
        });

        ITransducer<String, Integer> m = map(new Function<Integer, String>() {
            @Override
            public String apply(Integer i) {
                return i.toString();
            }
        });

        ITransducer<String, Integer> xf = f.comp(m);

        List<String> odds = transduce(xf, new IStepFunction<List<String>, String>() {
            @Override
            public List<String> apply(List<String> result, String input, AtomicBoolean reduced) {
                result.add(input);
                return result;
            }
        }, new ArrayList<String>(), ints(10));

        String[] expected = {"1","3","5","7","9"};

        assertTrue(odds.equals(Arrays.asList(expected)));
    }

    public void testTake() throws Exception {
        ITransducer<Integer, Integer> xf = take(5);
        List<Integer> five = transduce(xf, new IStepFunction<List<Integer>, Integer>() {
            @Override
            public List<Integer> apply(List<Integer> result, Integer input, AtomicBoolean reduced) {
                result.add(input);
                return result;
            }
        }, new ArrayList<Integer>(), ints(20));

        Integer[] expected = {0,1,2,3,4};

        assertTrue(five.equals(Arrays.asList(expected)));
    }

    public void testTakeWhile() throws Exception {
        ITransducer<Integer, Integer> xf = takeWhile(new Predicate<Integer>() {
            @Override
            public boolean test(Integer integer) {
                return integer < 10;
            }
        });
        List<Integer> ten = transduce(xf, new IStepFunction<List<Integer>, Integer>() {
            @Override
            public List<Integer> apply(List<Integer> result, Integer input, AtomicBoolean reduced) {
                result.add(input);
                return result;
            }
        }, new ArrayList<Integer>(), ints(20));

        Integer[] expected = {0,1,2,3,4,5,6,7,8,9};

        assertTrue(ten.equals(Arrays.asList(expected)));
    }

    public void testDrop() throws Exception {
        ITransducer<Integer, Integer> xf = drop(5);
        List<Integer> five = transduce(xf, new IStepFunction<List<Integer>, Integer>() {
            @Override
            public List<Integer> apply(List<Integer> result, Integer input, AtomicBoolean reduced) {
                result.add(input);
                return result;
            }
        }, new ArrayList<Integer>(), ints(10));

        Integer[] expected = {5,6,7,8,9};

        assertTrue(five.equals(Arrays.asList(expected)));
    }

    public void testDropWhile() throws Exception {
        ITransducer<Integer, Integer> xf = dropWhile(new Predicate<Integer>() {
            @Override
            public boolean test(Integer integer) {
                return integer < 10;
            }
        });
        List<Integer> ten = transduce(xf, new IStepFunction<List<Integer>, Integer>() {
            @Override
            public List<Integer> apply(List<Integer> result, Integer input, AtomicBoolean reduced) {
                result.add(input);
                return result;
            }
        }, new ArrayList<Integer>(), ints(20));

        Integer[] expected = {10,11,12,13,14,15,16,17,18,19};

        assertTrue(ten.equals(Arrays.asList(expected)));
    }
}