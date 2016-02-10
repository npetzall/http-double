package npetzall.httpdouble.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ByteBufferInputStream extends InputStream {

    private final ByteBuffer byteBuffer;

    public ByteBufferInputStream(ByteBuffer byteBuffer) {
        if (byteBuffer == null) {
            throw new IllegalArgumentException("ByteBuffer can't be null");
        }
        this.byteBuffer = byteBuffer.duplicate();
        this.byteBuffer.rewind();
    }

    @Override
    public int read() throws IOException {
        if (!byteBuffer.hasRemaining()) {
            return -1;
        }
        return byteBuffer.get() & 0xFF;
    }

    @Override
    public int read(byte[] bytes, int off, int len)
            throws IOException {
        if (!byteBuffer.hasRemaining()) {
            return -1;
        }

        int length = Math.min(len, byteBuffer.remaining());
        byteBuffer.get(bytes, off, length);
        return length;
    }

    @Override
    public int available() throws IOException {
        return byteBuffer.remaining();
    }

}
