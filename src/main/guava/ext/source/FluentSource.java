package guava.ext.source;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.io.CharSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


/**
 * Extension to CharSource for "Object" source, which is sourcing from CharSource.
 *
 * APIs are similar to CharSource adhere to "tell rather than ask", so that underlying reader close will not be
 * cluttered in client codes.
 *
 * In addition, FluentSource also provides common higher order functions for transformation, filtering...etc.
 *
 * Author:  Fred Deng
 */
public abstract class FluentSource<T> {

    public static FluentSource<String> byLines(final CharSource source) {
        return new FluentSource<String>() {
            @Override
            public CloseableIterator<String> openIterator() {
                try {
                    CloseableIteratorBufferedReader closeableIter = new CloseableIteratorBufferedReader(source.openBufferedStream());
                    return new CloseableIteratorAuto<>(closeableIter);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        };
    }

    public abstract CloseableIterator<T> openIterator();

    public List<T> readAll(){
        try(CloseableIterator<T> iter = openIterator()){
            return ImmutableList.copyOf(iter);
        }
    }

    public <R> R readAll(SourceProcessor<T, R> processor) {
        try(CloseableIterator<T> iter = openIterator()) {
            while (iter.hasNext()) {
                processor.process(iter.next());
            }
        }
        return processor.getResult();
    }

    public <T2> FluentSource<T2> transform(final Function<T, T2> function) {
        return new FluentSource<T2>() {
            @Override
            public CloseableIterator<T2> openIterator() {
                CloseableIterator<T> origIter = origIter();
                return new CloseableIteratorAuto<>(Iterators.transform(origIter, function), origIter);
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
                return new CloseableIteratorAuto<>(Iterators.filter(origIter, predicate), origIter);

            }
        };
    }

    @SuppressWarnings("unchecked")
    public static <T> FluentSource<T> concat(FluentSource<? extends T>... sources) {
        return concat(Arrays.asList(sources));
    }

    public static <T> FluentSource<T> concat(final Iterable<FluentSource<? extends T>> sources) {
        return new FluentSource<T>(){

            @Override
            public CloseableIterator<T> openIterator() {
                ToAutoClosingIterator<T> closingIterators = new ToAutoClosingIterator<>();
                return new CloseableIteratorAuto<>(Iterators.concat(Iterators.transform(sources.iterator(), closingIterators)), closingIterators);
            }
        };
    }

    private static class ToAutoClosingIterator<T> implements Function<FluentSource<? extends T>, CloseableIterator<T>>, AutoCloseable{
        private final List<AutoCloseable> opened = Lists.newArrayList();
        private boolean closed = false;

        public void close() throws Exception {
            closed = true;
            Exception ex = null;
            for (AutoCloseable closeable : opened) {
                try {
                    closeable.close();
                } catch (Exception e) {
                    if(ex == null) ex = e;
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
            return new CloseableIteratorDecorator<>(curr);
        }

    }
}
