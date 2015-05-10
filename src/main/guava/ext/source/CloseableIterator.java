package guava.ext.source;

import java.util.Iterator;

/**
 * Author:  Fred Deng
 */
public interface CloseableIterator<T> extends Iterator<T>, AutoCloseable{
    /**
     * Overrides to suppress the Exception in method signature
     */
    @Override void close();
}
