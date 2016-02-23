package npetzall.httpdouble.admin;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import npetzall.httpdouble.admin.registry.AdminRegistry;
import npetzall.httpdouble.admin.services.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class AdminClientHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger log = LoggerFactory.getLogger(AdminClientHandler.class);

    private AdminRegistry registry = AdminRegistry.getInstance();

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(msg.uri());
        AdminService adminService = registry.getAdminServiceRef(queryStringDecoder.path()).getAdminService();
        try {
            adminService.handle(ctx, msg);
        } catch (Exception e) {
            log.error("Error in " + adminService.getName(), e);
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
