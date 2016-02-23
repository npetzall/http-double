package npetzall.httpdouble.admin;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

public class AdminServerInitializer extends ChannelInitializer<Channel> {

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline channelPipeline = ch.pipeline();
        channelPipeline.addLast(new HttpContentDecompressor());
        channelPipeline.addLast(new HttpRequestDecoder());
        channelPipeline.addLast(new HttpObjectAggregator(1048576));
        channelPipeline.addLast("responseEncoder", new HttpResponseEncoder());
        channelPipeline.addLast(new ChunkedWriteHandler());
        //channelPipeline.addLast(new HttpContentCompressor());
        channelPipeline.addLast("clientHandler", new AdminClientHandler());
    }
}
