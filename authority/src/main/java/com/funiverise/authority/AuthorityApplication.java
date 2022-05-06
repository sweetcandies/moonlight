package com.funiverise.authority;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author Funny
 * @version 1.0
 * @description: TODO
 * @date 2022/2/25 15:12
 */

@SpringBootApplication
@MapperScan("com.funiverise.authority.dao")
public class AuthorityApplication {

    public static void main( String[] args ){
        SpringApplication.run(AuthorityApplication.class, args );

    }
}
