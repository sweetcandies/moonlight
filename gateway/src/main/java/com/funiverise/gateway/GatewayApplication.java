package com.funiverise.gateway;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;


@SpringBootApplication
@MapperScan("com.funiverise.gateway.dao")
@EnableFeignClients(basePackages = "com.funiverise")

public class GatewayApplication {
    public static void main( String[] args ){
    	SpringApplication.run(GatewayApplication.class, args );

    }
}
