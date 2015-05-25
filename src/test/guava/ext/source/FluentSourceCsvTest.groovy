package guava.ext.source

import com.google.common.io.CharSource
import org.junit.Test

/**
 *
 * Author:  Fred Deng
 */
class FluentSourceCsvTest {
    @Test
    public void "csv source can tranform into map source"() throws Exception {
        assert FluentSource.onCsv(CharSource.wrap('a,b,c\n1,2,3\n')).readAll() == [['a':'1','b':'2','c':'3']]
    }

    @Test(expected=IllegalArgumentException)
    public void "csv source will fail fast if header and values line size not matched"() throws Exception {
        FluentSource.onCsv(CharSource.wrap('a,b,c\n1,2')).readAll()
    }

    @Test(expected=IllegalArgumentException)
    public void "csv source will fail fast if header has duplicate keys"() throws Exception {
        FluentSource.onCsv(CharSource.wrap('a,a,b,c\n1,2,3,4')).readAll()
    }

    @Test
    public void "csv source works fine even if there is empty string in the value lines"() throws Exception {
        assert FluentSource.onCsv(CharSource.wrap('a,b,c,d\n1,,3,')).readAll() == [['a':'1','b':'','c':'3','d':'']]
    }

    @Test
    public void "single empty column works"() throws Exception {
        FluentSource.onCsv(CharSource.wrap('a\n1\n\n3')).readAll() == [['a':'1'], ['a':''],['a':'3']]
    }

    @Test
    public void "empty header single column still works"() throws Exception {
        FluentSource.onCsv(CharSource.wrap('\n\n\n1')).readAll() == [['':''], ['':''],['':'1']]
    }
}
