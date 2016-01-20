package npetzall.http_double.api;

import java.io.InputStream;
import java.util.Map;

public interface Request {
  public String path();
  public Map<String,String> headers();
  public InputStream body();
}
