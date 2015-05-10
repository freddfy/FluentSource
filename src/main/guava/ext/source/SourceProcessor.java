package guava.ext.source;

/**
 * Author:  Fred Deng
 */
public interface SourceProcessor<T, R> {
    void process(T input);
    R getResult();
}
