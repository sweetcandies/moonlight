package com.funiverise.register;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * @Author Funny
 * @Date: 2021-11-17 14:37:08
 */
@EnableEurekaServer
@SpringBootApplication
public class RegisterApplication {
	
    public static void main( String[] args ){
    	SpringApplication.run(RegisterApplication.class, args );
    }
}
