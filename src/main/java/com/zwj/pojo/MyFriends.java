package com.zwj.pojo;

import lombok.Data;

import javax.persistence.*;

@Table(name = "my_friends")
@Data
public class MyFriends {
    @Id
    private String id;

    @Column(name = "my_user_id")
    private String myUserId;

    @Column(name = "my_friend_user_id")
    private String myFriendUserId;


}