package com.cognitect.transducers;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
        ITransducer<String, Integer> xf = map(i -> i.toString());

        String s = transduce(xf, (result, input, reduced) -> result + input + " ", "", ints(10));

        assertEquals(s, "0 1 2 3 4 5 6 7 8 9 ");

        ITransducer<Integer, Integer> xn = map(i -> i);

        List<Integer> nums = transduce(xn, (result, input, reduced) -> {
            result.add(input + 1);
            return result;
        }, new ArrayList<Integer>(), ints(10));

        Integer[] expected = {1,2,3,4,5,6,7,8,9,10};

        assertTrue(nums.equals(Arrays.asList(expected)));

    }

    public void testFilter() throws Exception {

        ITransducer<Integer, Integer> xf = filter(integer -> integer.intValue() % 2 != 0);

        List<Integer> odds = transduce(xf, (result, input, reduced) -> {
            result.add(input);
            return result;
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

        List<Integer> vals = transduce(xf, (result, input, reduced) -> {
            result.add(input);
            return result;
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
        ITransducer<Character, Integer> xf = mapcat(integer -> {
            final String s = integer.toString();
            return new ArrayList<Character>(s.length()) {{
                for (char c : s.toCharArray())
                    add(c);
            }};
        });

        List<Character> vals = transduce(xf, (result, input, reduced) -> {
            result.add(input);
            return result;
        }, new ArrayList<Character>(), ints(10));

        Character[] expected = {'0','1','2','3','4','5','6','7','8','9'};

        assertTrue(vals.equals(Arrays.asList(expected)));
    }

    public void testComp() throws Exception {
        ITransducer<Integer, Integer> f = filter(integer -> integer.intValue() % 2 != 0);

        ITransducer<String, Integer> m = map(i -> i.toString());

        ITransducer<String, Integer> xf = f.comp(m);

        List<String> odds = transduce(xf, (result, input, reduced) -> {
            result.add(input);
            return result;
        }, new ArrayList<String>(), ints(10));

        String[] expected = {"1","3","5","7","9"};

        assertTrue(odds.equals(Arrays.asList(expected)));
    }

    public void testTake() throws Exception {
        ITransducer<Integer, Integer> xf = take(5);
        List<Integer> five = transduce(xf, (result, input, reduced) -> {
            result.add(input);
            return result;
        }, new ArrayList<Integer>(), ints(20));

        Integer[] expected = {0,1,2,3,4};

        assertTrue(five.equals(Arrays.asList(expected)));
    }

    public void testTakeWhile() throws Exception {
        ITransducer<Integer, Integer> xf = takeWhile(integer -> integer < 10);
        List<Integer> ten = transduce(xf, (result, input, reduced) -> {
            result.add(input);
            return result;
        }, new ArrayList<Integer>(), ints(20));

        Integer[] expected = {0,1,2,3,4,5,6,7,8,9};

        assertTrue(ten.equals(Arrays.asList(expected)));
    }

    public void testDrop() throws Exception {
        ITransducer<Integer, Integer> xf = drop(5);
        List<Integer> five = transduce(xf, (result, input, reduced) -> {
            result.add(input);
            return result;
        }, new ArrayList<Integer>(), ints(10));

        Integer[] expected = {5,6,7,8,9};

        assertTrue(five.equals(Arrays.asList(expected)));
    }

    public void testDropWhile() throws Exception {
        ITransducer<Integer, Integer> xf = dropWhile(integer -> integer < 10);
        List<Integer> ten = transduce(xf, (result, input, reduced) -> {
            result.add(input);
            return result;
        }, new ArrayList<Integer>(), ints(20));

        Integer[] expected = {10,11,12,13,14,15,16,17,18,19};

        assertTrue(ten.equals(Arrays.asList(expected)));
    }

    public void testTakeNth() throws Exception {
        ITransducer<Integer, Integer> xf = takeNth(2);
        List<Integer> evens = transduce(xf, (result, input, reduced) -> {
            result.add(input);
            return result;
        }, new ArrayList<Integer>(), ints(10));

        Integer[] expected = {0,2,4,6,8};

        assertTrue(evens.equals(Arrays.asList(expected)));
    }

    public void testReplace() throws Exception {
        ITransducer<Integer, Integer> xf = replace(new HashMap<Integer, Integer>() {{ put(3, 42); }});
        List<Integer> evens = transduce(xf, (result, input, reduced) -> {
            result.add(input);
            return result;
        }, new ArrayList<Integer>(), ints(5));

        Integer[] expected = {0,1,2,42,4};

        assertTrue(evens.equals(Arrays.asList(expected)));
    }

    public void testKeep() throws Exception {
        ITransducer<Integer, Integer> xf = keep(integer -> (integer % 2 == 0) ? null : integer);

        List<Integer> odds = transduce(xf, (result, input, reduced) -> {
            result.add(input);
            return result;
        }, new ArrayList<Integer>(), ints(10));

        Integer[] expected = {1,3,5,7,9};

        assertTrue(odds.equals(Arrays.asList(expected)));
    }

    public void testKeepIndexed() throws Exception {
        ITransducer<Integer, Integer> xf = keepIndexed((idx, integer) -> (idx == 1l || idx == 4l) ? integer : null);

        List<Integer> nums = transduce(xf, (result, input, reduced) -> {
            result.add(input);
            return result;
        }, new ArrayList<Integer>(), ints(10));

        Integer[] expected = {0,3};

        assertTrue(nums.equals(Arrays.asList(expected)));
    }

    public void testDedupe() throws Exception {
        Integer[] seed = {1,2,2,3,4,5,5,5,5,5,5,5,0};
        ITransducer<Integer, Integer> xf = dedupe();

        List<Integer> nums = transduce(xf, (result, input, reduced) -> {
            result.add(input);
            return result;
        }, new ArrayList<Integer>(), Arrays.asList(seed));

        Integer[] expected = {1,2,3,4,5,0};

        assertTrue(nums.equals(Arrays.asList(expected)));
    }

    public void testPartitionBy() throws Exception {
        Integer[] seed = {1,1,1,2,2,3,4,5,5};

        ITransducer<Iterable<Integer>, Integer> xf = partitionBy(integer -> integer);

        List<List<Integer>> vals = transduce(xf, (result, input, reduced) -> {
            List<Integer> ret = new ArrayList<Integer>();
            for (Integer i : input) {
                ret.add(i);
            }
            result.add(ret);
            return result;
        }, new ArrayList<List<Integer>>(), Arrays.asList(seed));

        final Integer[] a = {1,1,1};
        final Integer[] b = {2,2};
        final Integer[] c = {3};
        final Integer[] d = {4};
        final Integer[] e = {5,5};

        List<List<Integer>> expected = new ArrayList<List<Integer>>() {{
            add(Arrays.asList(a));
            add(Arrays.asList(b));
            add(Arrays.asList(c));
            add(Arrays.asList(d));
            add(Arrays.asList(e));
        }};

        assertTrue(vals.size() == 5);

        for(int i=0; i<expected.size(); i++) {
            assertEquals(vals.get(i).size(),expected.get(i).size());
            assertTrue(vals.get(i).equals(expected.get(i)));
        }
    }

    public void testPartitionAll() throws Exception {
        ITransducer<Iterable<Integer>, Integer> xf = partitionAll(3);

        List<List<Integer>> vals = transduce(xf, (result, input, reduced) -> {
            List<Integer> ret = new ArrayList<Integer>();
            for (Integer i : input) {
                ret.add(i);
            }
            result.add(ret);
            return result;
        }, new ArrayList<List<Integer>>(), ints(10));

        final Integer[] a = {0,1,2};
        final Integer[] b = {3,4,5};
        final Integer[] c = {6,7,8};
        final Integer[] d = {9};

        List<List<Integer>> expected = new ArrayList<List<Integer>>() {{
            add(Arrays.asList(a));
            add(Arrays.asList(b));
            add(Arrays.asList(c));
            add(Arrays.asList(d));
        }};

        assertTrue(vals.size() == 4);

        for(int i=0; i<expected.size(); i++) {
            assertEquals(vals.get(i).size(),expected.get(i).size());
            assertTrue(vals.get(i).equals(expected.get(i)));
        }
    }
}