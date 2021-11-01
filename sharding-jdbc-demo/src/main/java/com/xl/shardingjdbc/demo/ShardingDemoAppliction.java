package com.xl.shardingjdbc.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(value = {"com.xl.mphelper","com.xl.shardingjdbc.demo"})
@MapperScan(basePackages = "com.xl.shardingjdbc.demo.mapper")
public class ShardingDemoAppliction {
    public static void main(String[] args) {
        SpringApplication.run(ShardingDemoAppliction.class,args);
    }
}
