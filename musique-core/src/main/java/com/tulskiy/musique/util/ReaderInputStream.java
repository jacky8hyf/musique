package com.tulskiy.musique.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public class ReaderInputStream extends InputStream {
    private Reader reader;
    ReaderInputStream(Reader reader) {
        this.reader = reader;
    }
    @Override
    public int read() throws IOException {
        return reader.read();
    }
    @Override
    public int read(byte b[], int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        char[] cbuf = new char[len];
        int ret = reader.read(cbuf, 0, len);
        if(ret > 0) {
            System.arraycopy(cbuf, 0, b, off, ret); // only copy read number of bytes
        }
        return ret;
    }
    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }
    @Override
    public long skip(long n) throws IOException {
        return reader.skip(n);
    }
    @Override
    public int available() throws IOException {
        return 0; // cannot know from reader
    }
    @Override
    public void close() throws IOException {
        reader.close();
    }
    @Override
    public synchronized void mark(int readlimit) {
        try {
            reader.mark(readlimit);
        } catch (IOException ignored) { }
    }
    @Override
    public synchronized void reset() throws IOException {
        reader.reset();
    }
    @Override
    public boolean markSupported() {
        return reader.markSupported();
    }

}
