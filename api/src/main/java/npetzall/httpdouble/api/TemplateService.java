package npetzall.httpdouble.api;

import java.io.InputStream;

public interface TemplateService {
    void put(String serviceDoubleName, String templateName, InputStream inputstream);
    InputStream get(String serviceDoubleName, String templateName);
}
