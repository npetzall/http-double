package npetzall.httpdouble.server;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import npetzall.httpdouble.api.TemplateService;
import npetzall.httpdouble.metrics.MetricsHandler;
import npetzall.httpdouble.server.registry.ServiceDoubleRegistry;
import npetzall.httpdouble.server.service.SimpleRequest;
import npetzall.httpdouble.server.service.SimpleResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;

import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;


public class ClientHandler extends BaseClientHandler<HttpObject> {

    private static final Logger log = LoggerFactory.getLogger(ClientHandler.class);

    private static final Counter requestCounter = MetricsHandler.counter(ClientHandler.class, "request.count");
    private static final Meter requestMeter = MetricsHandler.meter(ClientHandler.class, "request.meter");

    public ClientHandler(ServiceDoubleRegistry serviceDoubleRegistry,
                         TemplateService templateService,
                         ScheduledExecutorService scheduledExecutorService) {
        super(serviceDoubleRegistry, templateService, scheduledExecutorService);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        try {
            handleRequestHeader(ctx, msg);
            handleRequestContent(ctx, msg);
        } catch (Exception e) {
            handleException(log, ctx, e);
        }
    }

    private void handleRequestHeader(ChannelHandlerContext ctx, HttpObject msg) {
        if (msg instanceof HttpRequest) {
            requestMeter.mark();
            requestCounter.inc();
            request = new SimpleRequest();
            response = new SimpleResponse();
            HttpRequest httpRequest = (HttpRequest)msg;
            request.queryStringDecoder(new QueryStringDecoder(httpRequest.uri()));
            serviceDoubleRef = serviceDoubleRegistry.getServiceDoubleByURLPath(request.path());
            if (serviceDoubleRef == null) {
                notFound(ctx);
               return;
            }
            request.shouldKeepAlive(HttpUtil.isKeepAlive(httpRequest));
            request.method(httpRequest.method().name());
            addHeadersToRequest(httpRequest.headers(), request);
            if (HttpUtil.is100ContinueExpected(httpRequest)) {
                ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
            }
        }
    }

    private void handleRequestContent(ChannelHandlerContext ctx, HttpObject msg) throws IOException {
        if (serviceDoubleRef == null) {
            return;
        }
        if (msg instanceof HttpContent) {
            HttpContent httpContent = (HttpContent) msg;
            if(httpContent.content().isReadable()) {
                request.body(new ByteBufInputStream(httpContent.content()));
            }
            handleLastContent(ctx, msg);
        }
    }

    private void handleLastContent(ChannelHandlerContext ctx, HttpObject msg) throws IOException {
        if (serviceDoubleRef == null) {
            return;
        }
        if (msg instanceof LastHttpContent) {
            addHeadersToRequest(((LastHttpContent)msg).trailingHeaders(), request);
            final Timer.Context timer = MetricsHandler.timer(serviceDoubleRef.getServiceDouble().getClass(), "processTimer").time();
            try {
                serviceDoubleRef.getServiceDouble().processRequest(request, response);
            } finally {
                timer.stop();
            }
            if (response.templateName() == null || response.templateName().isEmpty()) {
                notFound(ctx);
                return;
            } else {
                if (response.sendChunkedResponse()) {
                    chunkedResponse(ctx, response);
                } else {
                    fullResponse(ctx, response);
                }
            }
        }
    }
}
