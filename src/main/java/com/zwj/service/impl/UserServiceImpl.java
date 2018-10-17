package com.zwj.service.impl;

import com.zwj.mapper.UserMapper;
import com.zwj.pojo.User;
import com.zwj.service.UserService;
import com.zwj.utils.FastDFSClient;
import com.zwj.utils.FileUtils;
import com.zwj.utils.MD5Utils;
import com.zwj.utils.QRCodeUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

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
    User queryUserById(String userId) {
        return userMapper.selectByPrimaryKey(userId);
    }
}
