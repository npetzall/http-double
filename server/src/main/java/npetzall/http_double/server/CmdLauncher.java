package npetzall.http_double.server;

import npetzall.http_double.api.TemplateService;
import npetzall.http_double.server.registry.ServiceDoubleRegistry;
import npetzall.http_double.server.registry.ServiceLoaderBackedRegistry;
import npetzall.http_double.template.OffHeapTemplateRepo;

import java.io.IOException;

public class CmdLauncher {

    public static void main(String[] args) throws IOException {
        HttpDoubleServer httpDoubleServer = new HttpDoubleServer();
        TemplateService templateService = new OffHeapTemplateRepo();
        ServiceDoubleRegistry serviceDoubleRegistry = new ServiceLoaderBackedRegistry(templateService);
        httpDoubleServer.setTemplateService(templateService);
        httpDoubleServer.setServiceDoubleRegistry(serviceDoubleRegistry);
        httpDoubleServer.start();
        System.err.println("Server listening on port 3000, with no SSL\nPress enter/return to shutdown");
        System.in.read();
        httpDoubleServer.stop();

    }
}
