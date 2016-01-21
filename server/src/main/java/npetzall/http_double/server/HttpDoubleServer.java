package npetzall.http_double.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import npetzall.http_double.api.TemplateService;
import npetzall.http_double.server.registry.ServiceDoubleRegistry;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class HttpDoubleServer {

    private ScheduledExecutorService scheduledExecutorService;

    private TemplateService templateService;
    private ServiceDoubleRegistry serviceDoubleRegistry;

    private int usePort = 3000;
    private boolean useSSL = false;
    private int numberOfSchedulerThreads = 100;

    private boolean running = false;

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

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel channel;

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
                throw new RuntimeException("Unable to start", e);
            }
        }
    }

    private void validate() {
        if (templateService == null || serviceDoubleRegistry == null) {
            throw new IllegalStateException((templateService == null ? "TemplateService" : "ServiceDoubleRegistry") + " isn't set");
        }
    }

    private boolean isStopped() {
        return !running;
    }

    public void stop() {
        if (running) {
            try {
                channel.close();
                channel.closeFuture().sync();
                channel = null;
            } catch (InterruptedException e) {
                //nothing
            } finally {
                bossGroup.shutdownGracefully();
                bossGroup = null;
                workerGroup.shutdownGracefully();
                workerGroup = null;
            }
            scheduledExecutorService.shutdown();
            scheduledExecutorService = null;
            System.gc();
            System.runFinalization();
            running = false;
        }
    }

}
