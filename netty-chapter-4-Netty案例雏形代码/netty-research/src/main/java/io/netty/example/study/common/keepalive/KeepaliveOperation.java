package io.netty.example.study.common.keepalive;


import io.netty.example.study.common.Operation;
import lombok.Data;

@Data
public class KeepaliveOperation extends Operation {

    private long time ;

    public KeepaliveOperation() {
        this.time = System.nanoTime();
    }

    @Override
    public KeepaliveOperationResult execute() {
        KeepaliveOperationResult orderResponse = new KeepaliveOperationResult(time);
        return orderResponse;
    }
}
