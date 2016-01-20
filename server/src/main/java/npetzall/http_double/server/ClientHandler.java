package npetzall.http_double.server;

import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedStream;
import npetzall.http_double.api.TemplateService;
import npetzall.http_double.io.TokenReplaceStream;
import npetzall.http_double.server.registry.ServiceDoubleRef;
import npetzall.http_double.server.registry.ServiceDoubleRegistry;
import npetzall.http_double.server.service.SimpleRequest;
import npetzall.http_double.server.service.SimpleResponse;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_0;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;


public class ClientHandler extends SimpleChannelInboundHandler<HttpObject> {

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(100);

    private final ServiceDoubleRegistry serviceDoubleRegistry;
    private final TemplateService templateService;

    private SimpleRequest request;
    private SimpleResponse response = new SimpleResponse();

    private ServiceDoubleRef serviceDoubleRef;

    public ClientHandler(ServiceDoubleRegistry serviceDoubleRegistry, TemplateService templateService) {
        this.serviceDoubleRegistry = serviceDoubleRegistry;
        this.templateService = templateService;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if (msg instanceof HttpRequest) {
            request = new SimpleRequest();
            HttpRequest httpRequest = (HttpRequest)msg;
            serviceDoubleRef = serviceDoubleRegistry.getServiceDoubleByURLPath(httpRequest.uri());
            if (serviceDoubleRef == null) {
                ctx.writeAndFlush(new DefaultFullHttpResponse(HTTP_1_0, NOT_FOUND))
                .addListener(ChannelFutureListener.CLOSE);
            }
            request.path(httpRequest.uri());
            httpRequest.headers().forEach((entry) -> {
                request.addHeader(entry.getKey(), entry.getValue());
            });
            if (HttpUtil.is100ContinueExpected(httpRequest)) {
                ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
            }
        }
        if (msg instanceof HttpContent) {
            HttpContent httpContent = (HttpContent) msg;
            request.body(new ByteBufInputStream(httpContent.content()));
            if (msg instanceof LastHttpContent) {
                serviceDoubleRef.getServiceDouble().processRequest(request,response);
                write(ctx, response);
            }
        }
    }

    private void write(ChannelHandlerContext ctx, SimpleResponse response) {
        HttpResponse httpResponse = new DefaultHttpResponse(HTTP_1_1, OK);
        httpResponse.headers().set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
        ctx.write(httpResponse);
        HttpChunkedInput httpChunkedInput = new HttpChunkedInput(new ChunkedStream(new TokenReplaceStream(response.getTokens(), templateService.get(serviceDoubleRef.getName(), response.templateName()))));
        ctx.write(httpChunkedInput);
    }
}
