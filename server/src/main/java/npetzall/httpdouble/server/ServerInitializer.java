package npetzall.httpdouble.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedWriteHandler;
import npetzall.httpdouble.api.TemplateService;
import npetzall.httpdouble.server.registry.ServiceDoubleRegistry;

import java.util.concurrent.ScheduledExecutorService;

public class ServerInitializer extends ChannelInitializer<Channel> {

    private final SslContext sslContext;
    private final ServiceDoubleRegistry serviceDoubleRegistry;
    private final TemplateService templateService;
    private final ScheduledExecutorService scheduledExecutorService;

    public ServerInitializer(SslContext sslContext,
                             ServiceDoubleRegistry serviceDoubleRegistry,
                             TemplateService templateService,
                             ScheduledExecutorService scheduledExecutorService) {
        this.sslContext = sslContext;
        this.serviceDoubleRegistry = serviceDoubleRegistry;
        this.templateService = templateService;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline channelPipeline = ch.pipeline();

        if (sslContext != null) {
            channelPipeline.addLast(sslContext.newHandler(ch.alloc()));
        }
        channelPipeline.addLast(new HttpContentDecompressor());
        channelPipeline.addLast(new HttpRequestDecoder());
        channelPipeline.addLast(new HttpObjectAggregator(1048576));
        channelPipeline.addLast("responseEncoder", new HttpResponseEncoder());
        channelPipeline.addLast(new ChunkedWriteHandler());
        //channelPipeline.addLast(new HttpContentCompressor());
        channelPipeline.addLast("clientHandler", new ClientHandler(serviceDoubleRegistry, templateService, scheduledExecutorService));

    }
}
