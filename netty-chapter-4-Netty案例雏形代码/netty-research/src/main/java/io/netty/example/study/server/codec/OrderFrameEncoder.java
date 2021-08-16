package io.netty.example.study.server.codec;


import io.netty.handler.codec.LengthFieldPrepender;

/***
For example, LengthFieldPrepender(2) will encode the following 12-bytes string:
 +----------------+
 | "HELLO, WORLD" |
 +----------------+
 
into the following:
 +--------+----------------+
 + 0x000C | "HELLO, WORLD" |
 +--------+----------------+
 
If you turned on the lengthIncludesLengthFieldLength flag in the constructor, the encoded data would look like the following (12 (original data) + 2 (prepended data) = 14 (0xE)):
 +--------+----------------+
 + 0x000E | "HELLO, WORLD" |
 ***/
public class OrderFrameEncoder extends LengthFieldPrepender {
    public OrderFrameEncoder() {
        super(2);
    }
}
