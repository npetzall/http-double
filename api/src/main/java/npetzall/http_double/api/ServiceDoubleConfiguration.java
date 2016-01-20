package npetzall.http_double.api;

import java.io.InputStream;

public interface ServiceDoubleConfiguration {
    public ServiceDoubleConfiguration setName(String name);
    public ServiceDoubleConfiguration setUrlPath(String urlPath);
    public ServiceDoubleConfiguration addTemplate(String name, InputStream inputStream);
}
