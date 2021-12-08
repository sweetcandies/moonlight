package com.funiverise.gateway;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@MapperScan("com.funiverise.gateway.dao")
public class GatewayApplication {
    public static void main( String[] args ){
    	SpringApplication.run(GatewayApplication.class, args );

    }
}
