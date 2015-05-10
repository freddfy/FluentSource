package guava.ext.source

import com.google.common.base.Function
import com.google.common.base.Predicate
import com.google.common.io.CharSource
import org.junit.Before
import org.junit.Test
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

import static org.junit.Assert.fail
import static org.mockito.Mockito.*

/**
 *
 * Author:  Fred Deng
 */
class FluentSourceAutoCloseableTest {

    List<BufferedReader> allOpenedReaders

    @Before
    public void setUp() throws Exception {
        allOpenedReaders = []
    }

    @Test
    public void "should close the underlying stream whenever readAll"() throws Exception {
        subjectOf('1\n2\n3').readAll()

        verifyAllOpenedReadersClosed()

    }

    @Test
    public void "should close then underlying stream when processing all"() throws Exception {
        subjectOf('1\n2\n3').readAll(mock(SourceProcessor))

        verifyAllOpenedReadersClosed()
    }

    @Test
    public void "should close the underlying stream when chaining fluent methods"() throws Exception {
        subjectOf('1\n2\n3')
                .transform({ it.toInteger() } as Function)
                .filter({ it % 2 == 0 } as Predicate)
                .readAll()

        verifyAllOpenedReadersClosed()
    }

    @Test
    public void "should close the underlying stream even if exception thrown"() throws Exception {
        try {
            subjectOf('1\nNotANumber\n3')
                    .transform({ it.toInteger() } as Function)
                    .filter({ it % 2 == 0 } as Predicate)
                    .readAll()

            fail('Should have thrown NumberFormatException')
        } catch (NumberFormatException ignored) {
            verifyAllOpenedReadersClosed()
        }

    }

    @Test
    public void "should close all underlying stream when concat FluentSource"() throws Exception {
        FluentSource.concat(subjectOf('1\n2\n'), subjectOf('3\n4')).readAll()

        verifyAllOpenedReadersClosed()
    }

    @Test
    public void "should do lazy open readers when iterate thru concat FluentSource"() throws Exception {
        def one = subjectOf('1\n2')
        def two = subjectOf('3')

        def iter = FluentSource.concat(one, two).openIterator()
        assert '1' == iter.next()
        assert '2' == iter.next()

        assert allOpenedReaders.size() == 1

        assert '3' == iter.next()

        assert allOpenedReaders.size() == 2
    }

    private FluentSource<String> subjectOf(String content) {
        FluentSource.byLines(spyOpenStream(CharSource.wrap(content)))
    }

    private verifyAllOpenedReadersClosed() {
        allOpenedReaders.each {
            verify(it).close()
        }
    }

    CharSource spyOpenStream(CharSource source) {
        def spiedSource = spy(source)
        doAnswer(new Answer() {
            @Override
            Object answer(InvocationOnMock invocation) throws Throwable {
                allOpenedReaders << spy((BufferedReader) invocation.callRealMethod())
                allOpenedReaders.last()
            }
        }).when(spiedSource).openBufferedStream()
        spiedSource
    }
}
