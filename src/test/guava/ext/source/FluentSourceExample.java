package guava.ext.source;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.io.CharSource;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Author:  Fred Deng
 */
public class FluentSourceExample {
    @Test
    public void simple() throws Exception {

        int sumOfEven = FluentSource.on(CharSource.wrap("1,2,3,4,5"), ",")
                .transform(toInteger())
                .filter(isEven())
                .readAll(sum());

        assertEquals(6, sumOfEven);
    }

    private Function<String, Integer> toInteger() {
        return new Function<String, Integer>() {
            @Override
            public Integer apply(String input) {
                return Integer.valueOf(input);
            }
        };
    }

    private SourceProcessor<Integer, Integer> sum() {
        return new SourceProcessor<Integer, Integer>() {
            private int sum;

            @Override
            public void process(Integer input) {
                this.sum += input;
            }

            @Override
            public Integer getResult() {
                return sum;
            }
        };
    }

    private Predicate<Integer> isEven() {
        return new Predicate<Integer>() {
            @Override
            public boolean apply(Integer input) {
                return input % 2 == 0;
            }
        };
    }

    @Test
    public void csvSource() throws Exception {
        List<Map<String, String>> result = FluentSource.onCsv(CharSource.wrap("a,b,c\n1,2,3\n2,3,4"))
                .filter(aIsEven())
                .readAll();

        assertEquals(ImmutableMap.of("a", "2", "b", "3", "c", "4"), Iterables.getOnlyElement(result));

    }

    private Predicate<Map<String, String>> aIsEven() {
        return new Predicate<Map<String, String>>() {
            @Override
            public boolean apply(Map<String, String> input) {
                return Integer.valueOf(input.get("a")) % 2 == 0;
            }
        };
    }
}
