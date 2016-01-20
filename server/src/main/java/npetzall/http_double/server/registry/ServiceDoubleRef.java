package npetzall.http_double.server.registry;

import npetzall.http_double.api.ServiceDouble;

/**
 * Created by nosse on 2016-01-18.
 */
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
