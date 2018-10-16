package com.zwj.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.stereotype.Component;

@Component
public class WebSocketServer {

    private static class SingletionWSS{
        static final WebSocketServer instance = new WebSocketServer();
    }

    public static WebSocketServer getInstance() {
        return SingletionWSS.instance;
    }

    private EventLoopGroup mainGroup;
    private EventLoopGroup subGroup;
    private ServerBootstrap serverBootstrap;
    private ChannelFuture future;

    private WebSocketServer() {
        mainGroup = new NioEventLoopGroup();
        subGroup = new NioEventLoopGroup();
        serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(mainGroup,subGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new WSServerInitialzer());
    }

    public void start() {
        this.future = serverBootstrap.bind(8089);
        System.out.println("netty websocket server 启动完毕....");
    }
}
