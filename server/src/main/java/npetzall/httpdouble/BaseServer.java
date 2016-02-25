package npetzall.httpdouble;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;

public abstract class BaseServer {

    protected volatile boolean running = false;

    protected EventLoopGroup bossGroup;
    protected EventLoopGroup workerGroup;
    protected Channel channel;

    public abstract void start();

    protected boolean isStopped() {
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
