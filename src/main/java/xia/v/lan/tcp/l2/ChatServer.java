package xia.v.lan.tcp.l2;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.soap.Text;

import java.util.Date;

import static io.netty.handler.codec.http.HttpUtil.isKeepAlive;

/**
 * @author chenhao
 * @description <p>
 * created by chenhao 2019/11/13 16:06
 */
//https://blog.csdn.net/qq_37598011/article/details/84146231
    //https://www.jianshu.com/p/56216d1052d7
    //hello chat
public class ChatServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatServer.class);

    private void init(){
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup work = new NioEventLoopGroup();
        try {
            ServerBootstrap boot = new ServerBootstrap();
            boot.group(boss,work)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG,1024)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast("logging",new LoggingHandler())
                                    .addLast("http-codec",new HttpServerCodec())
                                    .addLast("aggregator",new HttpObjectAggregator(65536))
                                    .addLast("http-chunked",new ChunkedWriteHandler())
                                    .addLast("user-handler",new NioWebSocketHandler());
                        }
                    });
            ChannelFuture future = boot.bind(8008).sync();
            LOGGER.info("server started successfully");
            future.channel().closeFuture().sync();
        }catch (Exception e){
            e.printStackTrace();
            LOGGER.info("run error");
        }finally {
            boss.shutdownGracefully();
            work.shutdownGracefully();
            LOGGER.info("server has been shutdown");
        }
    }

    class NioWebSocketHandler extends SimpleChannelInboundHandler{

        private WebSocketServerHandshaker serverHandshaker;

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
            LOGGER.info("收到消息：{}",msg);
            if(msg instanceof FullHttpRequest){
                //握手消息
                handleHttpRequest(ctx, (FullHttpRequest) msg);
            }else if(msg instanceof WebSocketFrame){
                handlerWebSocketFrame(ctx, (WebSocketFrame) msg);
            }
        }

        /**
         * 唯一的一次http请求，用于创建websocket
         * @param ctx
         * @param request
         */
        private void handleHttpRequest(ChannelHandlerContext ctx,FullHttpRequest request){
            //要求Upgrade为websocket，过滤掉get/Post
            if(!request.decoderResult().isSuccess()
                || !"websocket".equalsIgnoreCase(request.headers().get("Upgrade"))){
                //若不是websocket方式，则创建BAD_REQUEST的req，返回给客户端
                sendHttpResponse(ctx,request,new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
                return;
            }
            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory("ws://localhost:8081/websocket",null,false);
            serverHandshaker = wsFactory.newHandshaker(request);
            if(serverHandshaker == null){
                WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            }else{
                serverHandshaker.handshake(ctx.channel(),request);
            }
        }

        /**
         * 拒绝不合法的请求，并返回错误信息
         * */
        private void sendHttpResponse(ChannelHandlerContext ctx,
                                             FullHttpRequest req, DefaultFullHttpResponse res){
            // 返回应答给客户端
            if (res.status().code() != 200) {
                ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(),
                        CharsetUtil.UTF_8);
                res.content().writeBytes(buf);
                buf.release();
            }
            ChannelFuture f = ctx.channel().writeAndFlush(res);
            // 如果是非Keep-Alive，关闭连接
            if (!isKeepAlive(req) || res.status().code() != 200) {
                f.addListener(ChannelFutureListener.CLOSE);
            }
        }

        private void handlerWebSocketFrame(ChannelHandlerContext ctx,WebSocketFrame frame){
            // 判断是否关闭链路的指令
            if(frame instanceof CloseWebSocketFrame){
                serverHandshaker.close(ctx.channel(),((CloseWebSocketFrame) frame).retain());
                return;
            }
            // 判断是否ping消息
            if(frame instanceof PingWebSocketFrame){
                ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
                return;
            }
            // 本例程仅支持文本消息，不支持二进制消息
            if(! (frame instanceof TextWebSocketFrame)){
                LOGGER.error("本例程仅支持文本消息");
                throw new UnsupportedOperationException(String.format("%s frame type not supported",frame.getClass().getName()));
            }
            // 返回应答消息
            String text = ((TextWebSocketFrame) frame).text();
            LOGGER.info("服务端收到：{}",text);
            TextWebSocketFrame tws = new TextWebSocketFrame(new Date().toString() + ctx.channel().id() + ":" + text);
            //群发
            ChannelSupervise.send2All(tws);
            //单发
            //ctx.channel().writeAndFlush(tws);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            LOGGER.info("客户端加入连接：{}",ctx.channel());
            ChannelSupervise.addChannel(ctx.channel());
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            LOGGER.info("断开连接：{}",ctx.channel());
            ChannelSupervise.removeChannel(ctx.channel());
        }
    }

    public static void main(String[] args) {
        new ChatServer().init();
    }
}
