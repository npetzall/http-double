package npetzall.httpdouble.admin;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import npetzall.httpdouble.server.BaseServer;

public class AdminServer extends BaseServer {

    private int port = 3010;

    public void setPort(int port) {
        this.port = port;
    }

    @Override
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
}
