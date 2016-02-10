package npetzall.httpdouble.server.registry;

import npetzall.httpdouble.api.ServiceDouble;

public class ServiceDoubleRef {
    private final String serviceDoubleName;
    private final ServiceDouble serviceDouble;

    public ServiceDoubleRef(String serviceDoubleName, ServiceDouble serviceDouble) {
        this.serviceDoubleName = serviceDoubleName;
        this.serviceDouble = serviceDouble;
    }

    public String getName() {
        return serviceDoubleName;
    }

    public ServiceDouble getServiceDouble() {
        return serviceDouble;
    }
}
