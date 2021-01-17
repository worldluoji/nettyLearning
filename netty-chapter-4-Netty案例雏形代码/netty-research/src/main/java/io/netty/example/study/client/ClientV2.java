package io.netty.example.study.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.example.study.client.codec.*;
import io.netty.example.study.client.handler.dispatcher.*;
import io.netty.example.study.common.OperationResult;
import io.netty.example.study.common.RequestMessage;
import io.netty.example.study.common.order.OrderOperation;
import io.netty.example.study.util.IdUtil;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;

@Slf4j
public class ClientV2 {

    public static void main(String[] args) throws InterruptedException, ExecutionException {

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        // 设置客户端连接超时时间为10秒
        bootstrap.option(NioChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);

        NioEventLoopGroup group = new NioEventLoopGroup();

        KeepaliveHandler keepaliveHandler = new KeepaliveHandler();

        try{
            bootstrap.group(group);
            RequestPendingCenter requestPendingCenter = new RequestPendingCenter();

            bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast("clientIdleChecker", new ClientIdleHandler());

                    pipeline.addLast(new OrderFrameDecoder());
                    pipeline.addLast(new OrderFrameEncoder());

                    pipeline.addLast(new OrderProtocolEncoder());
                    pipeline.addLast(new OrderProtocolDecoder());

                    pipeline.addLast(new ResponseDispatcherHandler(requestPendingCenter));

                    pipeline.addLast(new OperationToRequestMessageEncoder());

                    // 因为keepalive的发生是在编码之后的，所以要放到后面一点。
                    // 有了keepalive后，idle检测不会直接断开连接，因为服务器设置了10秒的读idle检测，而这里5秒发送一个keepalive
                    pipeline.addLast("keepaliveHandler", keepaliveHandler);

                    pipeline.addLast(new LoggingHandler(LogLevel.INFO));
                }
            });

            // 连接服务器，但是因为connect方法时异步的，所以下面需要sync()阻塞
            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 8090);

            channelFuture.sync();

            long streamId = IdUtil.nextId();

            RequestMessage requestMessage = new RequestMessage(
                    streamId, new OrderOperation(1001, "tudou"));

            OperationResultFuture operationResultFuture = new OperationResultFuture();

            requestPendingCenter.add(streamId, operationResultFuture);

            log.info("[ClientV2]send msg to server");
            channelFuture.channel().writeAndFlush(requestMessage);

            // 阻塞，直到上面ResponseDispatcherHandler返回结果，设置返回的东东
            OperationResult operationResult = operationResultFuture.get();

            log.info("[ClientV2]receive msg from server");

            log.info(operationResult.toString());

            // 客户端同步断开链接时，这句才会往下执行。让线程进入wait状态，也就是main线程不会执行到finally里面，
            // client持续运行, 直到监听到关闭事件
            channelFuture.channel().closeFuture().sync();
        } finally {
            log.info("client v2 down...");
            group.shutdownGracefully();
        }
    }

}
