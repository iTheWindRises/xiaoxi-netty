package com.zwj.controller;

import com.zwj.pojo.User;
import com.zwj.pojo.vo.UserVO;
import com.zwj.service.UserService;
import com.zwj.utils.JSONResult;
import com.zwj.utils.MD5Utils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/u")
public class UserController {

    @Autowired
    private UserService userService;

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
}
