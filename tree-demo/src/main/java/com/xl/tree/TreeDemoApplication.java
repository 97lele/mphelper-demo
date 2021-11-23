package com.xl.tree;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author tanjl11
 * @date 2021/11/22 15:33
 */
@SpringBootApplication
@ComponentScan(value = {"com.xl.mphelper","com.xl.tree"})
@MapperScan(basePackages = "com.xl.tree.mapper")
public class TreeDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(TreeDemoApplication.class, args);
    }
}
