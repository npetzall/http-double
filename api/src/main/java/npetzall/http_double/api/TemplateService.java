package npetzall.http_double.api;

import java.io.InputStream;

public interface TemplateService {
    public void put(String serviceDoubleName, String templateName, InputStream inputstream);
    public InputStream get(String serviceDoubleName, String templateName);
}
