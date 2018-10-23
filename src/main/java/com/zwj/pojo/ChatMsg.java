package com.zwj.pojo;

import lombok.Data;

import java.util.Date;
import javax.persistence.*;

@Table(name = "chat_msg")
@Data
public class ChatMsg {
    @Id
    private String id;

    @Column(name = "send_user_id")
    private String sendUserId;

    @Column(name = "accept_user_id")
    private String acceptUserId;

    private String msg;

    @Column(name = "sign_flag")
    private Integer signFlag;

    @Column(name = "create_time")
    private Date createTime;

}