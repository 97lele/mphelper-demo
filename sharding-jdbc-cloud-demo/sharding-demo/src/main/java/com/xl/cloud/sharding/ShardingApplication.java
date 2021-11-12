package com.xl.cloud.sharding;

import com.xl.cloud.sharding.service.OrderInfoService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;


@SpringBootApplication
@MapperScan("com.xl.cloud.sharding.mapper")
@EnableDiscoveryClient
public class ShardingApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(ShardingApplication.class, args);
    }
}
