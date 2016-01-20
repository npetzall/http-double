package npetzall.http_double.api;

import java.io.InputStream;

public interface ServiceDoubleConfiguration {
    public ServiceDoubleConfiguration name(String name);
    public ServiceDoubleConfiguration urlPath(String urlPath);
    public ServiceDoubleConfiguration addTemplate(String name, InputStream inputStream);
}
