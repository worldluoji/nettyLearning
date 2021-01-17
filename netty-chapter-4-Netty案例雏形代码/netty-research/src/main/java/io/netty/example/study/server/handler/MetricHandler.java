package io.netty.example.study.server.handler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.atomic.AtomicLong;

/**
 *  ChannelDuplexHandler既支持读也支持写
 *  Sharable注解表示该Handler可以被多个客户端共享使用
 * */
@ChannelHandler.Sharable
public class MetricHandler extends ChannelDuplexHandler {

    // 可将该数据发送到elk存储和展示
    private AtomicLong totalConnections = new AtomicLong();

    // 表示链接建立时执行
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        totalConnections.incrementAndGet();
        super.channelActive(ctx);
    }

    // 表示链接释放时执行
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        totalConnections.decrementAndGet();
        super.channelInactive(ctx);
    }
}
