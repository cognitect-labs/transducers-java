# transducers-java

Transducers are composable algorithmic transformations. They are independent from the context of their input and output sources and specify only the essence of the transformation in terms of an individual element. Because transducers are decoupled from input or output sources, they can be used in many different processes - collections, streams, channels, observables, etc. Transducers compose directly, without awareness of input or creation of intermediate aggregates.

Also see the introductory [blog post](http://blog.cognitect.com/blog/2014/8/6/transducers-are-coming) and this [video](https://www.youtube.com/watch?v=6mTbuzafcII).

## Releases and Dependency Information

* Latest release: 0.1.0
* [All Released Versions](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.cognitect%22%20AND%20a%3A%22transducers-java%22)

[Maven](http://maven.apache.org/) dependency information:

```xml
<dependency>
  <groupId>com.cognitect</groupId>
  <artifactId>transducers-java</artifactId>
  <version>0.1.0</version>
</dependency>
```

## Usage

Most of the 

```java
import static com.cognitect.transducers.Fns.*;
```

TODO

# Transducing functions

TODO

```java
ITransducer<String, Long> stringify = map(new Function<Long, String>() {
    @Override
    public String apply(Long i) {
        return i.toString();
    }
});
```

TODO - 

```java
IStepFunction<List<String>, String> addString = new IStepFunction<List<String>, String>() {
    @Override
    public List<String> apply(List<String> result, String input, AtomicBoolean reduced) {
        result.add(input);
        return result;
    }
};
```

The `addString` function supplies the knowledge of how to accumulate the result of an operation.  In this case, `addString` accepts a list and a `String` instance and adds it to the end of the list.  One of the most common ways to apply transducers is with the `com.cognitect.transducers.Fns#transduce` function, which is analogous to a standard `reduce` or `foldl` function found in many functional programming languages.  

```java
transduce(stringify, addString, new ArrayList<String>(), longs(10));

//=> ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9"]
```

### Composing transducers

TODO

```java
ITransducer<Long, Long> filterOdds = filter(new Predicate<Long>() {
    @Override
    public boolean test(Long num) {
        return num.longValue() % 2 != 0;
    }
});			
```

Transducers are composed using the `com.cognitect.transducers.Fns#compose` method:

```java
ITransducer<String, Long> stringifyOdds = filterOdds.comp(stringify);
```

The transducer `stringifyOdds` is a transformation stack that will be applied by a process to a series of input elements. Each function in the stack is performed before the operation it wraps.

```java
transduce(stringifyOdds, addString, new ArrayList<String>(), longs(10));

//=> ["1", "3", "5", "7", "9"]
```

TODO

## Contributing 

This library is open source, developed internally by [Cognitect](http://cognitect.com). Issues can be filed using [GitHub Issues](https://github.com/cognitect-labs/transducers-java/issues).

This project is provided without support or guarantee of continued development.
Because transducers-java may be incorporated into products or client projects, we prefer to do development internally and do not accept pull requests or patches. 

## Copyright and License

Copyright © 2014 Cognitect

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
