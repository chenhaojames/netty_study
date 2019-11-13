package xia.v.lan.tcp.l2;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author chenhao
 * @description <p>
 * created by chenhao 2019/11/13 19:30
 */
@Slf4j
public class ChannelSupervise {

    private static ChannelGroup globalGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static ConcurrentMap<String, ChannelId> map = new ConcurrentHashMap();

    public static void addChannel(Channel channel){
        globalGroup.add(channel);
        map.put(channel.id().asShortText(),channel.id());
    }

    public static void removeChannel(Channel channel){
        globalGroup.remove(channel);
        map.remove(channel.id().asLongText());
    }

    public static Channel findChannel(String id){
        return globalGroup.find(map.get(id));
    }

    public static void send2All(TextWebSocketFrame frame){
        globalGroup.writeAndFlush(frame);
    }



}
