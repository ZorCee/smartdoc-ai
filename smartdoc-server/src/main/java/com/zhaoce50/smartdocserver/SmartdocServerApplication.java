package com.zhaoce50.smartdocserver;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.zhaoce50.smartdocserver.mapper")
public class SmartdocServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartdocServerApplication.class, args);
    }
}









