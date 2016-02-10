package npetzall.httpdouble.server.service;

import npetzall.httpdouble.api.ServiceDoubleConfiguration;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class DefaultServiceDoubleConfiguration implements ServiceDoubleConfiguration {

    private String serviceDoubleName;
    private String urlPath;
    private Map<String, InputStream> templates = new HashMap<>();

    @Override
    public ServiceDoubleConfiguration name(String name) {
        this.serviceDoubleName = name;
        return this;
    }

    @Override
    public ServiceDoubleConfiguration urlPath(String urlPath) {
        this.urlPath = urlPath;
        return this;
    }

    @Override
    public ServiceDoubleConfiguration addTemplate(String name, InputStream inputStream) {
        templates.put(name, inputStream);
        return this;
    }

    public String getServiceDoubleName() {
        return serviceDoubleName;
    }

    public String getUrlPath() {
        return urlPath;
    }

    public Map<String,InputStream> getTemplates() {
        return templates;
    }
}
