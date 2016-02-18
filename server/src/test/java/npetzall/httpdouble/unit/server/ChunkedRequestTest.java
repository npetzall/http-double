package npetzall.httpdouble.unit.server;

import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.*;
import npetzall.httpdouble.api.TemplateService;
import npetzall.httpdouble.doubles.RecordingBodyServiceDouble;
import npetzall.httpdouble.doubles.ServiceDoubleRegistryDouble;
import npetzall.httpdouble.doubles.TemplateServiceDouble;
import npetzall.httpdouble.server.ClientHandlerFullHttpRequest;
import npetzall.httpdouble.server.ServerInitializer;
import npetzall.httpdouble.server.registry.ServiceDoubleRegistry;
import org.testng.annotations.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.assertj.core.api.Assertions.assertThat;

public class ChunkedRequestTest {

    @Test
    public void withAggregator() {
        RecordingBodyServiceDouble serviceDouble = new RecordingBodyServiceDouble();
        ServiceDoubleRegistry serviceDoubleRegistry = new ServiceDoubleRegistryDouble(serviceDouble, "withOutAggregator");
        TemplateService templateService = new TemplateServiceDouble();
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        ServerInitializer serverInitializer = new ServerInitializer(null, serviceDoubleRegistry,templateService, scheduledExecutorService);
        EmbeddedChannel ech = new EmbeddedChannel(serverInitializer);
        DefaultHttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "http://localhost/serviceDoubleDouble");
        request.headers().set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
        DefaultHttpContent chunkOne = new DefaultHttpContent(Unpooled.wrappedBuffer("chunkOne".getBytes()));
        DefaultHttpContent chunkTwo = new DefaultHttpContent(Unpooled.wrappedBuffer("chunkTwo".getBytes()));
        DefaultLastHttpContent last = new DefaultLastHttpContent(Unpooled.wrappedBuffer("last".getBytes()));
        ech.writeInbound(request, chunkOne, chunkTwo, last);
        assertThat(serviceDouble.getRequestBody()).isEqualTo("chunkOnechunkTwolast");
    }

    @Test
    public void withAggregatorClientHandlerFullHttpRequest() {
        RecordingBodyServiceDouble serviceDouble = new RecordingBodyServiceDouble();
        ServiceDoubleRegistry serviceDoubleRegistry = new ServiceDoubleRegistryDouble(serviceDouble, "withOutAggregator");
        TemplateService templateService = new TemplateServiceDouble();
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        ClientHandlerFullHttpRequest clientHandlerFullHttpRequest = new ClientHandlerFullHttpRequest(serviceDoubleRegistry,templateService, scheduledExecutorService);
        EmbeddedChannel ech = new EmbeddedChannel(new HttpObjectAggregator(1089282), clientHandlerFullHttpRequest);
        DefaultHttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "http://localhost/serviceDoubleDouble");
        request.headers().set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
        DefaultHttpContent chunkOne = new DefaultHttpContent(Unpooled.wrappedBuffer("chunkOne".getBytes()));
        DefaultHttpContent chunkTwo = new DefaultHttpContent(Unpooled.wrappedBuffer("chunkTwo".getBytes()));
        DefaultLastHttpContent last = new DefaultLastHttpContent(Unpooled.wrappedBuffer("last".getBytes()));
        ech.writeInbound(request, chunkOne, chunkTwo, last);
        assertThat(serviceDouble.getRequestBody()).isEqualTo("chunkOnechunkTwolast");
    }
}
