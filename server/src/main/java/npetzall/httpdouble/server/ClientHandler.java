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
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.ScheduledExecutorService;

import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;


public class ClientHandler extends BaseClientHandler<HttpObject> {

    private static final Logger log = LoggerFactory.getLogger(ClientHandler.class);

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

    private void handleRequestHeader(ChannelHandlerContext ctx, HttpObject msg) {
        if (msg instanceof HttpRequest) {
            request = new SimpleRequest();
            response = new SimpleResponse();
            HttpRequest httpRequest = (HttpRequest)msg;
            serviceDoubleRef = serviceDoubleRegistry.getServiceDoubleByURLPath(httpRequest.uri());
            if (serviceDoubleRef == null) {
                notFound(ctx);
               return;
            }
            request.shouldKeepAlive(HttpUtil.isKeepAlive(httpRequest));
            request.path(httpRequest.uri());
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
            serviceDoubleRef.getServiceDouble().processRequest(request,response);
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
