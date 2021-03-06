package guava.ext.source

import com.google.common.base.Charsets
import com.google.common.base.Function
import com.google.common.base.Predicate
import com.google.common.io.CharSource
import org.junit.After
import org.junit.Test

import java.nio.file.Files

import static com.google.common.io.Files.asCharSource

/**
 *
 * Author:  Fred Deng
 */
class FluentSourceTest {

    private file

    @After
    public void tearDown() throws Exception {
        file?.delete()
    }

    @Test
    public void "can convert from CharSource to FluentSource"() throws Exception {
        assert ['1','2','3'] == FluentSource.onLines(srcOf('1\n2\n3\n')).readAll()

        assert ['1','2','3'] == FluentSource.onLines(srcOf('1\n2\n3')).readAll()
    }

    static Function<String, Integer> toInteger() {
        new Function<String,Integer>(){
            @Override
            Integer apply(String input) {
                input.toInteger()
            }
        }
    }

    @Test(expected=NumberFormatException)
    public void "will fail fast if any exception happens when converting from CharSource to FuentSource"() throws Exception {
        FluentSource.onLines(srcOf('1\nb\n3\n')).transform(toInteger()).readAll()
    }

    @Test
    public void "can process all sources and return result"() throws Exception {
        assert 6 == FluentSource.onLines(srcOf('1\n2\n3\n')).transform(toInteger()).readAll(new SourceProcessor<Integer, Integer>() {
            int sum = 0;
            @Override
            boolean process(Integer input) {
                sum += input
            }

            @Override
            Integer getResult() {
                sum
            }
        })
    }

    @Test
    public void "will stop processing when source processor returns false"() throws Exception {
        assert 3 == FluentSource.onLines(srcOf('1\n2\n3\n')).transform(toInteger()).readAll(new SourceProcessor<Integer, Integer>() {
            int sumCappedBy3 = 0;
            @Override
            boolean process(Integer input) {
                (sumCappedBy3 += input) < 3
            }

            @Override
            Integer getResult() {
                sumCappedBy3
            }
        })
    }


    private static CharSource srcOf(String lines) {
        CharSource.wrap(lines)
    }

    @Test
    public void "can transform to another FluentSource"() throws Exception {
        assert [1,2,3] == FluentSource.onLines(srcOf('1\n2\n3')).transform(toInteger()).readAll()
    }

    @Test
    public void "can filter to another FluentSource"() throws Exception {
        assert [2] == FluentSource.onLines(srcOf('1\n2\n3')).transform(toInteger()).filter({it%2 == 0} as Predicate).readAll()
    }

    @Test
    public void "can concat FluentSources"() throws Exception {
        file = Files.createTempFile('source', 'txt').toFile()
        FluentSource<String> source1 = FluentSource.onLines(asCharSource(file, Charsets.UTF_8))
        FluentSource<String> source2 = FluentSource.onLines(srcOf('3'))

        updateFile('1\n')
        assert ['1','3'] == FluentSource.concat(source1, source2).readAll()

        updateFile('1\n2')
        assert ['1','2','3'] == FluentSource.concat(source1, source2).readAll()
    }

    @Test
    public void "will detect the underlying source change"() throws Exception {
        file = Files.createTempFile('source', 'txt').toFile()
        def subject = FluentSource.onLines(asCharSource(file, Charsets.UTF_8)).transform(toInteger())

        updateFile('1\n2\n')
        assert [1,2] == subject.readAll()

        updateFile('2\n3\n')
        assert [2,3] == subject.readAll()
    }

    private void updateFile(String content) {
        file.withWriter {
            it.write(content)
        }
    }

    @Test
    public void "will support regex delimeters"() throws Exception {
        assert ['a', ' b', ' c'] == FluentSource.on(srcOf('a, b, c'), ',').readAll()
        assert ['a', '', 'b', '', 'c'] == FluentSource.on(srcOf('a, b, c'), ~/[,\s]/).readAll()
        assert [' ',' '] == FluentSource.on(srcOf('a, b, c'), ~/\S+/).readAll()
    }

}
