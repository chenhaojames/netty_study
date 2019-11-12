package xia.v.lan.tcp.l1;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author chenhao
 * @description <p>
 * created by chenhao 2019/11/12 14:55
 */
public class NettyClient {

    public void connect(String host,int port){
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY,true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ClientHandler());
                        }
                    });
            ChannelFuture future = bootstrap.connect(host, port).sync();
            future.channel().closeFuture().sync();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            group.shutdownGracefully();
        }
    }

    class ClientHandler extends SimpleChannelInboundHandler{
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf byteBuf = (ByteBuf) msg;
            byte[] bytes = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(bytes);
            System.out.println("server say to client:"+new String(bytes,"UTF-8"));
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            byte[] bytes = "hello,netty".getBytes("UTF-8");
            ByteBuf byteBuf = Unpooled.buffer(bytes.length);
            byteBuf.writeBytes(byteBuf);
            ctx.writeAndFlush(byteBuf);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.close();
        }
    }
}
