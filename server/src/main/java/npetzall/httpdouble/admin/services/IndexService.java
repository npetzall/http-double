package npetzall.httpdouble.admin.services;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import npetzall.httpdouble.admin.registry.AdminRegistry;
import npetzall.httpdouble.admin.registry.AdminServiceRef;

import java.util.Map;

public class IndexService implements AdminService {

    public String getName() {
        return "Index";
    }

    public String getPath() {
        return "/";
    }

    @Override
    public void handle(ChannelHandlerContext ctx, FullHttpRequest request) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<html><head><title>ServiceDoubles</title></head><body><br/><br/>");
        for (Map.Entry<String, AdminServiceRef> entry : AdminRegistry.getInstance().getAllAdminServices().entrySet()) {
            stringBuilder.append("<a href=\""+entry.getKey()+"\">").append(entry.getValue().getName()).append("</a><br/>");
        }
        stringBuilder.append("</body></html>");
        ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(stringBuilder.toString().getBytes())));
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }
}
