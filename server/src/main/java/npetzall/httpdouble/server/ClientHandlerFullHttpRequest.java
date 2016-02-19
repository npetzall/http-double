package npetzall.httpdouble.server;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import npetzall.httpdouble.api.TemplateService;
import npetzall.httpdouble.server.registry.ServiceDoubleRegistry;
import npetzall.httpdouble.server.service.SimpleRequest;
import npetzall.httpdouble.server.service.SimpleResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.ScheduledExecutorService;

import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;


public class ClientHandlerFullHttpRequest extends BaseClientHandler<FullHttpRequest> {

    private static final Logger log = LoggerFactory.getLogger(ClientHandlerFullHttpRequest.class);

    public ClientHandlerFullHttpRequest(ServiceDoubleRegistry serviceDoubleRegistry,
                                        TemplateService templateService,
                                        ScheduledExecutorService scheduledExecutorService) {
        super(serviceDoubleRegistry,templateService,scheduledExecutorService);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest) throws Exception {
        try {
            request = new SimpleRequest();
            response = new SimpleResponse();
            serviceDoubleRef = serviceDoubleRegistry.getServiceDoubleByURLPath(fullHttpRequest.uri());
            if (serviceDoubleRef == null) {
                notFound(ctx);
                return;
            }
            request.shouldKeepAlive(HttpUtil.isKeepAlive(fullHttpRequest));
            request.path(fullHttpRequest.uri());
            request.method(fullHttpRequest.method().name());
            addHeadersToRequest(fullHttpRequest.headers(), request);
            addHeadersToRequest(fullHttpRequest.trailingHeaders(), request);

            if (fullHttpRequest.content().isReadable()) {
                request.body(new ByteBufInputStream(fullHttpRequest.content()));
            }
            serviceDoubleRef.getServiceDouble().processRequest(request, response);
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
        } catch (Exception e) {
            log.error("Faild on request from: " + getRemoteAddress(ctx), e);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(byteArrayOutputStream));
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                    HTTP_1_1,
                    INTERNAL_SERVER_ERROR,
                    Unpooled.wrappedBuffer(byteArrayOutputStream.toByteArray()));
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
            ctx.writeAndFlush(response)
                    .addListener(ChannelFutureListener.CLOSE);
        }
    }
}
