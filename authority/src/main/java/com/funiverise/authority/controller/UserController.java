package com.funiverise.authority.controller;


import com.funiverise.authority.service.IUserService;
import com.funiverise.enums.ReturnResultEnums;
import com.funiverise.message.ReturnMsg;
import com.funiverise.object.vo.UserDetailVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author Funny
 * @since 2021-12-07
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private IUserService userService;

    @GetMapping("detail")
    public ReturnMsg<UserDetailVO> findUserDetail(@RequestParam String username) {
        if (StringUtils.isBlank(username)) {
            return ReturnMsg.initFailResult(ReturnResultEnums.R_000001);
        }
        return userService.getUserDetail(username);
    }

    /**
     * 用户注册
     * @param detailVO  用户详情VO
     * @return  是否注册成功
     */
    @PostMapping("register")
    public ReturnMsg<String> registerUser(UserDetailVO detailVO) {
        return userService.addNewUser(detailVO);
    }


}

