package com.cognitect.transducers;

import static com.cognitect.transducers.Fns.*;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.TestCase;

public class FnsTest extends TestCase {

	private List<Integer> ints(final int n) {
		return new ArrayList<Integer>(n) {
			{
				for (int i = 0; i < n; i++) {
					add(i);
				}
			}
		};
	}

	private List<Long> longs(final long n) {
		return new ArrayList<Long>((int) n) {
			{
				for (long i = 0l; i < n; i++) {
					add(i);
				}
			}
		};
	}

	private <T> ITransducer<String, T> stringify() {
		return map(new Function<T, String>() {
			@Override
			public String apply(T i) {
				return i.toString();
			}
		});
	}

	private <T extends Number> Predicate<T> odd() {
		return new Predicate<T>() {
			@Override
			public boolean test(T integer) {
				return integer.intValue() % 2 != 0;
			}
		};
	}

	private <T> IStepFunction<List<T>, T> addToList() {
		return new IStepFunction<List<T>, T>() {
			@Override
			public List<T> apply(List<T> result, T input, AtomicBoolean reduced) {
				result.add(input);
				return result;
			}
		};
	}

	private <T> IStepFunction<List<List<T>>, Iterable<T>> addToLists() {
		return new IStepFunction<List<List<T>>, Iterable<T>>() {
			@Override
			public List<List<T>> apply(List<List<T>> result, Iterable<T> input, AtomicBoolean reduced) {
				List<T> ret = new ArrayList<T>();
				for (T i : input) {
					ret.add(i);
				}
				result.add(ret);
				return result;
			}
		};
	}

	private <T> Function<T, T> identity() {
		return new Function<T, T>() {
			@Override
			public T apply(final T value) {
				return value;
			}
		};
	}

	private Predicate<Integer> lessThan(final int n) {
		return new Predicate<Integer>() {
			@Override
			public boolean test(Integer integer) {
				return integer < n;
			}
		};
	}

	private ArrayList<Integer> newIntList() {
		return new ArrayList<Integer>();
	}

	public void testMap() throws Exception {
		ITransducer<String, Integer> xf = stringify();

		String s = transduce(xf, new IStepFunction<String, String>() {
			@Override
			public String apply(String result, String input, AtomicBoolean reduced) {
				return result + input + " ";
			}
		}, "", ints(10));

		assertEquals(s, "0 1 2 3 4 5 6 7 8 9 ");

		ITransducer<Integer, Integer> xn = map(this.<Integer> identity());

		List<Integer> nums = transduce(xn, new IStepFunction<List<Integer>, Integer>() {
			@Override
			public List<Integer> apply(List<Integer> result, Integer input, AtomicBoolean reduced) {
				result.add(input + 1);
				return result;
			}
		}, newIntList(), ints(10));

		assertEquals(asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), nums);

		// README usage test
		List<String> sl = transduce(this.<Long> stringify(), this.<String> addToList(), new ArrayList<String>(),
				longs(10));

		assertEquals(asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"), sl);

	}

	public void testFilter() throws Exception {
		ITransducer<Integer, Integer> xf = filter(this.<Integer> odd());

		List<Integer> odds = transduce(xf, this.<Integer> addToList(), newIntList(), ints(10));

		assertEquals(asList(1, 3, 5, 7, 9), odds);
	}

	public void testCat() throws Exception {
		ITransducer<Integer, Iterable<Integer>> xf = cat();

		List<Iterable<Integer>> data = new ArrayList<Iterable<Integer>>() {
			{
				add(ints(10));
				add(ints(20));
			}
		};

		List<Integer> vals = transduce(xf, this.<Integer> addToList(), newIntList(), data);

		List<Integer> expected = new ArrayList<Integer>() {
			{
				addAll(ints(10));
				addAll(ints(20));
			}
		};

		assertEquals(expected, vals);
	}

	public void testMapcat() throws Exception {
		ITransducer<Character, Integer> xf = mapcat(new Function<Integer, Iterable<Character>>() {
			@Override
			public Iterable<Character> apply(Integer integer) {
				final String s = integer.toString();
				return new ArrayList<Character>(s.length()) {
					{
						for (char c : s.toCharArray())
							add(c);
					}
				};
			}
		});

		List<Character> vals = transduce(xf, this.<Character> addToList(), new ArrayList<Character>(), ints(10));

		assertEquals(asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9'), vals);
	}

	public void testComp() throws Exception {
		ITransducer<Integer, Integer> f = filter(this.<Integer> odd());

		ITransducer<String, Integer> xf = f.comp(stringify());

		List<String> odds = transduce(xf, this.<String> addToList(), new ArrayList<String>(), ints(10));

		assertEquals(asList("1", "3", "5", "7", "9"), odds);

		// README Usage tests

		ITransducer<Long, Long> filterOdds = filter(this.<Long> odd());

		ITransducer<String, Long> stringifyOdds = filterOdds.comp(stringify());

		List<String> sl = transduce(stringifyOdds, this.<String> addToList(), new ArrayList<String>(), longs(10));

		assertEquals(asList("1", "3", "5", "7", "9"), sl);
	}

	public void testTake() throws Exception {
		ITransducer<Integer, Integer> xf = take(5);
		List<Integer> five = transduce(xf, this.<Integer> addToList(), newIntList(), ints(20));

		assertEquals(asList(0, 1, 2, 3, 4), five);
	}

	public void testTakeWhile() throws Exception {
		ITransducer<Integer, Integer> xf = takeWhile(lessThan(10));
		List<Integer> ten = transduce(xf, this.<Integer> addToList(), newIntList(), ints(20));

		assertEquals(asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), ten);
	}

	public void testDrop() throws Exception {
		ITransducer<Integer, Integer> xf = drop(5);
		List<Integer> five = transduce(xf, this.<Integer> addToList(), newIntList(), ints(10));

		assertEquals(asList(5, 6, 7, 8, 9), five);
	}

	public void testDropWhile() throws Exception {
		ITransducer<Integer, Integer> xf = dropWhile(lessThan(10));
		List<Integer> ten = transduce(xf, this.<Integer> addToList(), newIntList(), ints(20));

		assertEquals(asList(10, 11, 12, 13, 14, 15, 16, 17, 18, 19), ten);
	}

	public void testTakeNth() throws Exception {
		ITransducer<Integer, Integer> xf = takeNth(2);
		List<Integer> evens = transduce(xf, this.<Integer> addToList(), newIntList(), ints(10));

		assertEquals(asList(0, 2, 4, 6, 8), evens);
	}

	public void testReplace() throws Exception {
		ITransducer<Integer, Integer> xf = replace(new HashMap<Integer, Integer>() {
			{
				put(3, 42);
			}
		});
		List<Integer> evens = transduce(xf, this.<Integer> addToList(), newIntList(), ints(5));

		assertEquals(asList(0, 1, 2, 42, 4), evens);
	}

	public void testKeep() throws Exception {
		ITransducer<Integer, Integer> xf = keep(new Function<Integer, Integer>() {
			@Override
			public Integer apply(Integer integer) {
				return (integer % 2 == 0) ? null : integer;
			}
		});

		List<Integer> odds = transduce(xf, this.<Integer> addToList(), newIntList(), ints(10));

		assertEquals(asList(1, 3, 5, 7, 9), odds);
	}

	public void testKeepIndexed() throws Exception {
		ITransducer<Integer, Integer> xf = keepIndexed(new BiFunction<Long, Integer, Integer>() {
			@Override
			public Integer apply(Long idx, Integer integer) {
				return (idx == 1l || idx == 4l) ? integer : null;
			}
		});

		List<Integer> nums = transduce(xf, this.<Integer> addToList(), newIntList(), ints(10));

		assertEquals(asList(0, 3), nums);
	}

	public void testDedupe() throws Exception {
		List<Integer> seed = asList(1, 2, 2, 3, 4, 5, 5, 5, 5, 5, 5, 5, 0);
		ITransducer<Integer, Integer> xf = dedupe();

		List<Integer> nums = transduce(xf, this.<Integer> addToList(), newIntList(), seed);

		assertEquals(asList(1, 2, 3, 4, 5, 0), nums);
	}

	public void testPartitionBy() throws Exception {
		List<Integer> seed = asList(1, 1, 1, 2, 2, 3, 4, 5, 5);

		ITransducer<Iterable<Integer>, Integer> xf = partitionBy(this.<Integer> identity());

		List<List<Integer>> vals = transduce(xf, this.<Integer> addToLists(), new ArrayList<List<Integer>>(), seed);

		List<List<Integer>> expected = asList(asList(1, 1, 1), asList(2, 2), asList(3), asList(4), asList(5, 5));

		assertEquals(vals, expected);
	}

	public void testPartitionAll() throws Exception {
		ITransducer<Iterable<Integer>, Integer> xf = partitionAll(3);

		List<List<Integer>> vals = transduce(xf, this.<Integer> addToLists(), new ArrayList<List<Integer>>(), ints(10));

		List<List<Integer>> expected = asList(asList(0, 1, 2), asList(3, 4, 5), asList(6, 7, 8), asList(9));

		assertEquals(expected, vals);
	}

	public void testSimpleCovariance() throws Exception {
		ITransducer<Integer, Integer> m = map(new Function<Integer, Integer>() {
			@Override
			public Integer apply(Integer i) {
				return i * 2;
			}
		});

		List<Integer> input = ints(20);

		List<Number> res1 = transduce(m, this.<Number> addToList(), new ArrayList<Number>(), input);

		assertEquals(20, res1.size());

		ITransducer<Number, Number> f = filter(new Predicate<Number>() {
			@Override
			public boolean test(Number number) {
				return number.doubleValue() > 10.0;
			}
		});

		List<Number> res2 = transduce(m.comp(f), this.<Number> addToList(), new ArrayList<Number>(input.size()), input);

		assertEquals(14, res2.size());
	}
}