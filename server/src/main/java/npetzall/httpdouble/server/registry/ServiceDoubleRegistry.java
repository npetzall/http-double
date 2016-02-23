package npetzall.httpdouble.server.registry;

import java.util.Map;

public interface ServiceDoubleRegistry {
    ServiceDoubleRef getServiceDoubleByURLPath(String urlPath);
    Map<String, ServiceDoubleRef> getAllServiceDoubles();
}
