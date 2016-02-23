package npetzall.httpdouble.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import npetzall.httpdouble.api.TemplateService;
import npetzall.httpdouble.server.registry.ServiceDoubleRegistry;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class HttpDoubleServer extends BaseServer {

    private ScheduledExecutorService scheduledExecutorService;

    private TemplateService templateService;
    private ServiceDoubleRegistry serviceDoubleRegistry;

    private int usePort = 3000;
    private boolean useSSL = false;
    private int numberOfSchedulerThreads = 100;

    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    public void setServiceDoubleRegistry(ServiceDoubleRegistry serviceDoubleRegistry) {
        this.serviceDoubleRegistry = serviceDoubleRegistry;
    }

    public void setUsePort(int usePort) {
        this.usePort = usePort;
    }

    public void setUseSSL(boolean useSSL) {
        this.useSSL = useSSL;
    }

    public void setNumberOfSchedulerThreads(int numberOfSchedulerThreads) {
        this.numberOfSchedulerThreads = numberOfSchedulerThreads;
    }

    @Override
    public void start() {
        validate();
        scheduledExecutorService = Executors.newScheduledThreadPool(numberOfSchedulerThreads);
        if (isStopped()) {
            try {
                final SslContext sslCtx;
                if (useSSL) {
                    SelfSignedCertificate ssc = new SelfSignedCertificate();
                    sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
                } else {
                    sslCtx = null;
                }
                bossGroup = new NioEventLoopGroup(1);
                workerGroup = new NioEventLoopGroup();
                ServerBootstrap b = new ServerBootstrap();
                b.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ServerInitializer(sslCtx, serviceDoubleRegistry, templateService, scheduledExecutorService));

                channel = b.bind(usePort).sync().channel();

                running = true;
            } catch (Exception e) {
                throw new HttpDoubleServerException("Unable to start", e);
            }
        }
    }

    private void validate() {
        if (templateService == null || serviceDoubleRegistry == null) {
            throw new IllegalStateException((templateService == null ? "TemplateService" : "ServiceDoubleRegistry") + " isn't set");
        }
    }

    @Override
    public void stop() {
        if (running) {
            super.stop();
            scheduledExecutorService.shutdown();
            scheduledExecutorService = null;
        }
    }

}
