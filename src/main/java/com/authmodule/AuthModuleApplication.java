package com.authmodule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AuthModuleApplication {

    public static void main(String[] args) {
    	System.out.println("Checking");
        SpringApplication.run(AuthModuleApplication.class, args);
    }
}
