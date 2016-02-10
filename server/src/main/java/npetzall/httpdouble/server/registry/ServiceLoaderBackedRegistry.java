package npetzall.httpdouble.server.registry;

import npetzall.httpdouble.api.ServiceDouble;
import npetzall.httpdouble.api.TemplateService;
import npetzall.httpdouble.server.service.DefaultServiceDoubleConfiguration;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceLoaderBackedRegistry implements ServiceDoubleRegistry {

    private static ServiceLoader<ServiceDouble> serviceLoader = ServiceLoader.load(ServiceDouble.class);

    private Map<String, ServiceDoubleRef> urlServiceDoubleMap = new ConcurrentHashMap<>();

    private final TemplateService templateService;

    public ServiceLoaderBackedRegistry(TemplateService templateService) {
        this.templateService = templateService;
        serviceLoader.iterator().forEachRemaining((serviceDouble -> {
            DefaultServiceDoubleConfiguration serviceDoubleConfiguration = new DefaultServiceDoubleConfiguration();
            serviceDouble.configure(serviceDoubleConfiguration);
            urlServiceDoubleMap.put(serviceDoubleConfiguration.getUrlPath(),new ServiceDoubleRef(serviceDoubleConfiguration.getServiceDoubleName(), serviceDouble));
            addTemplatesToTemplateService(serviceDoubleConfiguration);
        }));
    }

    private void addTemplatesToTemplateService(DefaultServiceDoubleConfiguration serviceDoubleConfiguration) {
        String serviceDoubleName =  serviceDoubleConfiguration.getServiceDoubleName();
        serviceDoubleConfiguration.getTemplates().forEach((k,v) -> {
            templateService.put(serviceDoubleName, k, v);
        });
    }

    @Override
    public ServiceDoubleRef getServiceDoubleByURLPath(String urlPath) {
        return urlServiceDoubleMap.get(urlPath);
    }
}
