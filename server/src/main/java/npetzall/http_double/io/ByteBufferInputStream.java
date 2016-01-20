package npetzall.http_double.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ByteBufferInputStream extends InputStream {

    private final ByteBuffer byteBuffer;

    public ByteBufferInputStream(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer.duplicate();
        this.byteBuffer.rewind();
        if (byteBuffer == null) {
            throw new IllegalArgumentException("ByteBuffer can't be null");
        }
    }

    public int read() throws IOException {
        if (!byteBuffer.hasRemaining()) {
            return -1;
        }
        return byteBuffer.get() & 0xFF;
    }

    public int read(byte[] bytes, int off, int len)
            throws IOException {
        if (!byteBuffer.hasRemaining()) {
            return -1;
        }

        len = Math.min(len, byteBuffer.remaining());
        byteBuffer.get(bytes, off, len);
        return len;
    }

    @Override
    public int available() throws IOException {
        return byteBuffer.remaining();
    }
}
