package npetzall.httpdouble.server;

public class HttpDoubleServerException extends RuntimeException {

    public HttpDoubleServerException(String message) {
        super(message);
    }

    public HttpDoubleServerException(String message, Throwable cause) {
        super(message, cause);
    }

}
