package com.zwj.pojo.vo;

import lombok.Data;

/**
 * 这是好友请求发送方的信息
 */
@Data
public  class FriendRequestVO {
    private String sendUserId;

    private String sendUsername;

    private String sendFaceImage;


    private String sendNickname;


}