package npetzall.httpdouble.admin.services;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import npetzall.httpdouble.server.registry.ServiceDoubleRef;
import npetzall.httpdouble.server.registry.ServiceDoubleRegistry;

import java.util.Map.Entry;

public class ListDoubleService implements AdminService {

    private ServiceDoubleRegistry registry;

    public ListDoubleService(ServiceDoubleRegistry registry) {
        this.registry = registry;
    }

    public String getName() { return "DoubleServiceList"; }
    public String getPath() { return "/servicedoubles"; }

    @Override
    public void handle(ChannelHandlerContext ctx, FullHttpRequest request) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<html><head><title>ServiceDoubles</title></head><body><br/><br/>");
        for (Entry<String, ServiceDoubleRef> entry : registry.getAllServiceDoubles().entrySet()) {
            stringBuilder.append("<a hre=\""+entry.getKey()+"\">").append(entry.getValue().getName()).append("</a><br/>");
        }
        stringBuilder.append("</body></html>");
        ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(stringBuilder.toString().getBytes())));
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }
}
