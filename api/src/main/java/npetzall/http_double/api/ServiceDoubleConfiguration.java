package npetzall.http_double.api;

import java.io.InputStream;

public interface ServiceDoubleConfiguration {
    ServiceDoubleConfiguration name(String name);
    ServiceDoubleConfiguration urlPath(String urlPath);
    ServiceDoubleConfiguration addTemplate(String name, InputStream inputStream);
}
