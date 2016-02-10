package npetzall.http_double.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class TokenReplaceStream extends InputStream {

    private static final String PRE_FIX = "${";
    private static final String POST_FIX = "}";

    private static final Charset charset = StandardCharsets.UTF_8;

    private final byte[] preFixBytes = PRE_FIX.getBytes(charset);
    private final byte postFixByte = POST_FIX.getBytes(charset)[0];

    private final byte[] tokenBuffer;

    private boolean reachedEndOfStream;

    private CircularIntBuffer outBuffer = new CircularIntBuffer(80);

    private Map<String, String> tokenMap;
    private InputStream inputStream;

    public TokenReplaceStream(Map<String,String> tokens, InputStream inputStream) {
        this.tokenMap = tokens;
        this.inputStream = inputStream;
        int longestToken = tokenMap.keySet().stream().mapToInt(String::length).max().getAsInt();
        tokenBuffer = new byte[longestToken+1];
    }

    @Override
    public int available() throws IOException {
        return Math.max(inputStream.available(), outBuffer.available());
    }

    @Override
    public int read() throws IOException {
        if (reachedEndOfStream) {
            return -1;
        }
        return doRead();
    }

    private int doRead() throws IOException {
        if (outBuffer.available() > 0) {
            return outBuffer.read();
        }
        int read = readSource();
        if (read == -1) {
            reachedEndOfStream = true;
        }
        return read;
    }

    private int readSource() throws IOException {
        int read0 = inputStream.read();
        if (read0 == -1) {
            return -1;
        }
        if (read0 == preFixBytes[0]) {
            checkPrefix();
        } else {
            outBuffer.write(read0);
        }
        return outBuffer.read();
    }

    private void checkPrefix() throws IOException {
        int read1 = inputStream.read();
        if (read1 == preFixBytes[1]) {
            tryToken();
        } else {
            outBuffer.write(preFixBytes[0]);
            outBuffer.write(read1);
        }
    }

    private void tryToken() throws IOException {
        int index = fillBuffer();
        int lastRead = tokenBuffer[index];
        if (lastRead == -1) {
            writePreFix();
            for(int i = 0; i <= index; i++) {
                outBuffer.write(tokenBuffer[i]);
            }
        } else if (lastRead == postFixByte) {
            checkForTokenReplacement(index);
        } else {
            writeTokenbuffer(index);
        }
    }

    private int fillBuffer() throws IOException {
        int read;
        int index = 0;
        for(; index < tokenBuffer.length; index++) {
            read = inputStream.read();
            tokenBuffer[index] = (byte)read;
            if (read == -1 || read == postFixByte) {
                index++;
                break;
            }
        }
        return index-1;
    }

    private void writePreFix() {
        for(byte b: preFixBytes) {
            outBuffer.write(b);
        }
    }

    private void checkForTokenReplacement(int index) {
        String value = tokenMap.get(new String(tokenBuffer,0,index, charset));
        if (value != null) {
            writeReplacement(value);
        } else {
            writeTokenbuffer(index);
        }
    }

    private void writeReplacement(String replacement) {
        for(byte b: replacement.getBytes(charset)) {
            outBuffer.write(b);
        }
    }

    private void writeTokenbuffer(int index) {
        writePreFix();
        for(int i = 0; i <= index; i++) {
            outBuffer.write(tokenBuffer[i]);
        }
    }

    public static class CircularIntBuffer {
        private int readPointer = 0;
        private int writerPointer = 0;
        private AtomicInteger available = new AtomicInteger(0);
        private volatile boolean closed;

        private final int bufferSize;
        private final int[] buffer;
        private final boolean[] allocated;

        public CircularIntBuffer(int bufferSize) {
            this.bufferSize = bufferSize;
            buffer = new int[bufferSize];
            allocated = new boolean[bufferSize];
        }

        public boolean write(int i) {
            if (closed) {
                throw new RuntimeException("Can't write to closed buffer");
            }
            if(allocated[writerPointer]) {
                return false;
            }

            buffer[writerPointer] = i;
            allocated[writerPointer] = true;
            available.incrementAndGet();
            writerPointer = (writerPointer + 1) % bufferSize;
            return true;
        }

        public int read() {
            return doRead();
        }

        private int doRead() {
            if(closed) {
                return -1;
            } else {
                int value = buffer[readPointer];
                allocated[readPointer] = false;
                available.decrementAndGet();
                readPointer = (readPointer + 1) % bufferSize;
                return value;
            }
        }

        public int available() {
            return available.get();
        }

        public void close() {
            closed = true;
        }
    }
}
