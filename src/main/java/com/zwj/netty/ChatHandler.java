package com.zwj.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.time.LocalDateTime;

/**
 * 处理消息的handler
 * TextWebSocketFrame:在netty中,是用于为websocket专门处理文本的对象,frame是载体
 */
public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    //用于记录和管理所有客户端的channel
    private static ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);


    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame msg) throws Exception {
        //获取客户端传输过来的消息
        String content = msg.text();
        System.out.println("接收到的数据"+ content);

//        for (Channel client : clients) {
//            client.writeAndFlush(new TextWebSocketFrame("[服务器接收到消息]:"+ LocalDateTime.now()
//                +"接收到消息,消息为:"+content));
//        }

        //与上面的for循环一致
        clients.writeAndFlush(new TextWebSocketFrame("[服务器接收到消息]:"+ LocalDateTime.now()
                +"接收到消息,消息为:"+content));

    }

    /**
     * 当客户端连接到服务端之后,获取客户端的channel放到ChannelGroup进行管理
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        clients.add(ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        //当触发handlerRemoved,ChannelGroup会自动移除对应客户端的channel对应->
        //clients.remove(ctx.channel());
        System.out.println("客户端断开,channel对应的长id为"+ctx.channel().id().asLongText());
        System.out.println("客户端断开,channel对应的短id为"+ctx.channel().id().asShortText());


    }
}
