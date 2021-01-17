package io.netty.example.study.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.example.study.server.codec.OrderFrameDecoder;
import io.netty.example.study.server.codec.OrderFrameEncoder;
import io.netty.example.study.server.codec.OrderProtocolDecoder;
import io.netty.example.study.server.codec.OrderProtocolEncoder;
import io.netty.example.study.server.handler.OrderServerProcessHandler;
import io.netty.handler.flush.FlushConsolidationHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import io.netty.handler.traffic.GlobalChannelTrafficShapingHandler;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.UnorderedThreadPoolEventExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;

@Slf4j
public class Server {

    public static void main(String[] args) throws InterruptedException, ExecutionException {

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.channel(NioServerSocketChannel.class);

        serverBootstrap.handler(new LoggingHandler(LogLevel.INFO));

        // 关闭negal算法，即小包可立即发出，不必等待
        serverBootstrap.childOption(NioChannelOption.TCP_NODELAY, true);
        // 设置等待队列中的连接数（当连接不够用时，后来的链接会放到队列中等待）
        serverBootstrap.option(NioChannelOption.SO_BACKLOG, 1024);

        // 创建一个独立的线程池专门给orderHandler业务使用
        UnorderedThreadPoolEventExecutor executor = new UnorderedThreadPoolEventExecutor(3,  new DefaultThreadFactory("orderHandleThreadPoll"));

        //Linux下，可以使用EpollEventLoopGroup()来代替NioEventLoopGroup，这样就会使用native epoll
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("boss"));
        NioEventLoopGroup group = new NioEventLoopGroup(2, new DefaultThreadFactory("worker"));

        // 全局读写限流控制都设置为100MB, 当实际业务机器、资源已经足够，就没有必要设置它了. 如果只对channel限流就用ChannelTrafficShapingHandler
        GlobalTrafficShapingHandler tsHandler = new GlobalTrafficShapingHandler(new NioEventLoopGroup(), 100 * 1024 * 1024, 100 * 1024 * 1024);

        try{
            serverBootstrap.group(bossGroup, group);

            serverBootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast("tsHandler", tsHandler);

                    pipeline.addLast("frameDecoder", new OrderFrameDecoder());
                    pipeline.addLast("frameEncoder", new OrderFrameEncoder());

                    pipeline.addLast("protocolEncoder", new OrderProtocolEncoder());
                    pipeline.addLast("protocolDecoder", new OrderProtocolDecoder());

                    pipeline.addLast("logger", new LoggingHandler(LogLevel.INFO));

                    // flush增强，flush 3次才真正进行flush操作，减少写的次数，增大吞吐量，但同样带来了延迟，实时性很高的不建议打开
                    pipeline.addLast("flushSolid", new FlushConsolidationHandler(3, true));
                    pipeline.addLast(executor, new OrderServerProcessHandler());
                }
            });

            ChannelFuture channelFuture = serverBootstrap.bind(8090).sync();
            // 阻塞，直到监听到关闭事件
            channelFuture.channel().closeFuture().sync();
        } finally {
            log.info("Server down...");
            group.shutdownGracefully();
        }


    }

}
