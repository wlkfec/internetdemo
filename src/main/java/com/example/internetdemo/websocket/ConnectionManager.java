package com.example.internetdemo.websocket;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.ChannelMatcher;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;

public class ConnectionManager {

    private static final ChannelGroup CONNECTION_GROUP = new DefaultChannelGroup("fenzhongsiFront", GlobalEventExecutor.INSTANCE);

    public static void add(Channel channel) {
        CONNECTION_GROUP.add(channel);
    }

    public static void broadcast(Object msg) {
        if (msg instanceof TextWebSocketFrame || msg instanceof BinaryWebSocketFrame) {
            CONNECTION_GROUP.writeAndFlush(msg);
            return;
        }
        if (msg instanceof String) {
            CONNECTION_GROUP.writeAndFlush(new TextWebSocketFrame((String) msg));
        }
    }

    public static ChannelGroupFuture broadcast(Object msg, ChannelMatcher matcher) {
        return CONNECTION_GROUP.writeAndFlush(msg, matcher);
    }

    public static ChannelGroup flush() {
        return CONNECTION_GROUP.flush();
    }

    public static boolean discard(Channel channel) {
        return CONNECTION_GROUP.remove(channel);
    }

    public static ChannelGroupFuture disconnect() {
        return CONNECTION_GROUP.disconnect();
    }

    public static ChannelGroupFuture disconnect(ChannelMatcher matcher) {
        return CONNECTION_GROUP.disconnect(matcher);
    }

    public static boolean contains(Channel channel) {
        return CONNECTION_GROUP.contains(channel);
    }

    public static int size() {
        return CONNECTION_GROUP.size();
    }
}