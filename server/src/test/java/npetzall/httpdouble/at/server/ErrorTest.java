package npetzall.httpdouble.at.server;

import io.netty.buffer.EmptyByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.*;
import npetzall.httpdouble.server.ClientHandlerFullHttpRequest;
import npetzall.httpdouble.server.ServerInitializer;
import npetzall.httpdouble.server.registry.ServiceLoaderBackedRegistry;
import npetzall.httpdouble.template.OffHeapTemplateRepo;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Fail.fail;
import static org.assertj.core.api.StrictAssertions.assertThat;

public class ErrorTest {

    private EmbeddedChannel createEmbeddedChannel(ScheduledExecutorService scheduledExecutorService) {
        OffHeapTemplateRepo offHeapTemplateRepo = new OffHeapTemplateRepo();
        ServiceLoaderBackedRegistry serviceLoaderBackedRegistry = new ServiceLoaderBackedRegistry(offHeapTemplateRepo);
        ServerInitializer serverInitializer = new ServerInitializer(null, serviceLoaderBackedRegistry, offHeapTemplateRepo, scheduledExecutorService);
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(serverInitializer);
        embeddedChannel.pipeline().remove("responseEncoder");
        return embeddedChannel;
    }

    private EmbeddedChannel createEmbeddedChannelWithClientFullRequest(ScheduledExecutorService scheduledExecutorService) {
        OffHeapTemplateRepo offHeapTemplateRepo = new OffHeapTemplateRepo();
        ServiceLoaderBackedRegistry serviceLoaderBackedRegistry = new ServiceLoaderBackedRegistry(offHeapTemplateRepo);
        ServerInitializer serverInitializer = new ServerInitializer(null, serviceLoaderBackedRegistry, offHeapTemplateRepo, scheduledExecutorService);
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(serverInitializer);
        embeddedChannel.pipeline().remove("responseEncoder");
        embeddedChannel.pipeline().replace("clientHandler", "clientHandlerFullRequest", new ClientHandlerFullHttpRequest(serviceLoaderBackedRegistry, offHeapTemplateRepo, scheduledExecutorService));
        return embeddedChannel;
    }

    @Test
    public void channelFailUsingClientHandler() throws InterruptedException {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        EmbeddedChannel embeddedChannel = createEmbeddedChannel(scheduledExecutorService);
        DefaultFullHttpRequest fullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.PUT, "/error", Unpooled.wrappedBuffer("Npetzall".getBytes()));
        fullHttpRequest.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        embeddedChannel.writeInbound(fullHttpRequest);
        scheduledExecutorService.shutdown();
        scheduledExecutorService.awaitTermination(1000, TimeUnit.MILLISECONDS);
        assertThat(embeddedChannel.outboundMessages().size()).isEqualTo(1);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for(;;) {
            Object obj = embeddedChannel.readOutbound();
            if (obj instanceof EmptyByteBuf || obj == null) {
                break;
            } else if (obj instanceof FullHttpResponse) {
                assertThat(((FullHttpResponse)obj).status().code()).isEqualTo(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
            } else {
                fail("Unknown ouput");
            }
        }
    }

    @Test
    public void notFoundUsingClientHandler() throws InterruptedException {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        EmbeddedChannel embeddedChannel = createEmbeddedChannel(scheduledExecutorService);
        DefaultFullHttpRequest fullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.PUT, "/notfound", Unpooled.wrappedBuffer("Npetzall".getBytes()));
        fullHttpRequest.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        embeddedChannel.writeInbound(fullHttpRequest);
        scheduledExecutorService.shutdown();
        scheduledExecutorService.awaitTermination(1000, TimeUnit.MILLISECONDS);
        assertThat(embeddedChannel.outboundMessages().size()).isEqualTo(1);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for(;;) {
            Object obj = embeddedChannel.readOutbound();
            if (obj instanceof EmptyByteBuf || obj == null) {
                break;
            } else if (obj instanceof FullHttpResponse) {
                assertThat(((FullHttpResponse)obj).status().code()).isEqualTo(HttpResponseStatus.NOT_FOUND.code());
            } else {
                fail("Unknown ouput");
            }
        }
    }

    @Test
    public void channelFailUsingClientHandlerFullRequest() throws InterruptedException {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        EmbeddedChannel embeddedChannel = createEmbeddedChannelWithClientFullRequest(scheduledExecutorService);
        DefaultFullHttpRequest fullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.PUT, "/error", Unpooled.wrappedBuffer("Npetzall".getBytes()));
        fullHttpRequest.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        embeddedChannel.writeInbound(fullHttpRequest);
        scheduledExecutorService.shutdown();
        scheduledExecutorService.awaitTermination(1000, TimeUnit.MILLISECONDS);
        assertThat(embeddedChannel.outboundMessages().size()).isEqualTo(1);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for(;;) {
            Object obj = embeddedChannel.readOutbound();
            if (obj instanceof EmptyByteBuf || obj == null) {
                break;
            } else if (obj instanceof FullHttpResponse) {
                assertThat(((FullHttpResponse)obj).status().code()).isEqualTo(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
            } else {
                fail("Unknown ouput");
            }
        }
    }

    @Test
    public void notFoundUsingClientHandlerFullRequest() throws InterruptedException {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        EmbeddedChannel embeddedChannel = createEmbeddedChannelWithClientFullRequest(scheduledExecutorService);
        DefaultFullHttpRequest fullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.PUT, "/notfound", Unpooled.wrappedBuffer("Npetzall".getBytes()));
        fullHttpRequest.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        embeddedChannel.writeInbound(fullHttpRequest);
        scheduledExecutorService.shutdown();
        scheduledExecutorService.awaitTermination(1000, TimeUnit.MILLISECONDS);
        assertThat(embeddedChannel.outboundMessages().size()).isEqualTo(1);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for(;;) {
            Object obj = embeddedChannel.readOutbound();
            if (obj instanceof EmptyByteBuf || obj == null) {
                break;
            } else if (obj instanceof FullHttpResponse) {
                assertThat(((FullHttpResponse)obj).status().code()).isEqualTo(HttpResponseStatus.NOT_FOUND.code());
            } else {
                fail("Unknown ouput");
            }
        }
    }
}
