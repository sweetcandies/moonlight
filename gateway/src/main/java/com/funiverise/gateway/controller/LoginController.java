package com.funiverise.gateway.controller;

import org.springframework.beans.factory.annotation.Value;
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

    @Value("${server.host}")
    private String hello;

    @GetMapping("hello")
    public void sayHello() {
        System.out.println(hello);
    }

}
