package npetzall.httpdouble.api;

import java.io.InputStream;
import java.util.Map;

public interface Request {
    String path();
    String method();
    Map<String, String> headers();
    InputStream body();
}
