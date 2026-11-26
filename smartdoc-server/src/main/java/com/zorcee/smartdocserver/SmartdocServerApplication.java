package com.zorcee.smartdocserver;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.zorcee.smartdocserver.mapper")
public class SmartdocServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartdocServerApplication.class, args);
    }
}









