package com.xl.mphelper.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author tanjl11
 * @date 2021/10/27 9:42
 */
@SpringBootApplication(scanBasePackages = "com.xl.mphelper.*")
@MapperScan(basePackages="com.xl.mphelper.example.mapper")
public class MpHelperApplication {
    public static void main(String[] args) {
        SpringApplication.run(MpHelperApplication.class, args);
    }
}
