package com.xl.shardingjdbc.demo.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xl.shardingjdbc.demo.entity.OrderInfo;
import com.xl.shardingjdbc.demo.mapper.OrderInfoMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> {
    @Resource
    private OrderInfoMapper orderInfoMapper;

    public void testDataBase() {
//        List<OrderInfo> collect = IntStream.range(0, 10)
//                .mapToObj(this::genOrder)
//                .collect(Collectors.toList());
//        this.saveBatch(collect);
        Page<OrderInfo> orderInfoPage = orderInfoMapper.selectPage(new Page<>(0, 5), Wrappers.lambdaQuery(OrderInfo.class)
        .orderByDesc(OrderInfo::getUserName)
        );
        System.out.println(orderInfoPage.getRecords());
    }

    OrderInfo genOrder(int i) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setUserId((long) (i % 2));
        orderInfo.setTotalAmount(BigDecimal.valueOf(ThreadLocalRandom.current().nextLong(100)));
        orderInfo.setUserName(i + " ");
        orderInfo.setCreateTime(LocalDateTime.now());
        orderInfo.setUpdateTime(LocalDateTime.now());
        return orderInfo;
    }
}
