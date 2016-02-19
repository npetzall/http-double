package npetzall.httpdouble.server;

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_0;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public abstract class BaseClientHandler<T> extends SimpleChannelInboundHandler<T> {

    protected final ScheduledExecutorService scheduledExecutorService;

    protected final ServiceDoubleRegistry serviceDoubleRegistry;
    protected final TemplateService templateService;

    protected SimpleRequest request;
    protected SimpleResponse response;

    protected ServiceDoubleRef serviceDoubleRef;

    public BaseClientHandler(ServiceDoubleRegistry serviceDoubleRegistry,
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

    protected static void notFound(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(new DefaultFullHttpResponse(HTTP_1_0, NOT_FOUND))
                .addListener(ChannelFutureListener.CLOSE);
    }

    protected static void addHeadersToRequest(HttpHeaders httpHeaders, SimpleRequest request) {
        httpHeaders.forEach(entry -> request.addHeader(entry.getKey(), entry.getValue()));
    }

    protected void chunkedResponse(final ChannelHandlerContext ctx, final SimpleResponse response) {
        scheduledExecutorService.schedule((Runnable)() -> {
            HttpResponse httpResponse = new DefaultHttpResponse(HTTP_1_1, OK);
            httpResponse.headers().set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
            setKeepAlive(httpResponse, request);
            if (response.contentType() != null) {
                httpResponse.headers().add(HttpHeaderNames.CONTENT_TYPE, response.contentType());
            }
            ctx.write(httpResponse);
            HttpChunkedInput httpChunkedInput = new HttpChunkedInput(new ChunkedStream(new TokenReplaceStream(response.getTokens(), templateService.get(serviceDoubleRef.getName(), response.templateName()))));
            ctx.write(httpChunkedInput);
            shouldClose(ctx,request);
        }, response.delay(), TimeUnit.MILLISECONDS);
    }

    protected InputStream getTokenReplaceStream(SimpleResponse response) {
        return new TokenReplaceStream(response.getTokens(), templateService.get(serviceDoubleRef.getName(), response.templateName()));
    }

    protected static void setKeepAlive(HttpResponse httpResponse, SimpleRequest request) {
        if (request.shouldKeepAlive()) {
            HttpUtil.setKeepAlive(httpResponse, request.shouldKeepAlive());
        }
    }

    protected static void shouldClose(ChannelHandlerContext ctx, SimpleRequest request) {
        if (shouldClose(request)) {
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        } else {
            ctx.flush();
        }
    }

    protected static boolean shouldClose(SimpleRequest request) {
        return !request.shouldKeepAlive();
    }

    protected void fullResponse(final ChannelHandlerContext ctx, final SimpleResponse response) throws IOException {
        final byte[] responseData = readStreamToByteArray(getTokenReplaceStream(response));
        scheduledExecutorService.schedule((Runnable)() -> {
            DefaultFullHttpResponse httpResponse = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(responseData));
            httpResponse.headers().add(HttpHeaderNames.CONTENT_LENGTH, responseData.length);
            if (response.contentType() != null) {
                httpResponse.headers().add(HttpHeaderNames.CONTENT_TYPE, response.contentType());
            }
            setKeepAlive(httpResponse, request);
            ctx.write(httpResponse);
            shouldClose(ctx, request);
        }, response.delay(), TimeUnit.MILLISECONDS);
    }

    protected static byte[] readStreamToByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] readBuff = new byte[1024];
        int numberRead;
        while((numberRead = inputStream.read(readBuff)) != -1) {
            byteArrayOutputStream.write(readBuff,0,numberRead);
        }
        return byteArrayOutputStream.toByteArray();
    }

    protected static String getRemoteAddress(ChannelHandlerContext ctx) {
        if (ctx != null && ctx.channel() != null && ctx.channel().remoteAddress() != null) {
            return ctx.channel().remoteAddress().toString();
        }
        return "UNKNOWN";
    }
}
