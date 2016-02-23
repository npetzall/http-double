package npetzall.httpdouble.admin;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class AdminServer {

    private int port = 3010;

    private boolean running = false;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel channel;

    public void setPort(int port) {
        this.port = port;
    }

    public void start() {
        if (isStopped()) {
            try {
                bossGroup = new NioEventLoopGroup(1);
                workerGroup = new NioEventLoopGroup();
                ServerBootstrap b = new ServerBootstrap();
                b.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new AdminServerInitializer());

                channel = b.bind(port).sync().channel();

                running = true;
            } catch (Exception e) {
                throw new AdminServerException("Unable to start", e);
            }
        }
    }

    private boolean isStopped() {
        return !running;
    }

    public void stop() {
        if (running) {
            try {
                channel.close();
                channel.closeFuture().sync();
                channel = null;
            } catch (InterruptedException e) {
                //nothing
            } finally {
                bossGroup.shutdownGracefully();
                bossGroup = null;
                workerGroup.shutdownGracefully();
                workerGroup = null;
            }
            running = false;
        }
    }

}
