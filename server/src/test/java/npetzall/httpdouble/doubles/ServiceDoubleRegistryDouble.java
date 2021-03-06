package npetzall.httpdouble.doubles;

import npetzall.httpdouble.api.ServiceDouble;
import npetzall.httpdouble.server.registry.ServiceDoubleRef;
import npetzall.httpdouble.server.registry.ServiceDoubleRegistry;

import java.util.Collections;
import java.util.Map;

public class ServiceDoubleRegistryDouble implements ServiceDoubleRegistry {

    private ServiceDoubleRef serviceDoubleRef;

    public ServiceDoubleRegistryDouble(ServiceDouble serviceDouble, String name) {
        serviceDoubleRef = new ServiceDoubleRef(name,serviceDouble);
    }

    @Override
    public ServiceDoubleRef getServiceDoubleByURLPath(String urlPath) {
        return serviceDoubleRef;
    }

    @Override
    public Map<String, ServiceDoubleRef> getAllServiceDoubles() {
        return Collections.singletonMap("*",serviceDoubleRef);
    }
}
