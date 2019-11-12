package xia.v.lan.websocket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.CharsetUtil;

/**
 * @author chenhao
 * @description <p>
 * created by chenhao 2019/7/4 14:20
 */
public class TextWebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private final ChannelGroup group;

    public TextWebSocketFrameHandler(ChannelGroup group) {
        this.group = group;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        ByteBuf buf = Unpooled.copiedBuffer("hello,netty!"+msg.text(), CharsetUtil.UTF_8);
        group.writeAndFlush(msg.replace(buf));    //6
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE) {

            ctx.pipeline().remove(HttpRequestHandler.class);    //3

            group.writeAndFlush(new TextWebSocketFrame("Client " + ctx.channel() + " joined"));//4

            group.add(ctx.channel());    //5
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
