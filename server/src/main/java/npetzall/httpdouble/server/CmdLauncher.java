package npetzall.httpdouble.server;

import npetzall.httpdouble.api.TemplateService;
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
        CmdLineParser parser = new CmdLineParser(serverConfiguration);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException cmdEx) {
            log.error(cmdEx.getMessage());
            parser.printUsage(System.out);
            System.exit(666);
        }
        HttpDoubleServer httpDoubleServer = new HttpDoubleServer();
        httpDoubleServer.setUsePort(serverConfiguration.usePort);
        httpDoubleServer.setUseSSL(serverConfiguration.useSSL);
        httpDoubleServer.setNumberOfSchedulerThreads(serverConfiguration.numberOfSchedulerThreads);
        TemplateService templateService = new OffHeapTemplateRepo();
        ServiceDoubleRegistry serviceDoubleRegistry = new ServiceLoaderBackedRegistry(templateService);
        httpDoubleServer.setTemplateService(templateService);
        httpDoubleServer.setServiceDoubleRegistry(serviceDoubleRegistry);
        httpDoubleServer.start();
        log.info("Using port: "+ serverConfiguration.usePort);
        log.info("Using ssl: " + serverConfiguration.useSSL);
        log.info("Number of threads for delay: " + serverConfiguration.numberOfSchedulerThreads);
        log.info("Press Enter to stop");
        System.in.read();
        httpDoubleServer.stop();

    }
}
