package com.zwj.service;

import com.zwj.netty.ChatMsg;
import com.zwj.pojo.User;
import com.zwj.pojo.vo.FriendRequestVO;
import com.zwj.pojo.vo.MyFriendsVO;

import java.util.List;

public interface UserService {
    /**
     * 判断用户名是否存在
     * @param username
     * @return
     */
    boolean queryUsernameIsExist(String username);

    /**
     * 查询用户名密码正确性
     * @param username
     * @param password
     * @return
     */
    User queryUserForLogin(String username,String password) throws Exception;

    /**
     * 注册用户
     * @param user
     * @return
     */
    User saveUser(User user) throws Exception;

    /**
     * 修改用户记录
     * @param user
     */
    User updateUserInfo(User user);

    /**
     * 添加好友
     * @param myUserId
     * @param friendUsername
     * @return
     */
    Integer preconditionSearchFriends(String myUserId,String friendUsername);

    User queryUserByUsername(String username);

    /**
     * 添加好友请求记录
     * @param myUserId
     * @param friendUsername
     */
    void sendFriendRequest(String myUserId, String friendUsername);


    List<FriendRequestVO> queryFriendRequestList(String acceptUserId);

    void deleteFriendRequest(String acceptUserId,String sendUserId);

    /**
     * 通过好友请求
     * @param acceptUserId
     * @param sendUserId
     */
    void passFriendRequest(String acceptUserId,String sendUserId);

    /**
     * 查询好友列表
     * @param userId
     * @return
     */
    List<MyFriendsVO> queryMyFriends(String userId);

    /**
     * 保存聊天消息到数据库
     * @param chatMsg
     * @return
     */
    String saveMsg(ChatMsg chatMsg);

    /**
     * 批量签收消息
     * @param msgIdList
     */
    void updateMsgSigned (List<String> msgIdList);

    List<com.zwj.pojo.ChatMsg> getUnReadMsgList(String acceptUserId);
}
