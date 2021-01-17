package io.netty.example.study.client.handler.dispatcher;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.example.study.common.RequestMessage;
import io.netty.example.study.common.keepalive.KeepaliveOperation;
import io.netty.example.study.util.IdUtil;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public class KeepaliveHandler extends ChannelDuplexHandler {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt == IdleStateEvent.FIRST_WRITER_IDLE_STATE_EVENT) {
            log.info("client write idle happend, need to send keepalive msg to server...");
            KeepaliveOperation operation = new KeepaliveOperation();
            RequestMessage keepaliveMsg = new RequestMessage(IdUtil.nextId(), operation);
            ctx.writeAndFlush(keepaliveMsg);
        }
        super.userEventTriggered(ctx, evt);
    }
}
