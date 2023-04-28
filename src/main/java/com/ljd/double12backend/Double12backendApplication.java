package com.ljd.double12backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.ljd.double12backend.dao")
public class Double12backendApplication {

    public static void main(String[] args) {
        SpringApplication.run(Double12backendApplication.class, args);
    }

}
