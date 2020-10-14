package com.example.internetdemo.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author YZS
 */
@Slf4j
@Order(1)
@Component
public class Server implements CommandLineRunner {

    private EventLoopGroup bossGroup;
    private EventLoopGroup workGroup;

    private static final String OS_NAME = System.getProperty("os.name");
    private static boolean isLinuxPlatform = false;

    static {
        if (OS_NAME != null && OS_NAME.toLowerCase().contains("linux")) {
            isLinuxPlatform = true;
        }
    }
    public static boolean isLinuxPlatform() {
        return isLinuxPlatform;
    }

    @Autowired
    private ChildHandler childHandler;

    @PostConstruct
    public void init() {
        if (useEpoll()) {
            bossGroup = new EpollEventLoopGroup(1, new DefaultThreadFactory("boss"));
            workGroup = new EpollEventLoopGroup(0, new DefaultThreadFactory("worker"));
        } else {
            bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("boss"));
            workGroup = new NioEventLoopGroup(0, new DefaultThreadFactory("worker"));
        }
    }

    @PreDestroy
    public void destroy() {
        if (bossGroup != null) { bossGroup.shutdownGracefully(); }
        if (workGroup != null) { workGroup.shutdownGracefully(); }
    }

    private void start() {
        try {
            ServerBootstrap sb = new ServerBootstrap();
            sb.group(bossGroup, workGroup)
                .channel(useEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .option(NioChannelOption.SO_BACKLOG, 1024)
                .option(NioChannelOption.SO_REUSEADDR, true)
                .childOption(NioChannelOption.TCP_NODELAY, true)
                .childOption(NioChannelOption.SO_SNDBUF, 65535)
                .childOption(NioChannelOption.SO_RCVBUF, 65535)
                .childOption(NioChannelOption.SO_KEEPALIVE, false)
                .childOption(NioChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(childHandler);
            ChannelFuture cf = sb.bind(8916).sync();
            log.info("websocket service started，listening On address：{}", cf.channel().localAddress());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private static boolean useEpoll() {
        return isLinuxPlatform() && Epoll.isAvailable();
    }


    @Override
    public void run(String... args) throws Exception {
        start();
    }
}
