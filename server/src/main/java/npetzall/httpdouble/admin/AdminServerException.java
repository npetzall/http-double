package npetzall.httpdouble.admin;

public class AdminServerException extends RuntimeException {
    public AdminServerException(String message, Exception exception) {
        super(message,exception);
    }
}
