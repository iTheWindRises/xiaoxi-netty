package com.zwj.pojo.vo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;

@Data
public  class UserVO {
    private String id;

    private String username;

    private String faceImage;

    private String faceImageBig;

    private String nickname;

    private String qrcode;

}