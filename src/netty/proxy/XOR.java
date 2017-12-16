package netty.proxy;

import io.netty.channel.ChannelHandlerContext;

import java.util.List;

public class XOR {

    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, List list) throws Exception {

//        System.out.println("enc: o: " + o);
//        System.out.println("list: " + list);
    }

    protected void decode(ChannelHandlerContext channelHandlerContext, Object o, List list) throws Exception {
//        System.out.println("dec: o: " + o);
//        ByteBuf buf = (ByteBuf)o;
//
//        for (int i = 0; i < 3; i++) {
//            byte b = (byte)buf.getUnsignedByte(i);
//            System.out.printf("%c ", b);
//        }
//        System.out.println();
//        buf.copy()
//        ByteBuf buf2 = buf.getBytes();
////        list.add(buf);
//        System.out.println("list: " + list);
    }
}
