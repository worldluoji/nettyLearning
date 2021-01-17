package io.netty.example.study.server.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.example.study.common.Operation;
import io.netty.example.study.common.RequestMessage;
import io.netty.example.study.common.auth.AuthOperation;
import io.netty.example.study.common.auth.AuthOperationResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public class AuthHandler extends SimpleChannelInboundHandler<RequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RequestMessage requestMessage) throws Exception {
        try {
            Operation operation = requestMessage.getMessageBody();
            if (operation instanceof AuthOperation) {
                AuthOperation authOperation = AuthOperation.class.cast(operation);
                AuthOperationResult result = authOperation.execute();
                if (result.isPassAuth()) {
                    log.info("pass auth...");
                } else {
                    log.error("not pass auth...");
                    channelHandlerContext.close();
                }
            } else {
                log.error("not an AuthOperation");
            }
        } catch (Exception e) {
            log.error("not pass auth2...", e);
            channelHandlerContext.close();
        } finally {
            // 授权如果通过，就可以把该handler移除pipeline,下一次就不用再授权了。
            channelHandlerContext.pipeline().remove(this);
        }
    }
}
