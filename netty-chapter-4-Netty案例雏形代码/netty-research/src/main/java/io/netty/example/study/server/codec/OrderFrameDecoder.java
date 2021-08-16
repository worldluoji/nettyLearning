package io.netty.example.study.server.codec;


import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/***
 example1:
 lengthFieldOffset   = 0
 lengthFieldLength   = 2
 lengthAdjustment    = 0
 initialBytesToStrip = 2 (= the length of the Length field)

 BEFORE DECODE (14 bytes)         AFTER DECODE (12 bytes)
 +--------+----------------+      +----------------+
 | Length | Actual Content |----->| Actual Content |
 | 0x000C | "HELLO, WORLD" |      | "HELLO, WORLD" |
 +--------+----------------+      +----------------+
这里报文是 “长度 + 内容的形式”，头部长度标识后面内容有多长
lengthFieldLength = 2是指示头部的长度为2个字节
initialBytesToStrip = 2 会把标识内容长度的头部2个字节去掉。

example2:
 lengthFieldOffset   =  0
 lengthFieldLength   =  2
 lengthAdjustment    = -2 (= the length of the Length field)
 initialBytesToStrip =  0

 BEFORE DECODE (14 bytes)         AFTER DECODE (14 bytes)
 +--------+----------------+      +--------+----------------+
 | Length | Actual Content |----->| Length | Actual Content |
 | 0x000E | "HELLO, WORLD" |      | 0x000E | "HELLO, WORLD" |
这里报文是 “长度 + 内容的形式”，头部长度标识总的内容有多长
  Because the length value in this example message is always greater than the body length by 2, 
  we specify -2 as lengthAdjustment for compensation

example3:
 lengthFieldOffset   = 2 (= the length of Header 1)
 lengthFieldLength   = 3
 lengthAdjustment    = 0
 initialBytesToStrip = 0

 BEFORE DECODE (17 bytes)                      AFTER DECODE (17 bytes)
 +----------+----------+----------------+      +----------+----------+----------------+
 | Header 1 |  Length  | Actual Content |----->| Header 1 |  Length  | Actual Content |
 |  0xCAFE  | 0x00000C | "HELLO, WORLD" |      |  0xCAFE  | 0x00000C | "HELLO, WORLD" |
 +----------+----------+----------------+      +----------+----------+----------------+
 入口报文还有其它header，占了2个字节, 这时通过指定lengthFieldOffset标识Length的位置
 

example4:
 lengthFieldOffset   = 1 (= the length of HDR1)
 lengthFieldLength   = 2
 lengthAdjustment    = 1 (= the length of HDR2)
 initialBytesToStrip = 3 (= the length of HDR1 + LEN)

 BEFORE DECODE (16 bytes)                       AFTER DECODE (13 bytes)
 +------+--------+------+----------------+      +------+----------------+
 | HDR1 | Length | HDR2 | Actual Content |----->| HDR2 | Actual Content |
 | 0xCA | 0x000C | 0xFE | "HELLO, WORLD" |      | 0xFE | "HELLO, WORLD" |
 +------+--------+------+----------------+      +------+----------------+

 参考文档：https://docs.jboss.org/netty/3.1/api/org/jboss/netty/handler/codec/frame/LengthFieldBasedFrameDecoder.html
 ***/
public class OrderFrameDecoder extends LengthFieldBasedFrameDecoder {
    public OrderFrameDecoder() {
        super(Integer.MAX_VALUE, 0, 2, 0, 2);
    }
}
