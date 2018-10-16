package com.zwj.service.impl;

import com.zwj.mapper.UserMapper;
import com.zwj.pojo.User;
import com.zwj.service.UserService;
import com.zwj.utils.MD5Utils;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public boolean queryUsernameIsExist(String username) {
        User user = new User();
        user.setNickname(username);
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

    @Override
    public User saveUser(User user) throws Exception {
        user.setId(Sid.nextShort());
        user.setNickname(user.getUsername());
        user.setPassword(MD5Utils.getMD5Str(user.getPassword()));
        user.setFaceImage("");
        user.setFaceImageBig("");

        //TODO为每个用户生成唯一的二维码
        user.setQrcode("");

        userMapper.insert(user);
        return user;
    }
}
