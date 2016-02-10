package npetzall.http_double.server.registry;

@FunctionalInterface
public interface ServiceDoubleRegistry {
    ServiceDoubleRef getServiceDoubleByURLPath(String urlPath);
}
