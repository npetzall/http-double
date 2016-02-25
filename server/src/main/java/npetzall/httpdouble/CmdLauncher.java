package npetzall.httpdouble;

import npetzall.httpdouble.admin.AdminServer;
import npetzall.httpdouble.admin.registry.AdminRegistry;
import npetzall.httpdouble.admin.services.ListDoubleService;
import npetzall.httpdouble.api.TemplateService;
import npetzall.httpdouble.server.HttpDoubleServer;
import npetzall.httpdouble.server.registry.ServiceDoubleRegistry;
import npetzall.httpdouble.server.registry.ServiceLoaderBackedRegistry;
import npetzall.httpdouble.template.OffHeapTemplateRepo;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CmdLauncher {

    private static final Logger log = LoggerFactory.getLogger(CmdLauncher.class);

    private CmdLauncher() {}

    public static void main(String[] args) throws IOException {
        ServerConfiguration serverConfiguration = new ServerConfiguration();
        TemplateService templateService = new OffHeapTemplateRepo();
        ServiceDoubleRegistry serviceDoubleRegistry = new ServiceLoaderBackedRegistry(templateService);

        CmdLineParser parser = new CmdLineParser(serverConfiguration);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException cmdEx) {
            log.error("Parse arguments failed",cmdEx);
            parser.printUsage(System.out);
            System.exit(666);
        }
        HttpDoubleServer httpDoubleServer = new HttpDoubleServer();
        httpDoubleServer.setUsePort(serverConfiguration.usePort);
        httpDoubleServer.setUseSSL(serverConfiguration.useSSL);
        httpDoubleServer.setNumberOfSchedulerThreads(serverConfiguration.numberOfSchedulerThreads);
        httpDoubleServer.setTemplateService(templateService);
        httpDoubleServer.setServiceDoubleRegistry(serviceDoubleRegistry);
        httpDoubleServer.start();
        if(!serverConfiguration.disableAdmin) {
            ListDoubleService listDoubleService = new ListDoubleService(serviceDoubleRegistry);
            AdminRegistry.getInstance().register(listDoubleService.getName(), listDoubleService.getPath(), listDoubleService);
        }
        log.info("Using port: "+ serverConfiguration.usePort);
        log.info("Using ssl: " + serverConfiguration.useSSL);
        log.info("Number of threads for delay: " + serverConfiguration.numberOfSchedulerThreads);
        AdminServer adminServer = new AdminServer();
        log.info("Admin is disable: " + serverConfiguration.disableAdmin);
        if(!serverConfiguration.disableAdmin) {
            int adminPort = serverConfiguration.adminPort < 0 ? serverConfiguration.usePort + 10 : serverConfiguration.adminPort;
            adminServer.setPort(adminPort);
            adminServer.start();
            log.info("Admin is using port: " + adminPort);
        }
        log.info("Press Enter to stop");
        System.in.read();
        httpDoubleServer.stop();
        adminServer.stop();

    }
}
