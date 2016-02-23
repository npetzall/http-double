package npetzall.httpdouble.admin.registry;

import npetzall.httpdouble.admin.services.AdminService;
import npetzall.httpdouble.admin.services.IndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

public class AdminRegistry {

    private static final Logger log = LoggerFactory.getLogger(AdminRegistry.class);

    private static ServiceLoader<AdminService> serviceLoaderAdminService = ServiceLoader.load(AdminService.class);
    private static final AdminRegistry adminRegistry = new AdminRegistry();

    private final ConcurrentHashMap<String, AdminServiceRef> registry = new ConcurrentHashMap<>();
    private final AdminServiceRef index = new AdminServiceRef("Index", new IndexService());

    private AdminRegistry() {
        registry.put("/", index);
        log.info("Loaded: Index");
        serviceLoaderAdminService.iterator().forEachRemaining(adminService -> {
            register(adminService.getName(), adminService.getPath(), adminService);
        });
    }

    public static AdminRegistry getInstance() {
        return adminRegistry;
    }

    public void register(String name, String uri, AdminService adminService) {
        registry.put(uri, new AdminServiceRef(name,adminService));
        log.info("Loaded: " + name);
    }

    public AdminServiceRef getAdminServiceRef(String uri) {
        return registry.getOrDefault(uri,index);
    }

    public Map<String, AdminServiceRef> getAllAdminServices() {
        return Collections.unmodifiableMap(registry);
    }

}
