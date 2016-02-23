package npetzall.httpdouble.admin.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

public interface AdminService {

    String getName();
    String getPath();

    void handle(ChannelHandlerContext ctx, FullHttpRequest request) throws JsonProcessingException;

}
