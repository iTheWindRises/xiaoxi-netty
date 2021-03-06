package com.zwj.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

public class WSServerInitialzer extends ChannelInitializer<SocketChannel> {

    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();

        //websocket 基于http协议,所以要有http编解码器
        pipeline.addLast(new HttpServerCodec());
        //对写大数据流的支持
        pipeline.addLast(new ChunkedWriteHandler());
        //对httpMessage进行聚合,聚合成FullHTTPRequest或FullHTTPResponse
        //几乎在netty中的编程,都会使用到此handler
        pipeline.addLast(new HttpObjectAggregator(1024*64));

        //----------------以上是用于支持http协议-------------------//

        //----------------添加心跳支持-------------------//
        //针对客户端,如果在1分钟时没有想服务端发送读写心跳ALL,则主动断开
        //如果是读空闲,或者写空闲不做处理
        pipeline.addLast(new IdleStateHandler(60,60,90));
        // 自定义的空闲状态检测
        pipeline.addLast(new HeartBeatHandler());

        //----------------以上是用于支持http协议-------------------//

        //
        /**
         * websocket 服务器处理的协议,用于指定给客户端连接访问的路由:/ws
         * 这个handler会帮你处理一些繁重的复杂的工作
         * 会帮你处理握手动作:handshaking(close,ping,pong) ping+ pong  心跳
         * 对于websocket,都是以frames进行传输的,不同的数据类型对应的frames也不同
         */
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));

        //自定义handler
        pipeline.addLast(new ChatHandler());
    }
}
