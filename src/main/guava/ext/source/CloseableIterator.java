package guava.ext.source;

import java.io.Closeable;
import java.util.Iterator;

/**
 * Author:  Fred Deng
 */
public interface CloseableIterator<T> extends Iterator<T>, Closeable {
    /**
     * Overrides to suppress the Exception in method signature
     */
    @Override void close();
}
