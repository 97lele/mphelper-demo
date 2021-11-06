package com.xl.cloud.demo;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.StandardEnvironment;

@SpringBootApplication
@EnableDiscoveryClient
@Slf4j
public class NacosServerApplication {
    public static void main(String[] args) throws NacosException {
        ConfigurableApplicationContext run = SpringApplication.run(NacosServerApplication.class, args);
    }
}
