package guava.ext.source;

/**
 * Author:  Fred Deng
 */
public class CloseableIteratorDecorator<T> implements CloseableIterator<T> {

    private final CloseableIterator<? extends T> delegate;

    public CloseableIteratorDecorator(CloseableIterator<? extends T> delegate) {
        this.delegate = delegate;
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

    @Override
    public void close() {
       delegate.close();
    }
}
