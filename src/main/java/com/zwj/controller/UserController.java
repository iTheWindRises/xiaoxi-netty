package com.zwj.controller;

import com.zwj.enums.OperatorFriendRequestTypeEnum;
import com.zwj.enums.SearchFriendsStatusEnum;
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

        return JSONResult.ok(userVO);
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

        return JSONResult.ok(userVO);
    }

    /**
     * 搜索好友,根据账号进行匹配查询,而不是模糊查询
     * @param myUserId
     * @param friendUsername
     * @return
     * @throws Exception
     */
    @PostMapping("/search")
    public JSONResult searchUser(String myUserId,String friendUsername) throws Exception {

        //1.判断myUserId,friendId不为空
        if (StringUtils.isBlank(myUserId) || StringUtils.isBlank(friendUsername)) {
            return JSONResult.errorMsg("");
        }

        //前置条件-1.搜索用户名如果不存在
        //前置条件-2.搜索是自己
        //前置条件-3.搜索已经是你的好友
        Integer status = userService.preconditionSearchFriends(myUserId, friendUsername);
        if (status== SearchFriendsStatusEnum.SUCCESS.status) {
            User userResult = userService.queryUserByUsername(friendUsername);
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(userResult,userVO);

            return JSONResult.ok(userVO);

        }else {
            String errorMsg = SearchFriendsStatusEnum.getMsgByKey(status);
            return JSONResult.errorMsg(errorMsg);
        }

    }


    @PostMapping("/addFriend")
    public JSONResult addFriend(String myUserId,String friendUsername) throws Exception {

        //1.判断myUserId,friendId不为空
        if (StringUtils.isBlank(myUserId) || StringUtils.isBlank(friendUsername)) {
            return JSONResult.errorMsg("");
        }

        //前置条件-1.搜索用户名如果不存在
        //前置条件-2.搜索是自己
        //前置条件-3.搜索已经是你的好友
        Integer status = userService.preconditionSearchFriends(myUserId, friendUsername);
        if (status== SearchFriendsStatusEnum.SUCCESS.status) {
            userService.sendFriendRequest(myUserId,friendUsername);

        }else {
            String errorMsg = SearchFriendsStatusEnum.getMsgByKey(status);
            return JSONResult.errorMsg(errorMsg);
        }

        return JSONResult.ok();

    }

    @PostMapping("/queryFriendRequests")
    public JSONResult queryFriendRequests(String userId) throws Exception {

        //1.判断myUserId,friendId不为空
        if (StringUtils.isBlank(userId)) {
            return JSONResult.errorMsg("");
        }
        //2.查询用户接受到的好友申请
        return JSONResult.ok(userService.queryFriendRequestList(userId));

    }

    /**
     * 接收方通过或者忽略好友请求
     * @param acceptUserId
     * @param sendUserId
     * @param operType
     * @return
     * @throws Exception
     */
    @PostMapping("/operFriendRequest")
    public JSONResult operFriendRequest(String acceptUserId,String sendUserId,Integer operType) throws Exception {

        //1.判断myUserId,friendId不为空
        if (StringUtils.isBlank(acceptUserId)||StringUtils.isBlank(sendUserId)|| operType==null) {
            return JSONResult.errorMsg("");
        }
        //如果operType没有对应的枚举值,则直接抛出空错误信息
        if (StringUtils.isBlank(OperatorFriendRequestTypeEnum.getMsgByType(operType))) {
            return JSONResult.errorMsg("");
        }

        if (operType==OperatorFriendRequestTypeEnum.IGNORE.type) {
            //2.如果是忽略好友请求,则直接删除好友请求的数据记录
            userService.deleteFriendRequest(acceptUserId,sendUserId);
        }else if (operType==OperatorFriendRequestTypeEnum.PASS.type) {
            //3.如果是通过好友请求,则直接互相添加好友记录的数据记录
            userService.passFriendRequest(acceptUserId,sendUserId);
        }


        return JSONResult.ok(userService.queryMyFriends(acceptUserId));
    }

    /**
     * 查询我的好友列表
     * @param userId
     * @return
     * @throws Exception
     */
    @PostMapping("/myFriends")
    public JSONResult myFriends(String userId) throws Exception {

        //1.判断userId不为空
        if (StringUtils.isBlank(userId)) {
            return JSONResult.errorMsg("");
        }

        //2.查询好友列表


        return JSONResult.ok(userService.queryMyFriends(userId));
    }

}
