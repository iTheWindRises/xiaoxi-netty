package com.zwj.controller;

import com.zwj.pojo.User;
import com.zwj.pojo.bo.UserBO;
import com.zwj.pojo.vo.UserVO;
import com.zwj.service.UserService;
import com.zwj.utils.FastDFSClient;
import com.zwj.utils.FileUtils;
import com.zwj.utils.JSONResult;
import com.zwj.utils.MD5Utils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/u")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private FastDFSClient fastDFSClient;

    @PostMapping("/registOrLogin")
    public JSONResult registOrLogin(@RequestBody User user) throws Exception {

        //判断用户名和密码不能为空
        if (user == null || StringUtils.isBlank(user.getUsername())
             ||StringUtils.isBlank(user.getPassword())) {
            return JSONResult.errorMsg("用户名和密码不能为空");
        }
        //1.判断用户名是否存在,如果存在登录,不存在注册
        boolean isExist = userService.queryUsernameIsExist(user.getUsername());
        User userResult= null;
        if (isExist) {
            //1.1登录
            userResult = userService.queryUserForLogin(user.getUsername(), user.getPassword());
            if (userResult == null) {
                return JSONResult.errorMsg("用户名或密码不正确");
            }
        }else {
            //1.2注册
            userResult = userService.saveUser(user);
        }

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userResult,userVO);
        return JSONResult.ok(userVO);
    }

    @PostMapping("/uploadFaceBace64")
    public JSONResult uploadFaceBace64(@RequestBody UserBO userBO) throws Exception {
        if (userBO.getUserId()==null ||StringUtils.isBlank(userBO.getUserId())) {
            return JSONResult.errorMsg("用户id错误");
        }
        //获取前端传过来的base64字符串,然后转换为文件对象在上传
        String base64Data = userBO.getFaceData();
        String userFacePath =  "C:\\"+userBO.getUserId() +"userface.png";
        FileUtils.base64ToFile(userFacePath,base64Data);

        //上传文件到fastdfs
        MultipartFile faceFile = FileUtils.fileToMultipart(userFacePath);
        String url = fastDFSClient.uploadBase64(faceFile);

        //获取缩略图的地址url
        String thump = "_80x80.";
        String[] arr = url.split("\\.");
        String thumpImgUrl = arr[0] + thump + arr[1];

        //更新用户头像
        User user = new User();
        user.setId(userBO.getUserId());
        user.setFaceImage(thumpImgUrl);
        user.setFaceImageBig(url);

        User userResult = userService.updateUserInfo(user);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userResult,userVO);

        return JSONResult.ok(userResult);
    }

    @PostMapping("/setNickName")
    public JSONResult setNickName(@RequestBody UserBO userBO) throws Exception {
        //更新用户昵称
        User user = new User();
        user.setId(userBO.getUserId());
        user.setNickname(userBO.getNickname());

        User userResult = userService.updateUserInfo(user);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userResult,userVO);

        return JSONResult.ok(userResult);
    }
}
