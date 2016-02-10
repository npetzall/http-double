package npetzall.httpdouble.server;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedStream;
import npetzall.httpdouble.api.TemplateService;
import npetzall.httpdouble.io.TokenReplaceStream;
import npetzall.httpdouble.server.registry.ServiceDoubleRef;
import npetzall.httpdouble.server.registry.ServiceDoubleRegistry;
import npetzall.httpdouble.server.service.SimpleRequest;
import npetzall.httpdouble.server.service.SimpleResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_0;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;


public class ClientHandler extends SimpleChannelInboundHandler<HttpObject> {

    private final ScheduledExecutorService scheduledExecutorService;

    private final ServiceDoubleRegistry serviceDoubleRegistry;
    private final TemplateService templateService;

    private SimpleRequest request;
    private SimpleResponse response;

    private ServiceDoubleRef serviceDoubleRef;

    public ClientHandler(ServiceDoubleRegistry serviceDoubleRegistry,
                         TemplateService templateService,
                         ScheduledExecutorService scheduledExecutorService) {
        this.serviceDoubleRegistry = serviceDoubleRegistry;
        this.templateService = templateService;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if (msg instanceof HttpRequest) {
            request = new SimpleRequest();
            response = new SimpleResponse();
            HttpRequest httpRequest = (HttpRequest)msg;
            serviceDoubleRef = serviceDoubleRegistry.getServiceDoubleByURLPath(httpRequest.uri());
            if (serviceDoubleRef == null) {
                ctx.writeAndFlush(new DefaultFullHttpResponse(HTTP_1_0, NOT_FOUND))
                .addListener(ChannelFutureListener.CLOSE);
            }
            request.shouldKeepAlive(HttpUtil.isKeepAlive(httpRequest));
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
                if (response.sendChunkedResponse()) {
                    chunkedResponse(ctx, response);
                } else {
                    fullResponse(ctx, response);
                }
            }
        }
    }

    private void chunkedResponse(final ChannelHandlerContext ctx, final SimpleResponse response) {
        scheduledExecutorService.schedule((Runnable)() -> {
            HttpResponse httpResponse = new DefaultHttpResponse(HTTP_1_1, OK);
            httpResponse.headers().set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
            setKeepAlive(httpResponse);
            if (response.contentType() != null) {
                httpResponse.headers().add(HttpHeaderNames.CONTENT_TYPE, response.contentType());
            }
            ctx.write(httpResponse);
            HttpChunkedInput httpChunkedInput = new HttpChunkedInput(new ChunkedStream(new TokenReplaceStream(response.getTokens(), templateService.get(serviceDoubleRef.getName(), response.templateName()))));
            ctx.write(httpChunkedInput);
            shouldClose(ctx);
        }, response.delay(), TimeUnit.MILLISECONDS);
    }

    private InputStream getTokenReplaceStream(SimpleResponse response) {
        return new TokenReplaceStream(response.getTokens(), templateService.get(serviceDoubleRef.getName(), response.templateName()));
    }

    private void setKeepAlive(HttpResponse httpResponse) {
        if (request.shouldKeepAlive()) {
            HttpUtil.setKeepAlive(httpResponse, request.shouldKeepAlive());
        }
    }

    private void shouldClose(ChannelHandlerContext ctx) {
        if (shouldClose()) {
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        } else {
            ctx.flush();
        }
    }

    private boolean shouldClose() {
        return !request.shouldKeepAlive();
    }

    private void fullResponse(final ChannelHandlerContext ctx, final SimpleResponse response) throws IOException {
        final long contentLength = lengthOfStream(getTokenReplaceStream(response));
        scheduledExecutorService.schedule((Runnable)() -> {
            DefaultHttpResponse httpResponse = new DefaultHttpResponse(HTTP_1_1, OK);
            httpResponse.headers().add(HttpHeaderNames.CONTENT_LENGTH, contentLength);
            if (response.contentType() != null) {
                httpResponse.headers().add(HttpHeaderNames.CONTENT_TYPE, response.contentType());
            }
            setKeepAlive(httpResponse);
            ctx.write(httpResponse);
            ctx.write(new ChunkedStream(getTokenReplaceStream(response)));
            ctx.write(new DefaultLastHttpContent());
            shouldClose(ctx);
        }, response.delay(), TimeUnit.MILLISECONDS);
    }

    private long lengthOfStream(InputStream inputStream) throws IOException {
        byte[] readBuff = new byte[1024];
        long size = 0;
        int numberRead;
        while((numberRead = inputStream.read(readBuff)) != -1) {
            size += numberRead;
        }
        return size;
    }
}
