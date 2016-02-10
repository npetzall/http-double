package npetzall.httpdouble.server.registry;

@FunctionalInterface
public interface ServiceDoubleRegistry {
    ServiceDoubleRef getServiceDoubleByURLPath(String urlPath);
}
