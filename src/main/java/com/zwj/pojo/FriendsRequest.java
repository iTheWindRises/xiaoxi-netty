package com.zwj.pojo;

import lombok.Data;

import java.util.Date;
import javax.persistence.*;

@Table(name = "friends_request")
@Data
public class FriendsRequest {
    @Id
    private String id;

    @Column(name = "send_user_id")
    private String sendUserId;

    @Column(name = "accept_user_id")
    private String acceptUserId;

    @Column(name = "request_date_time")
    private Date requestDateTime;

}