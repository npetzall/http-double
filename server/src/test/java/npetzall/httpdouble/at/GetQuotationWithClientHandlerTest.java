package npetzall.httpdouble.at;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.EmptyByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.*;
import npetzall.httpdouble.server.ServerInitializer;
import npetzall.httpdouble.server.registry.ServiceLoaderBackedRegistry;
import npetzall.httpdouble.template.OffHeapTemplateRepo;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

public class GetQuotationWithClientHandlerTest {

    private EmbeddedChannel createEmbeddedChannel(ScheduledExecutorService scheduledExecutorService) {
        OffHeapTemplateRepo offHeapTemplateRepo = new OffHeapTemplateRepo();
        ServiceLoaderBackedRegistry serviceLoaderBackedRegistry = new ServiceLoaderBackedRegistry(offHeapTemplateRepo);
        ServerInitializer serverInitializer = new ServerInitializer(null, serviceLoaderBackedRegistry, offHeapTemplateRepo, scheduledExecutorService);
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(serverInitializer);
        embeddedChannel.pipeline().remove("responseEncoder");
        return embeddedChannel;
    }

    @Test
    public void fullRequestChunkedResponse() throws InterruptedException {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        EmbeddedChannel embeddedChannel = createEmbeddedChannel(scheduledExecutorService);

        DefaultFullHttpRequest fullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/singleValue", Unpooled.wrappedBuffer("Npetzall".getBytes()));
        fullHttpRequest.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        embeddedChannel.writeInbound(fullHttpRequest);
        scheduledExecutorService.shutdown();
        scheduledExecutorService.awaitTermination(1000, TimeUnit.MILLISECONDS);
        assertChunkedResponse(embeddedChannel);
    }

    private void assertChunkedResponse(EmbeddedChannel embeddedChannel) {
        assertThat(embeddedChannel.outboundMessages().size()).isGreaterThan(2);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for(;;) {
            Object obj = embeddedChannel.readOutbound();
            if (obj instanceof EmptyByteBuf || obj instanceof LastHttpContent) {
                break;
            } else if (obj instanceof HttpResponse) {
                assertThat(((HttpResponse)obj).headers().get(HttpHeaderNames.CONTENT_TYPE)).isEqualTo("text/xml");
                assertThat(((HttpResponse)obj).headers().get(HttpHeaderNames.TRANSFER_ENCODING)).isEqualTo(HttpHeaderValues.CHUNKED.toString());
            } else if (obj instanceof HttpContent) {
                copy(((HttpContent)obj).content(), out);
            } else {
                fail("Unknown ouput");
            }
        }
        assertThat(new ByteArrayInputStream(out.toByteArray())).hasSameContentAs(this.getClass().getResourceAsStream("/expected/getQuotationResponse.xml"));
    }

    private void copy(ByteBuf content, ByteArrayOutputStream out) {
        try {
            content.readBytes(out, content.readableBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void chunkedRequestChunkedResponse() throws InterruptedException {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        EmbeddedChannel embeddedChannel = createEmbeddedChannel(scheduledExecutorService);
        DefaultHttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/singleValue");
        request.headers().set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
        request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        DefaultHttpContent first = new DefaultHttpContent(Unpooled.wrappedBuffer("N".getBytes()));
        DefaultHttpContent second = new DefaultHttpContent(Unpooled.wrappedBuffer("petz".getBytes()));
        DefaultHttpContent third = new DefaultHttpContent(Unpooled.wrappedBuffer("all".getBytes()));
        DefaultLastHttpContent last = new DefaultLastHttpContent();
        embeddedChannel.writeInbound(request, first, second, third, last);
        scheduledExecutorService.shutdown();
        scheduledExecutorService.awaitTermination(1000, TimeUnit.MILLISECONDS);
        assertChunkedResponse(embeddedChannel);
    }

    @Test
    public void fullRequestFullResponse() throws InterruptedException {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        EmbeddedChannel embeddedChannel = createEmbeddedChannel(scheduledExecutorService);
        DefaultFullHttpRequest fullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.PUT, "/singleValue", Unpooled.wrappedBuffer("Npetzall".getBytes()));
        fullHttpRequest.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        embeddedChannel.writeInbound(fullHttpRequest);
        scheduledExecutorService.shutdown();
        scheduledExecutorService.awaitTermination(1000, TimeUnit.MILLISECONDS);
        assertFullResponse(embeddedChannel);
    }

    private void assertFullResponse(EmbeddedChannel embeddedChannel) {
        assertThat(embeddedChannel.outboundMessages().size()).isEqualTo(2);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for(;;) {
            Object obj = embeddedChannel.readOutbound();
            if (obj instanceof EmptyByteBuf) {
                break;
            } else if (obj instanceof FullHttpResponse) {
                assertThat(((FullHttpResponse)obj).headers().get(HttpHeaderNames.CONTENT_TYPE)).isEqualTo("text/xml");
                copy(((FullHttpResponse)obj).content(), out);
            } else {
                fail("Unknown ouput");
            }
        }
        assertThat(new ByteArrayInputStream(out.toByteArray())).hasSameContentAs(this.getClass().getResourceAsStream("/expected/getQuotationResponse.xml"));
    }

    @Test
    public void chunkedRequestFullResponse() throws InterruptedException {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        EmbeddedChannel embeddedChannel = createEmbeddedChannel(scheduledExecutorService);
        DefaultHttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.PUT, "/singleValue");
        request.headers().set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
        request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        DefaultHttpContent first = new DefaultHttpContent(Unpooled.wrappedBuffer("N".getBytes()));
        DefaultHttpContent second = new DefaultHttpContent(Unpooled.wrappedBuffer("petz".getBytes()));
        DefaultHttpContent third = new DefaultHttpContent(Unpooled.wrappedBuffer("all".getBytes()));
        DefaultLastHttpContent last = new DefaultLastHttpContent();
        embeddedChannel.writeInbound(request, first, second, third, last);
        scheduledExecutorService.shutdown();
        scheduledExecutorService.awaitTermination(1000, TimeUnit.MILLISECONDS);
        assertFullResponse(embeddedChannel);
    }
}
