package com.zwj.netty;

import com.zwj.utils.SpringUtil;
import com.zwj.enums.MsgActionEnum;
import com.zwj.service.UserService;
import com.zwj.utils.JsonUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 处理消息的handler
 * TextWebSocketFrame:在netty中,是用于为websocket专门处理文本的对象,frame是载体
 */
public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    //用于记录和管理所有客户端的channel
    public static ChannelGroup users = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);


    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame msg) throws Exception {
        //获取客户端传输过来的消息
        String content = msg.text();

        Channel currentChannel = channelHandlerContext.channel();

        //1.获取客户端发来的消息
        DataContent dataContent = JsonUtils.jsonToPojo(content,DataContent.class);
        Integer action = dataContent.getAction();
        //2.判断消息类型,根据不同的类型来处理不同的业务
        if (action == MsgActionEnum.CONNECT.type) {
            //2.1当websocket 第一次open的时候,初始化channel,把用户的channel和userid关联起来
            String senderId = dataContent.getChatMsg().getSenderId();
            UserChannelRel.put(senderId,currentChannel);

            //测试
            for (Channel channel : users) {
                System.out.println(channel.id().asLongText());
            }
            UserChannelRel.output();


        }else if (action == MsgActionEnum.CHAT.type) {
            //2.2聊天类型的消息,把聊天记录保存到数据库,同时标记消息的签收状态[未签收]
            ChatMsg chatMsg = dataContent.getChatMsg();
            String msgText = chatMsg.getMsg();
            String receiverId = chatMsg.getReceiverId();
            String senderId = chatMsg.getSenderId();
            //保存消息到数据库,并且标记为未签收
            UserService userService = (UserService) SpringUtil.getBean("userServiceImpl");
            String msgId = userService.saveMsg(chatMsg);
            chatMsg.setMsgId(msgId);


            DataContent dataContentMsg = new DataContent();
            dataContentMsg.setChatMsg(chatMsg);

            //发送消息
            //从全局用户Channel关系中获取接收方的channel
            Channel receiverChannel = UserChannelRel.get(receiverId);
            if (receiverChannel == null) {
                //TODO channel为空代表用户离线,推送消息(JPush,个推,小米推送)
            }else {
                //当receiverChannel不为空,从ChannelGroup取查找对应的channel是否存在
                Channel findChannel = users.find(receiverChannel.id());
                if (findChannel != null) {
                    //用户在线,发送消息
                    receiverChannel.writeAndFlush(
                            new TextWebSocketFrame(JsonUtils.objectToJson(dataContentMsg)));
                }else {
                    //用户离线
                    //TODO 推送消息(JPush,个推,小米推送)
                }
            }

        }else if (action == MsgActionEnum.SIGNED.type) {
            //2.3签收消息,针对具体的消息进行签收,修改数据库中对应的消息签收状态[已签收]
            UserService userService = (UserService) SpringUtil.getBean("userServiceImpl");
            //扩展字段在signed类型的消息中,代表需要签收的消息id,
            String msgIdStr = dataContent.getExtand();
            String[] msgIds = msgIdStr.split(",");

            List<String> msgIdList = new ArrayList<>();
            for (String msgId : msgIds) {
                if (StringUtils.isNotBlank(msgId)){
                    msgIdList.add(msgId);
                }
            }

            System.out.println(msgIdList.toString());

            if (msgIdList != null && !msgIdList.isEmpty() && msgIdList.size()>0) {
                //批量签收
                userService.updateMsgSigned(msgIdList);
            }

        }else if (action == MsgActionEnum.KEEPALIVE.type) {
            //TODO 2.4心跳类型的消息
            System.out.println("收到来自channel为:"+currentChannel+",的心跳包");

        }



    }

    /**
     * 当客户端连接到服务端之后,获取客户端的channel放到ChannelGroup进行管理
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        users.add(ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {

        String channelId = ctx.channel().id().asShortText();
        System.out.println("客户端被移除"+channelId);

        //当触发handlerRemoved,ChannelGroup会自动移除对应客户端的channel对应->
        users.remove(ctx.channel());

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        //发送异常关闭连接,随后从CHannelGroup移除
        ctx.channel().close();
        users.remove(ctx.channel());
    }
}
