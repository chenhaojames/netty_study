package xia.v.lan.udp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author chenhao
 * @description <p>
 * created by chenhao 2019/7/4 9:37
 */
public class LogEventEncoder extends MessageToMessageEncoder<LogEvent> {

    private final InetSocketAddress remoteAddress;

    public LogEventEncoder(InetSocketAddress remoteAddress) {  //1
        this.remoteAddress = remoteAddress;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, LogEvent msg, List<Object> out) throws Exception {
        byte[] file = msg.getLogfile().getBytes("UTF-8");
        byte[] message = msg.getMsg().getBytes("UTF-8");
        ByteBuf buf = ctx.alloc().buffer(file.length+message.length+1);
        buf.writeBytes(file);
        buf.writeByte(LogEvent.SEPARATOR);
        buf.writeBytes(message);
        out.add(new DatagramPacket(buf,remoteAddress));
    }
}
