## Synopsis
FluentSource is a Fluent API for marshalling [Guava CharSource]
(http://docs.guava-libraries.googlecode.com/git/javadoc/com/google/common/io/CharSource.html) into object sources.

## Code Examples
The following example demonstrate reading a char source and then transform into a integer source, and filter out the 
odd numbers then calculate the sum.

               int sumOfEven = FluentSource.on(CharSource.wrap("1,2,3,4,5"), ",")
                .transform(new Function<String, Integer>() {
                    @Override
                    public Integer apply(String input) {
                        return Integer.valueOf(input);
                    }
                })
                .filter(new Predicate<Integer>() {
                    @Override
                    public boolean apply(Integer input) {
                        return input % 2 == 0;
                    }
                })
                .readAll(new SourceProcessor<Integer, Integer>() {
                    private int sum;

                    @Override
                    public void process(Integer input) {
                        this.sum += input;
                    }

                    @Override
                    public Integer getResult() {
                        return sum;
                    }
                }); //6
                
The following example demonstrate reading a csv char source and transforming into a Map source

                FluentSource.onCsv(CharSource.wrap("a,b,c\n1,2,3")) //marshall into source of a single Map ["a":"1", "b":"2", "c":"3"]
                
More detailed examples available [here](https://github.com/freddfy/FluentSource/blob/master/src/test/guava/ext/source/FluentSourceExample.java)

## Motivation
The character stream in CharSource is usually too low level for coding business logic - we usually marshall them into
custom object stream for easier processing. Therefore the FluentSource is convenient here in providing higher order 
function for transforming the low-level CharSource into higher level "object" source. 

Also FluentSource adheres to the same 
[TellDontAsk](http://martinfowler.com/bliki/TellDontAsk.html) principle as CharSource so that the try with resource of 
the underlying Reader(Closeable) would not have to be cluttered in client usage.

## API Reference
All useful APIs in [FluentSource](https://github.com/freddfy/FluentSource/blob/master/src/main/FluentSource.java), requires JDK 1.6 or higher.