package guava.ext.source;

import com.google.common.base.Throwables;

import java.util.Iterator;

/**
 * Author:  Fred Deng
 */
public class CloseableIteratorAuto<T> implements CloseableIterator<T> {

    private final Iterator<T> delegate;
    private final AutoCloseable closeable;

    public CloseableIteratorAuto(CloseableIterator<T> closeableIterator) {
        this(closeableIterator, closeableIterator);
    }

    public CloseableIteratorAuto(Iterator<T> delegate, AutoCloseable closeable) {
        this.delegate = delegate;
        this.closeable = closeable;
    }

    @Override
    public void close() {
        try {
            closeable.close();
        } catch (Exception e) {
            Throwables.propagate(e);
        }
    }

    @Override
    public boolean hasNext() {
        return delegate.hasNext();
    }

    @Override
    public T next() {
        return delegate.next();
    }

    @Override
    public void remove() {
        delegate.remove();
    }
}
