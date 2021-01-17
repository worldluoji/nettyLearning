package io.netty.example.study.client.handler.dispatcher;

import io.netty.handler.timeout.IdleStateHandler;

public class ClientIdleHandler extends IdleStateHandler {
    public ClientIdleHandler(int readerIdleTimeSeconds, int writerIdleTimeSeconds, int allIdleTimeSeconds) {
        super(readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds);
    }

    public ClientIdleHandler() {
        super(0, 5, 0);
    }
}
