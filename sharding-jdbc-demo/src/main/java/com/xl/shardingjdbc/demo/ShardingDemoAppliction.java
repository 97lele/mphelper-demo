package com.xl.shardingjdbc.demo;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.xl.shardingjdbc.demo.service.OrderServiceImpl;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(value = {"com.xl.mphelper", "com.xl.shardingjdbc.demo"})
@MapperScan(basePackages = "com.xl.shardingjdbc.demo.mapper")
public class ShardingDemoAppliction {
    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(ShardingDemoAppliction.class, args);
        OrderServiceImpl bean = run.getBean(OrderServiceImpl.class);
        bean.testDataBase();
    }

    @Bean
    public MybatisPlusInterceptor interceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }
}
