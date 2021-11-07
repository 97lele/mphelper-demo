package com.xl.mphelper.example;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xl.mphelper.example.controller.OrderController;
import com.xl.mphelper.example.entity.OrderInfo;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;

/**
 * @author tanjl11
 * @date 2021/10/27 9:42
 */
@SpringBootApplication(scanBasePackages = "com.xl.mphelper.*")
@MapperScan(basePackages = "com.xl.mphelper.example.mapper")
@Slf4j
public class MpHelperApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(MpHelperApplication.class, args);
        OrderController controller = run.getBean(OrderController.class);
        List<OrderInfo> orderInfos = controller.testAdd();
        String suffix = orderInfos.get(0).suffix();
        Page<OrderInfo> orderInfoPage = controller.queryByPage(suffix);
        log.info("分页查询{}", orderInfoPage.getRecords().size());
        List<OrderInfo> query = controller.query(suffix);
        log.info("查询所有{}", query.size());

    }
}
