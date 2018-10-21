package com.zwj.mapper;

import com.zwj.pojo.User;
import com.zwj.pojo.vo.FriendRequestVO;
import com.zwj.pojo.vo.MyFriendsVO;
import com.zwj.utils.MyMapper;

import java.util.List;

public interface UserMapperCustom extends MyMapper<FriendRequestVO> {
    List<FriendRequestVO> queryFriendRequestList(String acceptUserId);

    List<MyFriendsVO> queryMyFriends(String userId);
}