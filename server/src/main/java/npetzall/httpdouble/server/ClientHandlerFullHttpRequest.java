package npetzall.httpdouble.server;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.QueryStringDecoder;
import npetzall.httpdouble.api.TemplateService;
import npetzall.httpdouble.metrics.MetricsHandler;
import npetzall.httpdouble.server.registry.ServiceDoubleRegistry;
import npetzall.httpdouble.server.service.SimpleRequest;
import npetzall.httpdouble.server.service.SimpleResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;


public class ClientHandlerFullHttpRequest extends BaseClientHandler<FullHttpRequest> {

    private static final Logger log = LoggerFactory.getLogger(ClientHandlerFullHttpRequest.class);

    private static final Counter requestCounter = MetricsHandler.counter(ClientHandlerFullHttpRequest.class, "request.count");
    private static final Meter requestMeter = MetricsHandler.meter(ClientHandlerFullHttpRequest.class, "request.meter");

    public ClientHandlerFullHttpRequest(ServiceDoubleRegistry serviceDoubleRegistry,
                                        TemplateService templateService,
                                        ScheduledExecutorService scheduledExecutorService) {
        super(serviceDoubleRegistry,templateService,scheduledExecutorService);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest) throws Exception {
        try {
            requestMeter.mark();
            requestCounter.inc();
            request = new SimpleRequest();
            response = new SimpleResponse();
            request.queryStringDecoder(new QueryStringDecoder(fullHttpRequest.uri()));
            serviceDoubleRef = serviceDoubleRegistry.getServiceDoubleByURLPath(request.path());
            if (serviceDoubleRef == null) {
                notFound(ctx);
                return;
            }
            request.shouldKeepAlive(HttpUtil.isKeepAlive(fullHttpRequest));
            request.method(fullHttpRequest.method().name());
            addHeadersToRequest(fullHttpRequest.headers(), request);
            addHeadersToRequest(fullHttpRequest.trailingHeaders(), request);

            if (fullHttpRequest.content().isReadable()) {
                request.body(new ByteBufInputStream(fullHttpRequest.content()));
            }
            sendResponse(ctx);
        } catch (Exception e) {
            handleException(log, ctx, e);
        }
    }
}
