package guava.ext.source;

import com.google.common.base.Throwables;
import com.google.common.collect.UnmodifiableIterator;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Author:  Fred Deng
 */
class CloseableIteratorBufferedReader extends UnmodifiableIterator<String> implements CloseableIterator<String> {
    private BufferedReader br;
    private String nextLine;

    public CloseableIteratorBufferedReader(BufferedReader br) {
        this.br = br;
        advance();
    }

    @Override
    public boolean hasNext() {
        return nextLine != null;
    }

    @Override
    public String next() {
        String line = nextLine;
        advance();
        return line;
    }

    private void advance() {
        try {
            nextLine = br.readLine();
        } catch (IOException e) {
            Throwables.propagate(e);
        }
    }

    @Override
    public void close() {
        try {
            br.close();
        } catch (IOException e) {
            Throwables.propagate(e);
        }
    }
}
