package com.example.internetdemo.websocket;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author YZS
 */
@Slf4j
@Component
public class ChildHandler extends ChannelInitializer<SocketChannel> {

    private EventExecutorGroup eventExecutorGroup;

    private MonitorHandler monitorHandler;

    private final LoggingHandler infoLogHandler = new LoggingHandler(LogLevel.INFO);

    @PostConstruct
    public void init() {
        eventExecutorGroup = new DefaultEventExecutorGroup(Runtime.getRuntime().availableProcessors() * 2);
        monitorHandler = new MonitorHandler();
    }

    @PreDestroy
    public void destroy() {
        eventExecutorGroup.shutdownGracefully();
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast(eventExecutorGroup, "HttpServerCodec", new HttpServerCodec());
        ch.pipeline().addLast(eventExecutorGroup, "HttpObjectAggregator", new HttpObjectAggregator(65536));
        ch.pipeline().addLast(eventExecutorGroup, "ChunkedWriteHandler", new ChunkedWriteHandler());
        ch.pipeline().addLast(eventExecutorGroup, "WebSocketServerProtocolHandler", new WebSocketServerProtocolHandler("/ws/demo", null, true));
        ch.pipeline().addLast(eventExecutorGroup, "WebSocketServerCompressionHandler", new WebSocketServerCompressionHandler());
        ch.pipeline().addLast("infoLogHandler", infoLogHandler);
        ch.pipeline().addLast("monitorHandler", monitorHandler);
    }
}
