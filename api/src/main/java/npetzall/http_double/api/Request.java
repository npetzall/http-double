package npetzall.http_double.api;

import java.io.InputStream;
import java.util.Map;

public interface Request {
  public String getPath();
  public Map<String,String> getHeaders();
  public InputStream getBody();
}
