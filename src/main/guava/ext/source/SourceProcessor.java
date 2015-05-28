package guava.ext.source;

/**
 * Similar to LineProcessor
 *
 * Author:  Fred Deng
 */
public interface SourceProcessor<T, R> {
    /**
     *
     * @return true if continue processing needed, false if no more processing needed
     */
    boolean process(T input);

    R getResult();
}
