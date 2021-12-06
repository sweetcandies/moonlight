package com.funiverise.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Funny
 * @Email hanyuefan776@163.com
 */
@RestController("test")
public class TestConfig {

    @GetMapping("sout")
    public void sayHello() {
        System.out.println("hello world");
    }
}
