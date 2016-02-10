package npetzall.httpdouble.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedWriteHandler;
import npetzall.httpdouble.api.TemplateService;
import npetzall.httpdouble.server.registry.ServiceDoubleRegistry;

import java.util.concurrent.ScheduledExecutorService;

public class ServerInitializer extends ChannelInitializer<SocketChannel> {

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
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline channelPipeline = ch.pipeline();

        if (sslContext != null) {
            channelPipeline.addLast(sslContext.newHandler(ch.alloc()));
        }
        channelPipeline.addLast(new HttpContentDecompressor());
        channelPipeline.addLast(new HttpRequestDecoder());
        channelPipeline.addLast(new HttpResponseEncoder());
        channelPipeline.addLast(new ChunkedWriteHandler());
        //channelPipeline.addLast(new HttpContentCompressor());
        channelPipeline.addLast(new ClientHandler(serviceDoubleRegistry, templateService, scheduledExecutorService));

    }
}
