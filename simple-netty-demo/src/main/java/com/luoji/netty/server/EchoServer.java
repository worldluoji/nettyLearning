package com.luoji.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * 在 Netty 中，bossGroup 就用来处理连接请求的，而 workerGroup 是用来处理读写请求的。
 * bossGroup 处理完连接请求后，会将这个连接提交给 workerGroup 来处理。这实际就是Reactor模式，一个线程专门处理请求转发，真正做事却是其它线程
 * workerGroup 里面有多个 EventLoop，那新的连接会交给哪个 EventLoop 来处理呢？这就需要一个负载均衡算法，Netty 中目前使用的是轮询算法。
 *
*  默认情况下，Netty 会创建“2*CPU 核数”个 EventLoop，
 * 由于网络连接与 EventLoop 有稳定的关系，所以事件处理器在处理网络事件的时候是不能有阻塞操作的，否则很容易导致请求大面积超时。
 * 如果实在无法避免使用阻塞操作，那可以通过线程池来异步处理。
 *
 * 对于TCP来说，处理 TCP 连接请求和读写请求是通过两个不同的 socket 完成的。即listen的socket和处理请求的socket不是一个
* */
public final class EchoServer {
    private final static int SERVER_PORT = 8690;
    public static void main(String[] args) throws InterruptedException {
        final EchoServerHandler handler = new EchoServerHandler();
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline p = socketChannel.pipeline();
                            p.addLast(new LoggingHandler(LogLevel.INFO));
                            p.addLast(handler);
                        }
                    });
            ChannelFuture f = bootstrap.bind(SERVER_PORT).sync();
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
}
