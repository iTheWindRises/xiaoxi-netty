package com.zwj.service;

import com.zwj.pojo.User;

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
}
