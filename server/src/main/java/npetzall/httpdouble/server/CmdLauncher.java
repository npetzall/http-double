package npetzall.httpdouble.server;

import npetzall.httpdouble.api.TemplateService;
import npetzall.httpdouble.server.registry.ServiceDoubleRegistry;
import npetzall.httpdouble.server.registry.ServiceLoaderBackedRegistry;
import npetzall.httpdouble.template.OffHeapTemplateRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CmdLauncher {

    private static final Logger log = LoggerFactory.getLogger(CmdLauncher.class);

    private CmdLauncher() {}

    public static void main(String[] args) throws IOException {
        HttpDoubleServer httpDoubleServer = new HttpDoubleServer();
        TemplateService templateService = new OffHeapTemplateRepo();
        ServiceDoubleRegistry serviceDoubleRegistry = new ServiceLoaderBackedRegistry(templateService);
        httpDoubleServer.setTemplateService(templateService);
        httpDoubleServer.setServiceDoubleRegistry(serviceDoubleRegistry);
        httpDoubleServer.start();
        log.info("Server listening on port 3000, with no SSL\nPress enter/return to shutdown");
        System.in.read();
        httpDoubleServer.stop();

    }
}
