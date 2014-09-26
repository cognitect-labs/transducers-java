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
        String s = transduce(
                Transducers.<String, Integer>map(i -> i.toString()),
                (result, input, reduced) -> result + input + " ",
                "",
                ints(10));

        System.out.println("map: " + s);
    }

    public void testFilter() throws Exception {

        Transducer<Integer, Integer> xf = filter(integer -> integer % 2 != 0);

        List<Integer> odds = transduce(
                filter(integer -> integer % 2 != 0),
                (result, input, reduced) -> {
                    result.add(input);
                    return result;},
                new ArrayList<Integer>(),
                ints(10));

        for(Integer i : odds) {
            System.out.println(i);
        }
    }

    public void testCat() throws Exception {

        List<Iterable<Integer>> data = new ArrayList<Iterable<Integer>>() {{
            add(ints(10));
            add(ints(20));
        }};

        List<Integer> vals = transduce(cat(), (result, input, reduced) -> {
            result.add(input);
            return result;
        }, new ArrayList<Integer>(), data);

        System.out.println("cat: " + vals.toString());
    }

    public void testMapcat() throws Exception {

        Transducer<Character, Integer> xf = mapcat(integer -> {
            final String s = integer.toString();
            return new ArrayList<Character>(s.length()) {{
                for(char c : s.toCharArray())
                add(c);
            }};
        });

        List<Character> vals = transduce(xf, (result, input, reduced) -> {
            result.add(input);
            return result;
        }, new ArrayList<Character>(), ints(10));

        System.out.println("mapcat: " + vals);
    }

    public void testComp() throws Exception {

        Transducer<String, Integer> xf =
                Transducers.<Integer>filter(integer -> integer % 2 != 0).comp(map(i -> i.toString()));

        List<String> odds = transduce(
                xf,
                (result, input, reduced) -> {
                    result.add(input);
                    return result;},
                new ArrayList<String>(),
                ints(10));

        for(String s : odds) {
            System.out.println(s);
        }
    }

    public void testTake() throws Exception {
        List<Integer> five = transduce(
                take(5),
                (result, input, reduced) -> {
                    result.add(input);
                    return result;},
                new ArrayList<Integer>(),
                ints(20));

        System.out.println(five);
    }

    public void testTakeWhile() throws Exception {
        List<Integer> ten = transduce(
                takeWhile(integer -> integer < 10),
                (result, input, reduced) -> {
                    result.add(input);
                    return result;},
                new ArrayList<Integer>(),
                ints(20));

        System.out.println(ten);
    }

    public void testSAM() throws Exception {
        map((Function) (s) -> { return s; });
        IStepFunction<List<Integer>, Integer> rsf = (result, input, reduced) -> { result.add(input); return result; };

    }
}