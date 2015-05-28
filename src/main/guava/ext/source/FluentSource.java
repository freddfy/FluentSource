package guava.ext.source;

import com.google.common.base.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.io.CharSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;


/**
 * Extension to CharSource for "Object" source, which is sourcing from CharSource.
 * <p/>
 * APIs are similar to CharSource adhere to "tell rather than ask", so that underlying reader close will not be
 * cluttered in client codes.
 * <p/>
 * In addition, FluentSource also provides common higher order functions for transformation, filtering...etc.
 * <p/>
 * Author:  Fred Deng
 */
public abstract class FluentSource<T> {

    public static FluentSource<String> on(final CharSource source, final Pattern delimeter) {
        return new FluentSource<String>() {
            @Override
            public CloseableIterator<String> openIterator() {
                try {
                    Scanner scanner = new Scanner(source.openStream()).useDelimiter(delimeter);
                    return new CloseableIteratorAdaptor<String>(scanner, scanner);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        };
    }

    public static FluentSource<String> on(final CharSource source, final String delimeter) {
        return on(source, Pattern.compile(delimeter));
    }

    public static FluentSource<String> onLines(final CharSource lineSource) {
        return on(lineSource, "[\\n\\r]");
    }

    public static FluentSource<Map<String, String>> onCsv(CharSource csvSource) {
        FluentSource<String> lines = onLines(csvSource);
        return lines.transform(new Supplier<Function<String, Map<String, String>>>() {
            @Override
            public Function<String, Map<String, String>> get() {
                return new Function<String, Map<String, String>>() {
                    List<String> headers;
                    @Override
                    public Map<String, String> apply(String input) {
                        if(headers == null) {
                            headers = split(input);
                            return null;
                        }
                        return zip(headers, split(input));
                    }

                    private List<String> split(String input) {
                        return Splitter.on(',').splitToList(input);
                    }

                    private Map<String, String> zip(List<String> headers, List<String> values) {
                        if (headers.size() != values.size()) {
                            throw new IllegalArgumentException("csv header and value size not matched at " + values);
                        }
                        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
                        for (int i = 0; i < headers.size(); i++) {
                            builder.put(headers.get(i), values.get(i));
                        }
                        return builder.build();
                    }
                };
            }
        }).filter(Predicates.not(Predicates.<Map<String, String>>isNull()));
    }

    public abstract CloseableIterator<T> openIterator();

    public List<T> readAll() {
        CloseableIterator<T> iter = null;
        try {
            iter = openIterator();
            return ImmutableList.copyOf(iter);
        } finally {
            if (iter != null) {
                iter.close();
            }
        }
    }

    public <R> R readAll(SourceProcessor<T, R> processor) {
        CloseableIterator<T> iter = null;
        try{
            iter = openIterator();
            while (iter.hasNext() && processor.process(iter.next())) {}
        } finally {
            if (iter != null) {
                iter.close();
            }
        }
        return processor.getResult();
    }

    public <T2> FluentSource<T2> transform(final Function<T, T2> function) {
        return transform(Suppliers.ofInstance(function));
    }

    public <T2> FluentSource<T2> transform(final Supplier<Function<T, T2>> functionSupplier) {
        return new FluentSource<T2>() {
            @Override
            public CloseableIterator<T2> openIterator() {
                Function<T, T2> function = functionSupplier.get();
                CloseableIterator<T> origIter = origIter();
                return new CloseableIteratorAdaptor<T2>(Iterators.transform(origIter, function), origIter);
            }
        };
    }

    private CloseableIterator<T> origIter() {
        return openIterator();
    }

    public FluentSource<T> filter(final Predicate<T> predicate) {
        return new FluentSource<T>() {
            @Override
            public CloseableIterator<T> openIterator() {
                CloseableIterator<T> origIter = origIter();
                return new CloseableIteratorAdaptor<T>(Iterators.filter(origIter, predicate), origIter);

            }
        };
    }

    @SuppressWarnings("unchecked")
    public static <T> FluentSource<T> concat(FluentSource<? extends T>... sources) {
        return concat(Arrays.asList(sources));
    }

    public static <T> FluentSource<T> concat(final Iterable<FluentSource<? extends T>> sources) {
        return new FluentSource<T>() {

            @Override
            public CloseableIterator<T> openIterator() {
                ToAutoClosingIterator<T> closingIterators = new ToAutoClosingIterator<T>();
                return new CloseableIteratorAdaptor<T>(Iterators.concat(Iterators.transform(sources.iterator(), closingIterators)), closingIterators);
            }
        };
    }

    private static class ToAutoClosingIterator<T> implements Function<FluentSource<? extends T>, CloseableIterator<T>>, AutoCloseable {
        private final List<AutoCloseable> opened = Lists.newArrayList();
        private boolean closed = false;

        public void close() throws Exception {
            closed = true;
            Exception ex = null;
            for (AutoCloseable closeable : opened) {
                try {
                    closeable.close();
                } catch (Exception e) {
                    if (ex == null) ex = e;
                }
            }
            if (ex != null) {
                throw ex;
            }
        }

        @Override
        public CloseableIterator<T> apply(FluentSource<? extends T> input) {
            if (closed) {
                throw new IllegalStateException("Already closed");
            }
            CloseableIterator<? extends T> curr = input.openIterator();
            opened.add(curr);
            return new CloseableIteratorDecorator<T>(curr);
        }

    }
}
