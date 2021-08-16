package io.netty.example.study.common;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public abstract class OperationResult extends MessageBody{

}
