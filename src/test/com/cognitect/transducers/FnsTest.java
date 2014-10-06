package com.cognitect.transducers;

import junit.framework.TestCase;

import java.util.*;
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

    public void testTakeNth() throws Exception {
        ITransducer<Integer, Integer> xf = takeNth(2);
        List<Integer> evens = transduce(xf, new IStepFunction<List<Integer>, Integer>() {
            @Override
            public List<Integer> apply(List<Integer> result, Integer input, AtomicBoolean reduced) {
                result.add(input);
                return result;
            }
        }, new ArrayList<Integer>(), ints(10));

        Integer[] expected = {0,2,4,6,8};

        assertTrue(evens.equals(Arrays.asList(expected)));
    }

    public void testReplace() throws Exception {
        ITransducer<Integer, Integer> xf = replace(new HashMap<Integer, Integer>() {{ put(3, 42); }});
        List<Integer> evens = transduce(xf, new IStepFunction<List<Integer>, Integer>() {
            @Override
            public List<Integer> apply(List<Integer> result, Integer input, AtomicBoolean reduced) {
                result.add(input);
                return result;
            }
        }, new ArrayList<Integer>(), ints(5));

        Integer[] expected = {0,1,2,42,4};

        assertTrue(evens.equals(Arrays.asList(expected)));
    }

    public void testKeep() throws Exception {
        ITransducer<Integer, Integer> xf = keep(new Function<Integer, Integer>() {
            @Override
            public Integer apply(Integer integer) {
                return (integer % 2 == 0) ? null : integer;
            }
        });

        List<Integer> odds = transduce(xf, new IStepFunction<List<Integer>, Integer>() {
            @Override
            public List<Integer> apply(List<Integer> result, Integer input, AtomicBoolean reduced) {
                result.add(input);
                return result;
            }
        }, new ArrayList<Integer>(), ints(10));

        Integer[] expected = {1,3,5,7,9};

        assertTrue(odds.equals(Arrays.asList(expected)));
    }

    public void testKeepIndexed() throws Exception {
        ITransducer<Integer, Integer> xf = keepIndexed(new BiFunction<Long, Integer, Integer>() {
            @Override
            public Integer apply(Long idx, Integer integer) {
                return (idx == 1l || idx == 4l) ? integer : null;
            }
        });

        List<Integer> nums = transduce(xf, new IStepFunction<List<Integer>, Integer>() {
            @Override
            public List<Integer> apply(List<Integer> result, Integer input, AtomicBoolean reduced) {
                result.add(input);
                return result;
            }
        }, new ArrayList<Integer>(), ints(10));

        Integer[] expected = {0,3};

        assertTrue(nums.equals(Arrays.asList(expected)));
    }

    public void testDedupe() throws Exception {
        Integer[] seed = {1,2,2,3,4,5,5,5,5,5,5,5,0};
        ITransducer<Integer, Integer> xf = dedupe();

        List<Integer> nums = transduce(xf, new IStepFunction<List<Integer>, Integer>() {
            @Override
            public List<Integer> apply(List<Integer> result, Integer input, AtomicBoolean reduced) {
                result.add(input);
                return result;
            }
        }, new ArrayList<Integer>(), Arrays.asList(seed));

        Integer[] expected = {1,2,3,4,5,0};

        assertTrue(nums.equals(Arrays.asList(expected)));
    }

    public void testPartitionBy() throws Exception {
        Integer[] seed = {1,1,1,2,2,3,4,5,5};

        ITransducer<Iterable<Integer>, Integer> xf = partitionBy(new Function<Integer, Integer>() {
            @Override
            public Integer apply(final Integer integer) {
                return integer;
            }
        });

        List<List<Integer>> vals = transduce(xf, new IStepFunction<List<List<Integer>>, Iterable<Integer>>() {
            @Override
            public List<List<Integer>> apply(List<List<Integer>> result, Iterable<Integer> input, AtomicBoolean reduced) {
                List<Integer> ret = new ArrayList<Integer>();
                for (Integer i : input) {
                    ret.add(i);
                }
                result.add(ret);
                return result;
            }
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

        List<List<Integer>> vals = transduce(xf, new IStepFunction<List<List<Integer>>, Iterable<Integer>>() {
            @Override
            public List<List<Integer>> apply(List<List<Integer>> result, Iterable<Integer> input, AtomicBoolean reduced) {
                List<Integer> ret = new ArrayList<Integer>();
                for (Integer i : input) {
                    ret.add(i);
                }
                result.add(ret);
                return result;
            }
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

    public void testCollectionCovariance() throws Exception {
        ITransducer<List<Integer>, Set<Integer>> m = map(new Function<Set<Integer>, List<Integer>>() {
            @Override
            public List<Integer> apply(Set<Integer> set) {
                List<Integer> l = new ArrayList<Integer>(set.size());
                for(Integer i : set) {
                    l.add(i);
                }
                return l;
            }
        });

        List<Set<Integer>> input = new ArrayList<Set<Integer>>() {{
            add(new HashSet<Integer>() {{
                for (int i : ints(20)) {
                    add(i);
                }
            }});
            add(new HashSet<Integer>() {{
                for(int i : ints(5)) {
                    add(i);
                }
            }});
        }};

        Collection<Integer> res = transduce(m, new IStepFunction<Collection<Integer>, List<Integer>>() {
            @Override
            public Collection<Integer> apply(Collection<Integer> result, List<Integer> input, AtomicBoolean reduced) {
                result.add(input.size());
                return result;
            }
        }, new ArrayList<Integer>(input.size()), input);

        System.out.println(res);
    }


    public void testSimpleCovariance() throws Exception {
        ITransducer<Integer, Integer> m = map(new Function<Integer, Integer>() {
            @Override
            public Integer apply(Integer i) {
                return i * 2;
            }
        });

        List<Integer> input = new ArrayList<Integer>() {{
            for(int i : ints(20)) {
                add(i);
            }
        }};

        Collection<Number> res = transduce(m, new IStepFunction<Collection<Number>, Integer>() {
            @Override
            public Collection<Number> apply(Collection<Number> result, Integer input, AtomicBoolean reduced) {
                result.add(input * 3);
                return result;
            }
        }, new ArrayList<Number>(input.size()), input);

        System.out.println(res);

        ITransducer<Number, Number> f = filter(new Predicate<Number>() {
            @Override
            public boolean test(Number number) {
                return number.doubleValue() > 10.0;
            }
        });

        // Neither of these composition statements compile
        //m.comp(f);
        //f.comp(m);
    }
}