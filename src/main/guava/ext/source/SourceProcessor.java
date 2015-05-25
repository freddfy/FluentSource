package guava.ext.source;

/**
 * Similar to LineProcessor
 *
 * Author:  Fred Deng
 */
public interface SourceProcessor<T, R> {
    void process(T input);
    R getResult();
}
