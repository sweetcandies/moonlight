package com.funiverise.authority.controller;

import com.funiverise.authority.entity.User;
import com.funiverise.authority.service.IUserService;
import com.funiverise.enums.ReturnResultEnums;
import com.funiverise.message.ReturnMsg;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author Funny
 * @version 1.0
 * @description: 登录模块controller
 * @date 2021/12/6 17:10
 */
@RestController
@RequestMapping("/auth")
public class LoginController {

    @Autowired
    private IUserService service;

    @GetMapping("hello")
    public void sayHello() {
        User user = service.getById("d2fd24def39846b58a3780180ec3a4ca");
        if (user == null) {
            System.out.println("没查到哦!");
        } else {
            System.out.println(user);
        }
    }

    /**
     * @description: 用户登录
     * @param: [username, password, app]
     * @return: com.funiverise.message.ReturnMsg<java.lang.String>
     * @author: Funny
     * @date: 2021/12/20 10:45
     */
    @PostMapping("login")
    @ResponseBody
    public ReturnMsg<String> loginByPassword(String username, String password) {

        if (StringUtils.isAnyBlank(username,password)) {
            return ReturnMsg.initFailResult(ReturnResultEnums.R_000005);
        }
        return service.loginByPassword(username,password);
    }

}
