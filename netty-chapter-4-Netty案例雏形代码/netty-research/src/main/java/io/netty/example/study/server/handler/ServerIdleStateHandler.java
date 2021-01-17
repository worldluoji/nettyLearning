package io.netty.example.study.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class ServerIdleStateHandler extends IdleStateHandler {
    public ServerIdleStateHandler(long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit) {
        super(readerIdleTime, writerIdleTime, allIdleTime, unit);
    }

    public ServerIdleStateHandler() {
        super(10, 0, 0, TimeUnit.SECONDS);
    }

    // channel空闲时进行处理
    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        // 如果是第一次读IDLE就关闭连接
        if (evt == IdleStateEvent.FIRST_READER_IDLE_STATE_EVENT) {
            ctx.close();
            return;
        }
        super.channelIdle(ctx, evt);
    }
}
