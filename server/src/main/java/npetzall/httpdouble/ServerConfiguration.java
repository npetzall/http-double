package npetzall.httpdouble;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.util.ArrayList;
import java.util.List;

public class ServerConfiguration {

    @Option(name="--port", usage="Specify port")
    int usePort = 3000;

    @Option(name="--admin-port", usage="Specifies the admin port")
    int adminPort = -1;

    @Option(name="--disable-admin", usage="Disable the admin side-kick")
    boolean disableAdmin = false;

    @Option(name="--ssl", usage="Enabled ssl")
    boolean useSSL = false;

    @Option(name="--st", usage="Specify number of threads used for handling delays")
    int numberOfSchedulerThreads = 100;

    @Argument
    List<String> arguments = new ArrayList<>();
}
