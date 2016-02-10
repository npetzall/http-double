package npetzall.httpdouble.io;

import java.util.concurrent.atomic.AtomicInteger;

public class CircularIntBuffer {
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
            throw new CircularBufferException("Can't write to closed buffer");
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
