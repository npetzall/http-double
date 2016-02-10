package npetzall.httpdouble.io;

public class CircularBufferException extends RuntimeException {

    public CircularBufferException(String message) {
        super(message);
    }

    public CircularBufferException(String message, Throwable cause) {
        super(message, cause);
    }

}
