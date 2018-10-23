package com.zwj.service.impl;

import com.zwj.enums.MsgActionEnum;
import com.zwj.enums.MsgSignFlagEnum;
import com.zwj.enums.SearchFriendsStatusEnum;
import com.zwj.mapper.*;
import com.zwj.netty.ChatMsg;
import com.zwj.netty.DataContent;
import com.zwj.netty.UserChannelRel;
import com.zwj.pojo.FriendsRequest;
import com.zwj.pojo.MyFriends;
import com.zwj.pojo.User;
import com.zwj.pojo.vo.FriendRequestVO;
import com.zwj.pojo.vo.MyFriendsVO;
import com.zwj.service.UserService;
import com.zwj.utils.*;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MyFriendsMapper myFriendsMapper;
    @Autowired
    private FriendsRequestMapper friendsRequestMapper;
    @Autowired
    private UserMapperCustom userMapperCustom;
    @Autowired
    private ChatMsgMapper chatMsgMapper;
    @Autowired
    private QRCodeUtils qrCodeUtils;
    @Autowired
    private FastDFSClient fastDFSClient;


    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public boolean queryUsernameIsExist(String username) {
        User user = new User();
        user.setUsername(username);
        User result = userMapper.selectOne(user);
        return result!= null ?true:false;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public User queryUserForLogin(String username, String password) throws Exception {

        Example userExample = new Example(User.class);

        Example.Criteria criteria = userExample.createCriteria();
        criteria.andEqualTo("username",username);
        criteria.andEqualTo("password",MD5Utils.getMD5Str(password));

        User result = userMapper.selectOneByExample(userExample);
        return result;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public User saveUser(User user) throws Exception {
        user.setId(Sid.nextShort());
        user.setNickname(user.getUsername());
        user.setPassword(MD5Utils.getMD5Str(user.getPassword()));
        user.setFaceImage("");
        user.setFaceImageBig("");


        //为每个用户生成唯一的二维码 格式:xiaoxi_qrcode:[userId]
        String qrCodePath = "/Users/thewindrises/server/xiaoxi/qrcode/" + user.getUsername() +"qrcode.png";
        qrCodeUtils.createQRCode(qrCodePath,"xiaoxi_qrcode:"+user.getUsername());
        MultipartFile qrcodeFile = FileUtils.fileToMultipart(qrCodePath);
        String qrCodeUrl = fastDFSClient.uploadQRCode(qrcodeFile);
        user.setQrcode(qrCodeUrl);

        userMapper.insert(user);
        return user;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public User updateUserInfo(User user) {
        userMapper.updateByPrimaryKeySelective(user);
        return userMapper.selectByPrimaryKey(user.getId());
    }


    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Integer preconditionSearchFriends(String myUserId, String friendUsername) {
        //前置条件-1.搜索用户名如果不存在
        User user = queryUserByUsername(friendUsername);
        if (user== null)
            return SearchFriendsStatusEnum.USER_NOT_EXIST.status;

        //前置条件-2.搜索是自己
        if (myUserId.equals(user.getId()))
            return SearchFriendsStatusEnum.NOT_YOURSELF.status;

        //前置条件-3.搜索已经是你的好友
        Example example = new Example(MyFriends.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("myUserId",myUserId);
        criteria.andEqualTo("myFriendUserId",user.getId());
        MyFriends myFriendRel = myFriendsMapper.selectOneByExample(example);
        if (myFriendRel != null)
            return SearchFriendsStatusEnum.ALREADY_FRIENDS.status;

        return SearchFriendsStatusEnum.SUCCESS.status;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    User queryUserById(String userId) {

        return userMapper.selectByPrimaryKey(userId);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public User queryUserByUsername(String username) {

        Example example = new Example(User.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("username",username);

        return userMapper.selectOneByExample(example);

    }
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void sendFriendRequest(String myUserId, String friendUsername) {

        User friend = queryUserByUsername(friendUsername);

        Example fre = new Example(FriendsRequest.class);
        Example.Criteria frc = fre.createCriteria();
        frc.andEqualTo("sendUserId",myUserId);
        frc.andEqualTo("acceptUserId",friend.getId());
        FriendsRequest friendsRequest = friendsRequestMapper.selectOneByExample(fre);

        if (friendsRequest==null) {
            //2.如果不是你的好友并且好友记录没有添加,新增好友记录
            String requestId = Sid.nextShort();

            FriendsRequest request = new FriendsRequest();
            request.setId(requestId);
            request.setSendUserId(myUserId);
            request.setAcceptUserId(friend.getId());
            request.setRequestDateTime(new Date());

            friendsRequestMapper.insert(request);
        }
    }
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<FriendRequestVO> queryFriendRequestList(String acceptUserId) {
        return userMapperCustom.queryFriendRequestList(acceptUserId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void deleteFriendRequest(String acceptUserId, String sendUserId) {

        Example example = new Example(FriendsRequest.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("sendUserId",sendUserId);
        criteria.andEqualTo("acceptUserId",acceptUserId);
        friendsRequestMapper.deleteByExample(example);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void passFriendRequest(String acceptUserId, String sendUserId) {
        saveFriends(acceptUserId,sendUserId);
        saveFriends(sendUserId,acceptUserId);

        deleteFriendRequest(acceptUserId,sendUserId);


        //使用websocket主动推送消息到请求发起者,更新他的通讯录为最新
        Channel sendchannel = UserChannelRel.get(sendUserId);
        if (sendchannel != null) {
            DataContent dataContent = new DataContent();
            dataContent.setAction(MsgActionEnum.PULL_FRIEND.type);

            sendchannel.writeAndFlush(
                    new TextWebSocketFrame(JsonUtils.objectToJson(dataContent)));
        }
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<MyFriendsVO> queryMyFriends(String userId) {
        List<MyFriendsVO> myFirends = userMapperCustom.queryMyFriends(userId);
        return myFirends;
    }
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public String saveMsg(ChatMsg chatMsg) {

        com.zwj.pojo.ChatMsg msgDB = new com.zwj.pojo.ChatMsg();
        String msgId = Sid.nextShort();
        msgDB.setId(msgId);
        msgDB.setAcceptUserId(chatMsg.getReceiverId());
        msgDB.setSendUserId(chatMsg.getSenderId());
        msgDB.setCreateTime(new Date());
        msgDB.setSignFlag(MsgSignFlagEnum.unsign.type);
        msgDB.setMsg(chatMsg.getMsg());

        chatMsgMapper.insert(msgDB);
        return msgId;
    }
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updateMsgSigned(List<String> msgIdList) {
        userMapperCustom.batchUpdateMsgSigned(msgIdList);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<com.zwj.pojo.ChatMsg> getUnReadMsgList(String acceptUserId) {

        Example example = new Example(com.zwj.pojo.ChatMsg.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("signFlag",0);
        criteria.andEqualTo("acceptUserId",acceptUserId);
        List<com.zwj.pojo.ChatMsg> result = chatMsgMapper.selectByExample(example);
        return result;
    }

    private void saveFriends(String acceptUserId, String sendUserId) {
        MyFriends friends = new MyFriends();
        friends.setId(Sid.nextShort());
        friends.setMyUserId(acceptUserId);
        friends.setMyFriendUserId(sendUserId);

        myFriendsMapper.insert(friends);
    }
}
