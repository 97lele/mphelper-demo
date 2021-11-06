package com.xl.cloud.sharding;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


@SpringBootApplication
@MapperScan("com.xl.cloud.sharding.mapper")
@EnableDiscoveryClient
public class ShardingApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShardingApplication.class, args);
    }
}
