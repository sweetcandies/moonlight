package com.funiverise.gateway.controller;

import com.funiverise.gateway.entity.User;
import com.funiverise.gateway.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Funny
 * @version 1.0
 * @description: 登录模块controller
 * @date 2021/12/6 17:10
 */
@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private IUserService service;

    @GetMapping("hello")
    public void sayHello() {
        User user = service.getById("1");
        if (user == null) {
            System.out.println("没查到哦!");
        }
    }

}
