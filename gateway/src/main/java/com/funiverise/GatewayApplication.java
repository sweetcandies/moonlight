package com.funiverise;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@MapperScan("com.funiverise.authority.dao")
public class GatewayApplication {
    public static void main( String[] args ){
    	SpringApplication.run(GatewayApplication.class, args );

    }
}
